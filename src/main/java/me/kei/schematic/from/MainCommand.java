package me.kei.schematic.from;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class MainCommand implements CommandExecutor, TabCompleter {
    Plugin plugin;

    public MainCommand(SchemCB schemCB) {
        this.plugin = schemCB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length < 4) {
            sender.sendMessage(ChatColor.RED + "/schemcb <schem file> <x> <y> <z>");
            return true;
        }

        Block block;
        Supplier<EditSession> sessionFactory;

        if (sender instanceof Player) {
            if (!sender.isOp()) {
                sender.sendMessage(ChatColor.RED + "Require OP.");
                return true;
            }
            Player player = (Player) sender;
            block = player.getLocation().getBlock();
            com.sk89q.worldedit.entity.Player wPlayer = BukkitAdapter.adapt(player);
            sessionFactory = () -> WorldEdit.getInstance().getSessionManager().get(wPlayer).createEditSession(wPlayer);
        } else if (sender instanceof BlockCommandSender) {
            BlockCommandSender player = (BlockCommandSender) sender;
            block = player.getBlock();
            sessionFactory = () -> WorldEdit.getInstance().getEditSessionFactory().getEditSession(BukkitAdapter.adapt(block.getWorld()), -1);
        } else {
            sender.sendMessage(ChatColor.RED + "You must be a player or command block.");
            return true;
        }

        BlockVector3 position;
        try {
            position = BlockVector3.at(
                    args[1].startsWith("~") ? block.getX() + (args[1].length() == 1 ? 0 : Integer.parseInt(args[1].substring(1))) : Integer.parseInt(args[1]),
                    args[2].startsWith("~") ? block.getY() + (args[2].length() == 1 ? 0 : Integer.parseInt(args[2].substring(1))) : Integer.parseInt(args[2]),
                    args[3].startsWith("~") ? block.getZ() + (args[3].length() == 1 ? 0 : Integer.parseInt(args[3].substring(1))) : Integer.parseInt(args[3])
            );
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "Error: Invalid location");
            return true;
        }

        Clipboard clipboard;
        try {
            File file = new File(new File("."), "plugins/WorldEdit/schematics/" + args[0] + ".schem");
            ClipboardFormat format = ClipboardFormats.findByFile(file);
            if (format == null) {
                sender.sendMessage(ChatColor.RED + "Error: Unknown Schematic Format");
                return true;
            }
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();
            }
        } catch (IOException e) {
            sender.sendMessage(ChatColor.RED + "Error: IO Error");
            return true;
        }

        try {
            try (EditSession editSession = sessionFactory.get()) {
                Operation operation = new ClipboardHolder(clipboard)
                        .createPaste(editSession)
                        .to(position)
                        .ignoreAirBlocks(false)
                        .build();
                Operations.complete(operation);
            }

            sender.sendMessage(ChatColor.GREEN + "Successfully placed!");
        } catch (WorldEditException e) {
            sender.sendMessage(ChatColor.RED + "Error: WorldEdit Error: " + e.getLocalizedMessage());
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        switch (args.length) {
            case 1:
                return Collections.singletonList("<schem>");
            case 2:
                return Collections.singletonList("<x>");
            case 3:
                return Collections.singletonList("<y>");
            case 4:
                return Collections.singletonList("<z>");
        }
        return Collections.emptyList();
    }
}

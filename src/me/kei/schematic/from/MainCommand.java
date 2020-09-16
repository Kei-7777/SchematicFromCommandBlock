package me.kei.schematic.from;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.extent.clipboard.io.MCEditSchematicReader;
import com.sk89q.worldedit.function.mask.ExistingBlockMask;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.transform.AffineTransform;
import com.sk89q.worldedit.math.transform.Transform;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import net.minecraft.server.v1_15_R1.WorldData;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;

public class MainCommand implements CommandExecutor, TabCompleter {
    Plugin plugin;

    public MainCommand(SchemCB schemCB) {
        this.plugin = schemCB;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.isOp()){
            sender.sendMessage(ChatColor.RED + "Require OP.");
            return true;
        }

        if(args.length < 4){
            sender.sendMessage(ChatColor.RED + "/schemcb <schem file> <x> <y> <z>");
            return true;
        }

        if (sender instanceof Player || sender instanceof BlockCommandSender) {
            if (sender instanceof Player) {
                try {
                    File file = new File("." + File.separator + "plugins" + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + args[0] + ".schem");
                    ClipboardFormat format = ClipboardFormats.findByFile(file);
                    Clipboard clipboard;
                    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                        clipboard = reader.read();
                    }

                    try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(((Player) sender).getWorld()), -1)) {
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(
                                        Integer.parseInt(args[1]),
                                        Integer.parseInt(args[2]),
                                        Integer.parseInt(args[3])
                                ))
                                .ignoreAirBlocks(false)
                                .build();
                        Operations.complete(operation);
                    }
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ex.getLocalizedMessage());
                }
            } else if (sender instanceof BlockCommandSender) {
                try {
                    File file = new File("." + File.separator + "plugins" + File.separator + "WorldEdit" + File.separator + "schematics" + File.separator + args[0] + ".schem");
                    ClipboardFormat format = ClipboardFormats.findByFile(file);
                    Clipboard clipboard;
                    try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                        clipboard = reader.read();
                    }

                    try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(new BukkitWorld(((BlockCommandSender) sender).getBlock().getWorld()), -1)) {
                        Operation operation = new ClipboardHolder(clipboard)
                                .createPaste(editSession)
                                .to(BlockVector3.at(
                                        Integer.parseInt(args[1]),
                                        Integer.parseInt(args[2]),
                                        Integer.parseInt(args[3])
                                ))
                                .ignoreAirBlocks(false)
                                .build();
                        Operations.complete(operation);
                    }
                } catch (Exception ex) {
                    sender.sendMessage(ChatColor.RED + "Error: " + ex.getLocalizedMessage());
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }
}

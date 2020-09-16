package me.kei.schematic.from;

import org.bukkit.plugin.java.JavaPlugin;

public class SchemCB extends JavaPlugin {

    @Override
    public void onEnable() {
        this.getServer().getPluginCommand("schemcb").setExecutor(new MainCommand(this));
    }
}

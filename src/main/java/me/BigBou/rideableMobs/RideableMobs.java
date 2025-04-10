package me.BigBou.rideableMobs;

import me.BigBou.rideableMobs.command.RideableMobsCommand;
import me.BigBou.rideableMobs.listener.PacketListener;
import me.BigBou.rideableMobs.listener.PlayerListener;
import me.BigBou.rideableMobs.util.Util;
import org.bukkit.plugin.java.JavaPlugin;

public final class RideableMobs extends JavaPlugin {
    public RideableMobs() {
    }

    public void onEnable() {
        this.getConfig().options().copyDefaults();
        this.saveDefaultConfig();
        Util.onEnable(this);
        RideableMobsCommand rideableMobsCommand = new RideableMobsCommand(this);
        this.getCommand("rideablemobs").setExecutor(rideableMobsCommand);
        this.getCommand("rideablemobs").setTabCompleter(rideableMobsCommand);
        PacketListener.onEnable(this);
        this.getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }

    public void onDisable() {
    }
}

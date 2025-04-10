package me.BigBou.rideableMobs.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Player;
import org.bukkit.entity.WaterMob;
import org.bukkit.plugin.Plugin;

public class Util {
    private static Plugin plugin;
    public static final Set<World> ENABLED_WORLDS = new HashSet();

    public Util() {
    }

    public static void onEnable(Plugin plugin) {
        Util.plugin = plugin;
        getWorlds();
    }

    public static void onReload(CommandSender sender) {
        plugin.reloadConfig();
        ENABLED_WORLDS.clear();
        getWorlds();
        sender.sendMessage(ChatColor.GREEN + "RideableMobs reload complete.");
    }

    public static boolean canSwim(Entity entity) {
        boolean var10000;
        switch (entity.getType()) {
            case DROWNED:
            case GUARDIAN:
            case TURTLE:
            case AXOLOTL:
                var10000 = true;
                break;
            default:
                var10000 = entity instanceof WaterMob;
        }

        return var10000;
    }

    public static boolean canFly(Entity entity) {
        boolean var10000;
        switch (entity.getType()) {
            case ALLAY:
            case BAT:
            case BEE:
            case BLAZE:
            case ENDER_DRAGON:
            case PARROT:
            case VEX:
            case WITHER:
                var10000 = true;
                break;
            default:
                var10000 = entity instanceof Flying;
        }

        return var10000;
    }

    public static boolean isWorldEnabled(Player player) {
        return ENABLED_WORLDS.contains(player.getWorld());
    }

    private static void getWorlds() {
        List<String> worlds = plugin.getConfig().getStringList("worlds");
        worlds.forEach((world) -> {
            World w = Bukkit.getWorld(world);
            ENABLED_WORLDS.add(w);
        });
    }
}
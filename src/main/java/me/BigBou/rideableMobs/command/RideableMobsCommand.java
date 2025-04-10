package me.BigBou.rideableMobs.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import me.BigBou.rideableMobs.util.Util;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.StringUtil;

public class RideableMobsCommand implements CommandExecutor, TabCompleter {
    private JavaPlugin plugin;
    private static HashMap<UUID, PermissionAttachment> permissions;
    private static List<String> availableCommands = new ArrayList<String>() {
        {
            this.add("permission");
            this.add("reload");
        }
    };

    public RideableMobsCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        permissions = new HashMap();
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("rideablemobs.commands")) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return false;
        } else if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Invalid usage. Use /rideablemobs <command>.");
            return false;
        } else if ("permission".equals(args[0])) {
            if (args.length < 4) {
                sender.sendMessage(ChatColor.RED + "Invalid usage. Use /rideablemobs permission <player> <entity_type> <true/false>.");
                return false;
            } else {
                Player player = Bukkit.getPlayer(args[1]);
                if (player == null) {
                    sender.sendMessage(ChatColor.RED + "Player not found.");
                    return false;
                } else {
                    EntityType entityType;
                    try {
                        entityType = EntityType.valueOf(args[2].toUpperCase());
                    } catch (IllegalArgumentException var11) {
                        sender.sendMessage(ChatColor.RED + "Invalid entity type.");
                        return false;
                    }

                    boolean value;
                    try {
                        value = Boolean.parseBoolean(args[3]);
                    } catch (IllegalArgumentException var10) {
                        sender.sendMessage(ChatColor.RED + "Invalid value. Use true or false.");
                        return false;
                    }

                    PermissionAttachment attachment = (PermissionAttachment)permissions.computeIfAbsent(player.getUniqueId(), (playerUUID) -> player.addAttachment(this.plugin));
                    String var10001 = entityType.name().toLowerCase();
                    attachment.setPermission("rideablemobs.ride." + var10001, value);
                    ChatColor var12 = ChatColor.GREEN;
                    sender.sendMessage(var12 + "Permission " + (value ? "set" : "removed") + " successfully.");
                    Optional<String> message = Optional.of(ChatColor.translateAlternateColorCodes('&', (String)Objects.requireNonNull(this.plugin.getConfig().getString(value ? "granted-permission" : "revoked-permission"))));
                    Optional<String> var10000 = message.map((m) -> m.replaceAll("%entity_type%", entityType.name()));
                    Objects.requireNonNull(player);
                    var10000.ifPresent(player::sendMessage);
                    return true;
                }
            }
        } else if ("reload".equals(args[0])) {
            Util.onReload(sender);
            return true;
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid command.");
            return false;
        }
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        ArrayList var10000;
        switch (args.length) {
            case 1 -> var10000 = (ArrayList)StringUtil.copyPartialMatches(args[0], availableCommands, new ArrayList());
            case 2 -> var10000 = (ArrayList)StringUtil.copyPartialMatches(args[1], Bukkit.getOnlinePlayers().stream().map(Player::getName).toList(), new ArrayList());
            case 3 -> var10000 = (ArrayList)StringUtil.copyPartialMatches(args[2], Arrays.stream(EntityType.values()).filter(EntityType::isAlive).map((entityType) -> entityType.name().toLowerCase()).toList(), new ArrayList());
            case 4 -> var10000 = (ArrayList)StringUtil.copyPartialMatches(args[3], List.of("true", "false"), new ArrayList());
            default -> var10000 = null;
        }

        return var10000;
    }
}

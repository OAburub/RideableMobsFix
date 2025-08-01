package me.BigBou.rideableMobs.util;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import me.BigBou.rideableMobs.listener.InputListener;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Util {
    public static Plugin getPlugin() {
        return plugin;
    }

    private static Plugin plugin;
    public static final Set<World> ENABLED_WORLDS = new HashSet<>();
    private static NamespacedKey entityFreezeModifierKey;
    public static AttributeModifier getEntityFreezeModifier(){
         return new AttributeModifier(entityFreezeModifierKey, -1, AttributeModifier.Operation.MULTIPLY_SCALAR_1);
    };
    public static BukkitRunnable getRunnable(Player player){
        return new BukkitRunnable() {
            @Override
            public void run() {
                if (Util.isWorldEnabled(player)) {
                    Entity vehicle = player.getVehicle();
                    if (vehicle != null && !(vehicle instanceof Vehicle)) {
                        InputListener.WrappedInput input = InputListener.playerInputs.get(player.getUniqueId());
                        if (input == null) {
                            return; // No input data available
                        }
                        if (!input.isSneak()) {
                            // Inside the update/tick loop
                            int forward = input.isForward() ? 1 : (input.isBackward() ? -1 : 0);
                            int sideways = input.isLeft() ? 1 : (input.isRight() ? -1 : 0);

                            // Get player's yaw and pitch
                            Location playerLocation = player.getLocation();
                            float yaw = playerLocation.getYaw();
                            float pitch = playerLocation.getPitch();
                            vehicle.setRotation(yaw, pitch);

                            // Convert yaw from degrees to radians
                            double radians = Math.toRadians(yaw);

                            // Calculate movement in X and Z directions
                            double x = (-forward) * Math.sin(radians) + sideways * Math.cos(radians);
                            double z = forward * Math.cos(radians) + sideways * Math.sin(radians);

                            // Create a velocity vector
                            Vector velocity = new Vector(x, 0.0, z).normalize().multiply(plugin.getConfig().getDouble("entity-speed") * (input.isSprint()? 1.3 : 1));  // Adjust speed as needed

                            // Retain the vertical velocity (e.g., gravity, flying, etc.)
                            velocity.setY(vehicle.getVelocity().getY());

                            // Ensure no non-finite values in the velocity vector
                            if (!Double.isFinite(velocity.getX())) {
                                velocity.setX(0);
                            }
                            if (!Double.isFinite(velocity.getZ())) {
                                velocity.setZ(0);
                            }

                            // Handle special movement cases (e.g., in water, flying)
                            if (vehicle.isInWater() && !Util.canSwim(vehicle) && !vehicle.isOnGround()) {
                                velocity.setY(-0.08);  // Slight downward velocity to simulate sinking
                            }

                            if (Util.canFly(vehicle) && !vehicle.isOnGround()) {
                                velocity.setY(-0.08);  // Slight downward velocity to simulate flying gravity
                            }

                            // Jumping logic (if the player is jumping and can fly or is on the ground)
                            if (input.isJump() && (Util.canFly(vehicle) || Util.canSwim(vehicle) && vehicle.isInWater() || vehicle.isOnGround())) {
                                velocity.setY(0.5);  // Apply upward velocity for jumping
                            }

                            // Special handling for EnderDragon (reverse its rotation)
                            if (vehicle instanceof EnderDragon enderDragon) {
                                enderDragon.setRotation(yaw + 180.0F, pitch);  // Reverse the dragon's rotation
                            }
                            // Apply the velocity to the vehicle
                            vehicle.setVelocity(velocity);

                        }
                    }
                }
            }
        };
    }

    public Util() {
    }

    public static void onEnable(Plugin plugin) {
        Util.plugin = plugin;
        entityFreezeModifierKey = new NamespacedKey(plugin, "entity_freeze");
        getWorlds();
    }

    public static void onReload(CommandSender sender) {
        plugin.reloadConfig();
        ENABLED_WORLDS.clear();
        getWorlds();
        sender.sendMessage(Component.text("RideableMobs reload complete.").color(NamedTextColor.GREEN));
    }

    public static boolean canSwim(Entity entity) {
        return switch (entity.getType()) {
            case DROWNED, GUARDIAN, TURTLE, AXOLOTL -> true;
            default -> entity instanceof WaterMob;
        };
    }

    public static boolean canFly(Entity entity) {
        return switch (entity.getType()) {
            case ALLAY, BAT, BEE, BLAZE, ENDER_DRAGON, PARROT, VEX, WITHER -> true;
            default -> entity instanceof Flying;
        };
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
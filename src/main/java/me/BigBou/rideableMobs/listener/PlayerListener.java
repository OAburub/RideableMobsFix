package me.BigBou.rideableMobs.listener;

import me.BigBou.rideableMobs.util.Util;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.entity.EnderDragon.Phase;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class PlayerListener implements Listener {
    private final JavaPlugin plugin;
    private final Map<Material, Collection<PotionEffect>> foodEffects = new HashMap<>();
    private final Map<UUID, BukkitTask> taskMap = new HashMap<>();

    public PlayerListener(JavaPlugin plugin) {
        this.plugin = plugin;
        this.foodEffects.put(Material.ENCHANTED_GOLDEN_APPLE, List.of(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 3), new PotionEffect(PotionEffectType.REGENERATION, 600, 1), new PotionEffect(PotionEffectType.FIRE_RESISTANCE, 6000, 0), new PotionEffect(PotionEffectType.RESISTANCE, 6000, 0)));
        this.foodEffects.put(Material.GOLDEN_APPLE, List.of(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0), new PotionEffect(PotionEffectType.REGENERATION, 100, 1)));
    }

    @EventHandler
    public void onInteractAtEntity(PlayerInteractAtEntityEvent e) {
        EquipmentSlot hand = e.getHand();
        if (hand == EquipmentSlot.HAND) {
            Entity entity = e.getRightClicked();
            if (entity.getPassengers().isEmpty() && (!(entity instanceof Vehicle) || entity instanceof ZombieHorse || entity instanceof SkeletonHorse)) {
                Player player = e.getPlayer();
                if (Util.isWorldEnabled(player)) {
                    EntityType type = entity.getType();
                    String entityType = !(entity instanceof ComplexEntityPart) && !(entity instanceof ComplexLivingEntity) ? type.name().toLowerCase() : "ender_dragon";
                    boolean isEnabled = !(entity instanceof ComplexEntityPart) && !(entity instanceof ComplexLivingEntity) ? this.plugin.getConfig().getBoolean(entityType) : this.plugin.getConfig().getBoolean("ender_dragon");
                    if (!isEnabled) {
                        String message = this.plugin.getConfig().getString("entity-disabled");

                        assert message != null;

                        if (!message.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                    } else if (!player.hasPermission("rideablemobs.ride." + entityType)) {
                        String message = this.plugin.getConfig().getString("no-permission-ride");

                        assert message != null;

                        if (!message.isEmpty()) {
                            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
                        }

                    } else {
                        boolean shouldBeEmptyHand = this.plugin.getConfig().getBoolean("requires-empty-hand");
                        if (shouldBeEmptyHand && !player.getInventory().getItemInMainHand().getType().isAir()) {
                            ItemStack stack = player.getInventory().getItemInMainHand();
                            Material material = stack.getType();
                            if (material.isEdible()) {
                                LivingEntity livingEntity = (LivingEntity)entity;
                                AttributeInstance ai = livingEntity.getAttribute(Attribute.MAX_HEALTH);
                                switch (material) {
                                    case ENCHANTED_GOLDEN_APPLE:
                                        livingEntity.addPotionEffects(this.foodEffects.get(Material.ENCHANTED_GOLDEN_APPLE));

                                        assert ai != null;

                                        livingEntity.setHealth(ai.getValue());
                                        player.getWorld().spawnParticle(Particle.HEART, livingEntity.getEyeLocation(), 5);
                                        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                                            stack.setAmount(stack.getAmount() - 1);
                                        }
                                        break;
                                    case GOLDEN_APPLE:
                                        livingEntity.addPotionEffects(this.foodEffects.get(Material.GOLDEN_APPLE));

                                        assert ai != null;

                                        livingEntity.setHealth(Math.min(livingEntity.getHealth() + (double)10.0F, ai.getValue()));
                                        player.getWorld().spawnParticle(Particle.HEART, livingEntity.getEyeLocation(), 3);
                                        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                                            stack.setAmount(stack.getAmount() - 1);
                                        }
                                        break;
                                    case APPLE:
                                        assert ai != null;

                                        livingEntity.setHealth(Math.min(livingEntity.getHealth() + (double)3.0F, ai.getValue()));
                                        player.getWorld().spawnParticle(Particle.HEART, livingEntity.getEyeLocation(), 1);
                                        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
                                            stack.setAmount(stack.getAmount() - 1);
                                        }
                                }
                            }

                        } else {
                            // start the movement task
                            taskMap.put(player.getUniqueId(), Util.getRunnable(player).runTaskTimer(Util.getPlugin(), 0,0));
                            if (entity instanceof ArmorStand && !player.isSneaking()) {
                                entity.addPassenger(player);
                            } else if (entity instanceof ComplexEntityPart entityPart) {
                                EnderDragon enderDragon = (EnderDragon) entityPart.getParent();
                                enderDragon.setPhase(Phase.FLY_TO_PORTAL);
                                enderDragon.addPassenger(player);
                            } else if (!(entity instanceof ArmorStand)) {
                                entity.addPassenger(player);
                                // prevent Moving entities from moving on their own
                                if (entity instanceof Attributable attributable){
                                    AttributeInstance movementSpeedAttr = attributable.getAttribute(Attribute.MOVEMENT_SPEED);
                                    if (movementSpeedAttr != null){
                                        movementSpeedAttr.setBaseValue(0.0);
                                    }
                                }
                            }

                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        if (e.getEntity() instanceof Player player) {
            Entity vehicle = e.getDismounted();
            if (vehicle instanceof EnderDragon enderDragon) {
                enderDragon.setPhase(Phase.HOVER);
            }
            // make moving entities able to move on their own again
            else if (vehicle instanceof Attributable attributable){
                AttributeInstance movementSpeedAttr = attributable.getAttribute(Attribute.MOVEMENT_SPEED);
                if (movementSpeedAttr != null){
                    movementSpeedAttr.setBaseValue(movementSpeedAttr.getDefaultValue());
                }
            }
            // cancel and remove the movement task
            taskMap.get(player.getUniqueId()).cancel();
            taskMap.remove(player.getUniqueId());
            if (Util.canSwim(vehicle) && vehicle.isInWater() && !player.isSneaking()) {
                e.setCancelled(true);
            }

        }
    }
}

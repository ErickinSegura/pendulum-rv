package org.delta.listeners.chargebase.mobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.MobRegistry;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class HealerBehaviorListener implements Listener {

    private final pendulum plugin;
    private final ChargeBaseManager manager;
    private final Random rng = new Random();

    private static final double HEAL_RADIUS = 20.0;
    private static final double HEAL_AMOUNT = 18.0;
    private static final double RESURRECT_CHANCE = 0.35;
    private static final double RESURRECT_RADIUS = 30.0;

    private static final double POTION_THROW_CHANCE = 0.3;
    private final Set<UUID> potionCooldown = new HashSet<>();

    public HealerBehaviorListener(pendulum plugin, ChargeBaseManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        startHealLoop();
    }

    private void startHealLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!manager.isActive()) return;

                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (!(e instanceof LivingEntity healer)) continue;
                        if (!healer.getScoreboardTags().contains("healer_basico") &&
                                !healer.getScoreboardTags().contains("healer_avanzado")) continue;
                        healNearbyAllies(healer);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void healNearbyAllies(LivingEntity healer) {
        if (!manager.isActive() || manager.getSpawnManager() == null) return;

        healer.getNearbyEntities(HEAL_RADIUS, HEAL_RADIUS, HEAL_RADIUS).forEach(nearby -> {
            if (!(nearby instanceof LivingEntity ally)) return;
            if (ally instanceof Player) return;
            if (!manager.getSpawnManager().isManagedMob(ally.getUniqueId())) return;

            double maxHp = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
            ally.setHealth(Math.min(maxHp, ally.getHealth() + HEAL_AMOUNT));
            ally.getWorld().spawnParticle(Particle.HEART, ally.getLocation().add(0, 1.5, 0), 3);
        });
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAllyDeath(EntityDeathEvent event) {
        if (!manager.isActive() || manager.getSpawnManager() == null) return;
        LivingEntity dead = event.getEntity();
        if (dead instanceof Player) return;
        if (!manager.getSpawnManager().isManagedMob(dead.getUniqueId())) return;
        potionCooldown.remove(event.getEntity().getUniqueId());

        dead.getNearbyEntities(RESURRECT_RADIUS, RESURRECT_RADIUS, RESURRECT_RADIUS).stream()
                .filter(e -> e instanceof LivingEntity le &&
                        le.getScoreboardTags().contains("healer_avanzado"))
                .findFirst()
                .ifPresent(healerEntity -> {
                    if (rng.nextDouble() > RESURRECT_CHANCE) return;

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        LivingEntity healer = (LivingEntity) healerEntity;
                        if (!healer.isValid()) return;

                        Location loc = dead.getLocation();
                        LivingEntity resurrected = respawnMob(dead, loc);
                        if (resurrected == null) return;

                        double maxHp = resurrected.getAttribute(Attribute.MAX_HEALTH).getValue();
                        resurrected.setHealth(maxHp * 0.4);

                        loc.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc.add(0, 1, 0), 30);
                        loc.getWorld().playSound(loc, Sound.ITEM_TOTEM_USE, 1.0f, 1.5f);
                    }, 1L);
                });
    }

    private LivingEntity respawnMob(LivingEntity dead, Location loc) {
        for (String tag : dead.getScoreboardTags()) {
            Optional<CustomMob> custom = MobRegistry.get(tag, plugin, loc);
            if (custom.isEmpty()) continue;
            if (custom.get().getMobClass() == MobClass.HEALER) return null;

            LivingEntity entity = custom.get().build();
            manager.getSpawnManager().registerSpawnedMob(entity.getUniqueId(), custom.get().getMobClass());
            return entity;
        }
        return null;
    }



    @EventHandler
    public void onHealerDamaged(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity healer)) return;
        if (!healer.getScoreboardTags().contains("healer_basico") &&
                !healer.getScoreboardTags().contains("healer_avanzado")) return;
        if (potionCooldown.contains(healer.getUniqueId())) return;
        if (rng.nextDouble() > POTION_THROW_CHANCE) return;

        Location healerLoc = healer.getLocation();
        org.bukkit.util.Vector toPlayer = event.getDamager().getLocation().toVector()
                .subtract(healerLoc.toVector())
                .normalize();

        Location throwLoc = healerLoc.clone().add(0, 1.5, 0);

        ThrownPotion potion = healer.getWorld().spawn(throwLoc, ThrownPotion.class);
        org.bukkit.inventory.ItemStack potionItem = new org.bukkit.inventory.ItemStack(org.bukkit.Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta meta = (org.bukkit.inventory.meta.PotionMeta) potionItem.getItemMeta();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.POISON, 100, 1, false, true), true);
        potionItem.setItemMeta(meta);
        potion.setItem(potionItem);
        potion.setVelocity(toPlayer.multiply(0.8));

        potionCooldown.add(healer.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () -> potionCooldown.remove(healer.getUniqueId()), 60L);
    }
}

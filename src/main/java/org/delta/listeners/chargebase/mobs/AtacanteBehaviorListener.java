package org.delta.listeners.chargebase;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.pendulum;

import java.util.*;

public class AtacanteBehaviorListener implements Listener {

    private final pendulum plugin;
    private final Set<UUID> freneticActive = new HashSet<>();
    private final Set<UUID> dashCooldown = new HashSet<>();
    private final Random rng = new Random();

    private static final double FRENETIC_THRESHOLD = 0.5;
    private static final long DASH_INTERVAL = 100L; // cada 5s

    public AtacanteBehaviorListener(pendulum plugin) {
        this.plugin = plugin;
        startDashLoop();
    }

    @EventHandler
    public void onAtacanteDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (!mob.getScoreboardTags().contains("atacante_basico")) return;
        if (freneticActive.contains(mob.getUniqueId())) return;

        double healthAfter = mob.getHealth() - event.getFinalDamage();
        double threshold = mob.getAttribute(Attribute.MAX_HEALTH).getValue() * FRENETIC_THRESHOLD;

        if (healthAfter <= threshold) {
            freneticActive.add(mob.getUniqueId());
            mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 2, false, true));
            mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, true));
            mob.getWorld().spawnParticle(Particle.LAVA, mob.getLocation().add(0, 1, 0), 20);
        }
    }

    private void startDashLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (!(e instanceof LivingEntity mob)) continue;
                        if (!mob.getScoreboardTags().contains("atacante_avanzado")) continue;
                        if (dashCooldown.contains(mob.getUniqueId())) continue;

                        Player nearest = nearestPlayer(mob);
                        if (nearest == null) continue;
                        if (mob.getLocation().distanceSquared(nearest.getLocation()) > 400) continue; // máx 20 bloques

                        performDash(mob, nearest);
                    }
                }
            }
        }.runTaskTimer(plugin, DASH_INTERVAL, DASH_INTERVAL);
    }

    private void performDash(LivingEntity mob, Player target) {
        org.bukkit.util.Vector dir = target.getLocation().toVector()
                .subtract(mob.getLocation().toVector())
                .normalize()
                .multiply(2.5);
        dir.setY(0.4);

        mob.setVelocity(dir);
        mob.getWorld().spawnParticle(Particle.LAVA, mob.getLocation().add(0, 1, 0), 15);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.2f);

        dashCooldown.add(mob.getUniqueId());
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                dashCooldown.remove(mob.getUniqueId()), DASH_INTERVAL);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (mob.getLocation().distanceSquared(target.getLocation()) <= 4) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60, 0, false, true));
                target.getWorld().spawnParticle(Particle.CRIT, target.getLocation().add(0, 1, 0), 10);
            }
        }, 10L);
    }

    private Player nearestPlayer(LivingEntity mob) {
        return mob.getWorld().getPlayers().stream()
                .filter(p -> !p.isDead() && p.getGameMode() == GameMode.SURVIVAL)
                .min(Comparator.comparingDouble(a -> a.getLocation().distanceSquared(mob.getLocation())))
                .orElse(null);
    }

    @EventHandler
    public void onAtacanteDeath(EntityDeathEvent event) {
        freneticActive.remove(event.getEntity().getUniqueId());
        dashCooldown.remove(event.getEntity().getUniqueId());
    }
}
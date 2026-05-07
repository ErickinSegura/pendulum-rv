package org.delta.listeners.chargebase.mobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class ControladorAvanzadoListener implements Listener {

    private final pendulum plugin;
    private final Set<UUID> phaseActive = new HashSet<>();
    private final Set<UUID> windZones = new HashSet<>();
    private final Random rng = new Random();

    private static final double TETHER_RANGE = 12.0;
    private static final double PHASE_THRESHOLD = 0.5;

    public ControladorAvanzadoListener(pendulum plugin) {
        this.plugin = plugin;
        startWindZoneTask();
    }


    private void startWindZoneTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (!(e instanceof LivingEntity mob)) continue;
                        if (!mob.getScoreboardTags().contains("controlador_avanzado")) continue;

                        spawnWindZone(mob);
                    }
                }
            }
        }.runTaskTimer(plugin, 100L, 100L);
    }

    private void spawnWindZone(LivingEntity mob) {
        Location center = mob.getLocation();
        World world = center.getWorld();

        double angle = rng.nextDouble() * 2 * Math.PI;
        double dist = 3 + rng.nextDouble() * 5;
        Location zoneCenter = center.clone().add(
                dist * Math.cos(angle), 0, dist * Math.sin(angle)
        );
        zoneCenter.setY(world.getHighestBlockYAt(zoneCenter.getBlockX(), zoneCenter.getBlockZ()) + 0.1);

        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 60) { cancel(); return; }
                for (double a = 0; a < 2 * Math.PI; a += Math.PI / 8) {
                    world.spawnParticle(Particle.GUST,
                            zoneCenter.clone().add(2 * Math.cos(a), 0.1, 2 * Math.sin(a)),
                            1, 0, 0, 0, 0);
                }
                for (Player p : world.getPlayers()) {
                    if (p.getLocation().distanceSquared(zoneCenter) <= 4) { // radio 2
                        p.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 40, 2, false, true));
                    }
                }
                ticks += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    @EventHandler
    public void onBreezeTarget(EntityTargetEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (!mob.getScoreboardTags().contains("controlador_avanzado")) return;
        if (!(event.getTarget() instanceof Player)) return;

        startTetherCheck(mob, (Player) event.getTarget());
    }

    private void startTetherCheck(LivingEntity mob, Player target) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!mob.isValid() || !target.isOnline()) { cancel(); return; }
                if (!(mob instanceof Mob mobCast)) { cancel(); return; }
                if (mobCast.getTarget() == null || !mobCast.getTarget().equals(target)) { cancel(); return; }

                double dist = mob.getLocation().distance(target.getLocation());
                if (dist > TETHER_RANGE) {
                    org.bukkit.util.Vector dir = mob.getLocation()
                            .toVector()
                            .subtract(target.getLocation().toVector())
                            .normalize()
                            .multiply(1.5);
                    dir.setY(0.3);
                    target.setVelocity(dir);
                    target.getWorld().spawnParticle(Particle.GUST, target.getLocation().add(0, 1, 0), 10);
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }

    @EventHandler
    public void onBreezeDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (!mob.getScoreboardTags().contains("controlador_avanzado")) return;

        double healthAfter = mob.getHealth() - event.getFinalDamage();
        double threshold = mob.getAttribute(Attribute.MAX_HEALTH).getValue() * PHASE_THRESHOLD;

        if (healthAfter <= threshold) {
            phaseActive.add(mob.getUniqueId());
        }

        if (phaseActive.contains(mob.getUniqueId())) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (!mob.isValid()) return;
                double a = rng.nextDouble() * 2 * Math.PI;
                double d = 2 + rng.nextDouble() * 3;
                Location teleport = mob.getLocation().clone().add(
                        d * Math.cos(a), 0, d * Math.sin(a)
                );
                teleport.setY(mob.getWorld().getHighestBlockYAt(teleport.getBlockX(), teleport.getBlockZ()) + 1);
                mob.getWorld().spawnParticle(Particle.GUST, mob.getLocation().add(0, 1, 0), 15);
                mob.teleport(teleport);
                mob.getWorld().spawnParticle(Particle.GUST, mob.getLocation().add(0, 1, 0), 15);
            }, 1L);
        }
    }

    @EventHandler
    public void onBreezeDeath(EntityDeathEvent event) {
        phaseActive.remove(event.getEntity().getUniqueId());
    }
}

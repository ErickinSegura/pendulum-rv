package org.delta.listeners.chargebase.mobs;

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
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class HibridoAvanzadoListener implements Listener {

    private final pendulum plugin;
    private final ChargeBaseManager manager;

    private final Set<UUID> shieldCooldown = new HashSet<>();
    private final Map<UUID, Integer> currentPhase = new HashMap<>();

    private static final double PHASE2_THRESHOLD = 0.66;
    private static final double PHASE3_THRESHOLD = 0.33;
    private static final double CONTROL_RADIUS = 14.0;
    private static final double REGEN_AMOUNT = 6.0;

    public HibridoAvanzadoListener(pendulum plugin, ChargeBaseManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        startControlLoop();
        startRegenLoop();
    }

    private void startControlLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                forEachHibrido(mob -> {
                    int phase = getPhase(mob);
                    if (phase < 1) return;

                    mob.getNearbyEntities(CONTROL_RADIUS, CONTROL_RADIUS, CONTROL_RADIUS).forEach(nearby -> {
                        if (!(nearby instanceof Player player)) return;
                        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, true));
                        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, phase, false, true)); // nivel sube con fase
                    });

                    mob.getWorld().spawnParticle(Particle.SCULK_SOUL,
                            mob.getLocation().add(0, 1.5, 0), 10, 1, 0.5, 1, 0.05);
                });
            }
        }.runTaskTimer(plugin, 0L, 80L);
    }


    private void startRegenLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                forEachHibrido(mob -> {
                    if (getPhase(mob) < 3) return;

                    double maxHp = mob.getAttribute(Attribute.MAX_HEALTH).getValue();
                    mob.setHealth(Math.min(maxHp, mob.getHealth() + REGEN_AMOUNT));
                    mob.getWorld().spawnParticle(Particle.HEART,
                            mob.getLocation().add(0, 2, 0), 3, 0.5, 0.3, 0.5, 0);
                });
            }
        }.runTaskTimer(plugin, 0L, 40L);
    }

    @EventHandler
    public void onHibridoDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity mob)) return;
        if (!mob.getScoreboardTags().contains("hibrido_avanzado")) return;

        double healthAfter = mob.getHealth() - event.getFinalDamage();
        double maxHp = mob.getAttribute(Attribute.MAX_HEALTH).getValue();
        int newPhase = calculatePhase(healthAfter, maxHp);
        int oldPhase = currentPhase.getOrDefault(mob.getUniqueId(), 1);

        if (newPhase > oldPhase) {
            currentPhase.put(mob.getUniqueId(), newPhase);
            announcePhase(mob, newPhase);
        }

        if (newPhase < 3) return;
        if (shieldCooldown.contains(mob.getUniqueId())) return;

        event.setCancelled(true);
        shieldCooldown.add(mob.getUniqueId());

        mob.getWorld().spawnParticle(Particle.SCULK_CHARGE_POP,
                mob.getLocation().add(0, 1.5, 0), 20, 0.5, 0.5, 0.5, 0.1);
        mob.getWorld().playSound(mob.getLocation(), Sound.BLOCK_SCULK_SHRIEKER_SHRIEK, 1.0f, 1.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () ->
                shieldCooldown.remove(mob.getUniqueId()), 40L); // cooldown 2s
    }

    @EventHandler
    public void onHibridoDeath(EntityDeathEvent event) {
        if (!event.getEntity().getScoreboardTags().contains("hibrido_avanzado")) return;
        UUID uid = event.getEntity().getUniqueId();
        shieldCooldown.remove(uid);
        currentPhase.remove(uid);
        Location loc = event.getEntity().getLocation();
        loc.getWorld().spawnParticle(Particle.SCULK_SOUL, loc.add(0, 1, 0), 20, 1, 0.5, 1, 0.05);
    }

    private int getPhase(LivingEntity mob) {
        return currentPhase.getOrDefault(mob.getUniqueId(), 1);
    }

    private int calculatePhase(double healthAfter, double maxHp) {
        double ratio = healthAfter / maxHp;
        if (ratio <= PHASE3_THRESHOLD) return 3;
        if (ratio <= PHASE2_THRESHOLD) return 2;
        return 1;
    }

    private void announcePhase(LivingEntity mob, int phase) {
        mob.getWorld().spawnParticle(Particle.SCULK_SOUL,
                mob.getLocation().add(0, 2, 0), 15, 1, 0.5, 1, 0.05);
        mob.getWorld().playSound(mob.getLocation(), Sound.ENTITY_WARDEN_ROAR, 0.8f, 1.0f);
    }

    private void forEachHibrido(java.util.function.Consumer<LivingEntity> action) {
        for (World world : Bukkit.getWorlds()) {
            for (Entity e : world.getEntities()) {
                if (!(e instanceof LivingEntity mob)) continue;
                if (!mob.getScoreboardTags().contains("hibrido_avanzado")) continue;
                action.accept(mob);
            }
        }
    }

}
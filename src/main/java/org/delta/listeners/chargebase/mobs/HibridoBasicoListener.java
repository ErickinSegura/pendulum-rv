package org.delta.listeners.chargebase.mobs;

import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HibridoBasicoListener implements Listener {

    private final pendulum plugin;
    private final ChargeBaseManager manager;
    private final Set<UUID> invulnerable = new HashSet<>();
    private final Set<UUID> fuseStarted = new HashSet<>();

    private static final double CHAIN_RADIUS = 8.0;
    private static final double HEAL_RADIUS = 10.0;
    private static final double ELECTRIC_ZONE_RADIUS = 4.0;

    public HibridoBasicoListener(pendulum plugin, ChargeBaseManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        startElectricAmbient();
    }

    private void startElectricAmbient() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity e : world.getEntities()) {
                        if (!(e instanceof Creeper creeper)) continue;
                        if (!creeper.getScoreboardTags().contains("hibrido_basico")) continue;
                        if (creeper.isIgnited()) continue;

                        Location loc = creeper.getLocation().add(0, 0.5, 0);
                        world.spawnParticle(Particle.ELECTRIC_SPARK, loc, 5, 0.2, 0.3, 0.2, 0);
                        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_IMPACT, 0.2f, 2.0f);
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 15L);
    }

    @EventHandler
    public void onFuseStart(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        if (!creeper.getScoreboardTags().contains("hibrido_basico")) return;
        if (fuseStarted.contains(creeper.getUniqueId())) return;

        fuseStarted.add(creeper.getUniqueId());

        creeper.getNearbyEntities(6, 6, 6).forEach(nearby -> {
            if (!(nearby instanceof Player player)) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 60, 0, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60, 1, false, true));
        });

        invulnerable.add(creeper.getUniqueId());
        creeper.getWorld().spawnParticle(Particle.ELECTRIC_SPARK, creeper.getLocation().add(0, 1, 0), 20);
        Bukkit.getScheduler().runTaskLater(plugin, () ->
                invulnerable.remove(creeper.getUniqueId()), 30L);

        creeper.getNearbyEntities(CHAIN_RADIUS, CHAIN_RADIUS, CHAIN_RADIUS).forEach(nearby -> {
            if (!(nearby instanceof Creeper other)) return;
            if (!other.getScoreboardTags().contains("hibrido_basico")) return;
            if (other.getUniqueId().equals(creeper.getUniqueId())) return;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (other.isValid()) {
                    other.ignite();
                    other.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                            other.getLocation().add(0, 1, 0), 15);
                    other.getWorld().playSound(other.getLocation(),
                            Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.5f, 1.5f);
                }
            }, 10L);
        });
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        if (!creeper.getScoreboardTags().contains("hibrido_basico")) return;

        Location loc = creeper.getLocation();
        World world = loc.getWorld();

        creeper.getNearbyEntities(HEAL_RADIUS, HEAL_RADIUS, HEAL_RADIUS).forEach(nearby -> {
            if (!(nearby instanceof LivingEntity ally)) return;
            if (ally instanceof Player) return;
            if (manager.getSpawnManager() == null) return;
            if (!manager.getSpawnManager().isManagedMob(ally.getUniqueId())) return;

            double maxHp = ally.getAttribute(Attribute.MAX_HEALTH).getValue();
            ally.setHealth(Math.min(maxHp, ally.getHealth() + 20.0));
            world.spawnParticle(Particle.HEART, ally.getLocation().add(0, 1.5, 0), 5);
        });

        spawnElectricZone(loc);

        fuseStarted.remove(creeper.getUniqueId());
        invulnerable.remove(creeper.getUniqueId());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!event.getEntity().getScoreboardTags().contains("hibrido_basico")) return;

        Location loc = event.getEntity().getLocation();
        World world = loc.getWorld();

        world.spawnParticle(Particle.ELECTRIC_SPARK, loc.add(0, 1, 0), 40, 1, 1, 1, 0.3);
        world.playSound(loc, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 1.0f, 1.5f);

        world.getNearbyEntities(loc, 5, 5, 5).forEach(nearby -> {
            if (!(nearby instanceof Player player)) return;
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 80, 1, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 80, 0, false, true));
            player.damage(4.0);
        });

        fuseStarted.remove(event.getEntity().getUniqueId());
        invulnerable.remove(event.getEntity().getUniqueId());
    }

    private void spawnElectricZone(Location center) {
        new BukkitRunnable() {
            int ticks = 0;
            @Override
            public void run() {
                if (ticks >= 100) { cancel(); return; } // 5 segundos

                for (double a = 0; a < 2 * Math.PI; a += Math.PI / 8) {
                    center.getWorld().spawnParticle(Particle.ELECTRIC_SPARK,
                            center.clone().add(
                                    ELECTRIC_ZONE_RADIUS * Math.cos(a),
                                    0.1,
                                    ELECTRIC_ZONE_RADIUS * Math.sin(a)),
                            1, 0, 0, 0, 0);
                }

                center.getWorld().getNearbyEntities(center, ELECTRIC_ZONE_RADIUS, 1, ELECTRIC_ZONE_RADIUS)
                        .forEach(e -> {
                            if (!(e instanceof Player player)) return;
                            player.damage(1.0);
                            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 20, 0, false, false));
                        });

                ticks += 5;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }
}
package org.delta.listeners.spawns;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.pendulum;

public class EndCreeperListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double CHANCE = 0.10;
    private static final String TAG = "ender_creeper";

    private static final long CHASE_INTERVAL = 60L;
    private static final double CHASE_MIN_RADIUS = 2.5;
    private static final double CHASE_RADIUS = 5.0;
    private static final double DODGE_RADIUS = 8.0;
    private static final double DETECT_RANGE = 24.0;

    private final pendulum plugin;

    public EndCreeperListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;
        if (event.getEntity().getType() != EntityType.ENDERMAN) return;

        World world = event.getEntity().getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;
        if (random.nextDouble() >= CHANCE) return;

        event.setCancelled(true);
        Creeper creeper = world.spawn(event.getEntity().getLocation(), Creeper.class, c -> {
            c.setPowered(true);
            c.setSilent(true);
            c.addScoreboardTag(TAG);
        });
        iniciarPersecucion(creeper);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Creeper creeper)) return;
        if (!creeper.getScoreboardTags().contains(TAG)) return;

        Location dest = puntoEnSuelo(creeper.getWorld(), creeper.getLocation(), 0, DODGE_RADIUS);
        if (dest != null) parpadear(creeper, dest);
    }

    private void iniciarPersecucion(Creeper creeper) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (creeper.isDead() || !creeper.isValid()) {
                    cancel();
                    return;
                }

                Player target = nearestPlayer(creeper);
                if (target == null) return;

                Location dest = puntoEnSuelo(creeper.getWorld(), target.getLocation(), CHASE_MIN_RADIUS, CHASE_RADIUS);
                if (dest != null) parpadear(creeper, dest);
            }
        }.runTaskTimer(plugin, CHASE_INTERVAL, CHASE_INTERVAL);
    }

    private void parpadear(Creeper creeper, Location dest) {
        World world = creeper.getWorld();
        world.spawnParticle(Particle.PORTAL, creeper.getLocation().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        creeper.teleport(dest);
        world.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
        world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
    }

    private Location puntoEnSuelo(World world, Location base, double minRadius, double maxRadius) {
        for (int intento = 0; intento < 6; intento++) {
            double angle = random.nextDouble() * 2 * Math.PI;
            double dist = minRadius + random.nextDouble() * (maxRadius - minRadius);
            int x = base.getBlockX() + (int) Math.round(Math.cos(angle) * dist);
            int z = base.getBlockZ() + (int) Math.round(Math.sin(angle) * dist);
            Block highest = world.getHighestBlockAt(x, z);
            if (highest.getType().isSolid()) {
                return highest.getLocation().add(0.5, 1, 0.5);
            }
        }
        return null;
    }

    private Player nearestPlayer(Creeper creeper) {
        Player nearest = null;
        double best = DETECT_RANGE * DETECT_RANGE;
        for (Player player : creeper.getWorld().getPlayers()) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
            double distance = player.getLocation().distanceSquared(creeper.getLocation());
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }
}

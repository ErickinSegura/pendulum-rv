package org.delta.listeners.spawns;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Enderman;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.pendulum;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EndermanHostileListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double HOSTILE_CHANCE = 0.10;
    private static final double AGGRO_RANGE = 32.0;
    private static final long CHECK_INTERVAL = 40L;

    private static final double TELEPORT_CHANCE = 0.35;
    private static final double TELEPORT_RADIUS = 20.0;
    private static final double TELEPORT_MAX_RANGE = 5.0;
    private static final long TELEPORT_COOLDOWN_MS = 10_000L;

    private final pendulum plugin;
    private final Map<UUID, Long> lastTeleportByPlayer = new ConcurrentHashMap<>();

    public EndermanHostileListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;
        if (event.getEntity().getType() != EntityType.ENDERMAN) return;
        if (event.getEntity().getWorld().getEnvironment() != World.Environment.THE_END) return;
        if (random.nextDouble() >= HOSTILE_CHANCE) return;

        startAggro((Enderman) event.getEntity());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        lastTeleportByPlayer.remove(event.getPlayer().getUniqueId());
    }

    private void startAggro(Enderman enderman) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (enderman.isDead() || !enderman.isValid()) {
                    cancel();
                    return;
                }

                LivingEntity target = enderman.getTarget();
                if (target instanceof Player current && !current.isDead()) {
                    if (current.getWorld() != enderman.getWorld()
                            || current.getLocation().distanceSquared(enderman.getLocation())
                                    > TELEPORT_MAX_RANGE * TELEPORT_MAX_RANGE) {
                        return;
                    }
                    long now = System.currentTimeMillis();
                    Long last = lastTeleportByPlayer.get(current.getUniqueId());
                    if ((last == null || now - last >= TELEPORT_COOLDOWN_MS)
                            && random.nextDouble() < TELEPORT_CHANCE
                            && teleport(enderman, current)) {
                        lastTeleportByPlayer.put(current.getUniqueId(), now);
                    }
                    return;
                }

                Player nearest = nearestPlayer(enderman);
                if (nearest != null) enderman.setTarget(nearest);
            }
        }.runTaskTimer(plugin, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    private boolean teleport(Enderman enderman, Player target) {
        World world = enderman.getWorld();
        int bx = target.getLocation().getBlockX() + (int) ((random.nextDouble() * 2 - 1) * TELEPORT_RADIUS);
        int bz = target.getLocation().getBlockZ() + (int) ((random.nextDouble() * 2 - 1) * TELEPORT_RADIUS);

        Block highest = world.getHighestBlockAt(bx, bz);
        if (!highest.getType().isSolid()) return false;

        Location dest = highest.getLocation().add(0.5, 1, 0.5);
        dest.setYaw(target.getLocation().getYaw());
        dest.setPitch(target.getLocation().getPitch());

        target.teleport(dest);
        world.spawnParticle(Particle.PORTAL, dest, 40, 0.3, 0.6, 0.3, 0.1);
        world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);
        target.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        return true;
    }

    private Player nearestPlayer(Enderman enderman) {
        Player nearest = null;
        double best = AGGRO_RANGE * AGGRO_RANGE;
        for (Player player : enderman.getWorld().getPlayers()) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
            double distance = player.getLocation().distanceSquared(enderman.getLocation());
            if (distance < best) {
                best = distance;
                nearest = player;
            }
        }
        return nearest;
    }
}

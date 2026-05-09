package org.delta.managers.chargebase;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.delta.pendulum;

public class ChargeBaseZone {
    private final Location center;
    private final double initialRadius;
    private double currentRadius;
    private int[] groundCache;
    private int cachedSteps;

    public ChargeBaseZone(Location center, double initialRadius) {
        this.center = center;
        this.initialRadius = initialRadius;
        this.currentRadius = initialRadius;
        Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), this::rebuildGroundCache, 1L);
    }

    private void rebuildGroundCache() {
        cachedSteps = (int) Math.min(720, Math.max(48, (2 * Math.PI * currentRadius) / 1.0));
        groundCache = new int[cachedSteps];
        World world = center.getWorld();
        for (int i = 0; i < cachedSteps; i++) {
            double angle = (2 * Math.PI / cachedSteps) * i;
            double x = center.getX() + currentRadius * Math.cos(angle);
            double z = center.getZ() + currentRadius * Math.sin(angle);
            groundCache[i] = world.getHighestBlockYAt((int) x, (int) z);
        }
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double dx = loc.getX() - center.getX();
        double dz = loc.getZ() - center.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= currentRadius;
    }

    public void spawnParticles() {
        World world = center.getWorld();
        if (world == null || groundCache == null || cachedSteps == 0) return;

        Particle.DustOptions wall = new Particle.DustOptions(Color.fromRGB(220, 50, 255), 2.5f);
        double heightSpacing = 2.0;
        double arcRadians = Math.PI / 3; // arco de 60° visible por jugador

        for (Player player : world.getPlayers()) {
            Location pLoc = player.getLocation();

            // ángulo del jugador relativo al centro
            double playerAngle = Math.atan2(
                    pLoc.getZ() - center.getZ(),
                    pLoc.getX() - center.getX()
            );

            // solo renderiza los steps dentro del arco
            for (int i = 0; i < cachedSteps; i++) {
                double angle = (2 * Math.PI / cachedSteps) * i;

                // diferencia angular normalizada
                double diff = Math.abs(angle - playerAngle);
                if (diff > Math.PI) diff = 2 * Math.PI - diff;
                if (diff > arcRadians) continue; // fuera del arco, skip

                double x = center.getX() + currentRadius * Math.cos(angle);
                double z = center.getZ() + currentRadius * Math.sin(angle);
                int groundY = groundCache[i];

                for (double y = groundY - 2; y <= groundY + 64; y += heightSpacing) {
                    world.spawnParticle(Particle.DUST,
                            new Location(world, x, y, z), 1, 0, 0, 0, 0, wall, true);
                }
            }
        }
    }

    public void shrink(double amount) {
        currentRadius = Math.max(0, currentRadius - amount);
        rebuildGroundCache();
    }

    public Location getCenter()       { return center; }
    public double    getCurrentRadius() { return currentRadius; }
    public double    getInitialRadius() { return initialRadius; }
}
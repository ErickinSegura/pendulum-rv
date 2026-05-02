package org.delta.managers.chargebase;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ChargeBaseZone {
    private final Location center;
    private final double initialRadius;
    private double currentRadius;
    private final int baseY;

    public ChargeBaseZone(Location center, double initialRadius) {
        this.center  = center;
        this.initialRadius = initialRadius;
        this.currentRadius = initialRadius;
        this.baseY = center.getWorld().getHighestBlockYAt(center.getBlockX(), center.getBlockZ());
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double dx = loc.getX() - center.getX();
        double dz = loc.getZ() - center.getZ();
        return Math.sqrt(dx * dx + dz * dz) <= currentRadius;
    }

    public void spawnParticles() {
        World world = center.getWorld();
        Particle.DustOptions wall = new Particle.DustOptions(Color.fromRGB(180, 0, 255), 1.5f);

        int wallSteps = (int) Math.min(360, Math.max(24, (2 * Math.PI * currentRadius) / 1.5));

        int yBottom = baseY - 8;
        int yTop    = baseY + 64;
        double heightSpacing = 2.0;

        for (double y = yBottom; y <= yTop; y += heightSpacing) {
            for (int i = 0; i < wallSteps; i++) {
                double angle = (2 * Math.PI / wallSteps) * i;
                double x = center.getX() + currentRadius * Math.cos(angle);
                double z = center.getZ() + currentRadius * Math.sin(angle);
                world.spawnParticle(Particle.DUST, x, y, z, 1, wall);
            }
        }
    }

    public void shrink(double amount) {
        currentRadius = Math.max(0, currentRadius - amount);
    }

    public Location getCenter()       { return center; }
    public double    getCurrentRadius() { return currentRadius; }
    public double    getInitialRadius() { return initialRadius; }
}
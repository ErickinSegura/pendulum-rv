package org.delta.managers.chargebase;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;

public class ChargeBaseZone {
    private final Location center;
    private final double initialRadius;
    private double currentRadius;

    public ChargeBaseZone(Location center, double initialRadius) {
        this.center = center;
        this.initialRadius = initialRadius;
        this.currentRadius = initialRadius;
    }

    public void spawnParticles() {
        World world = center.getWorld();
        int steps = 72;
        int heightSteps = 8;      // cuántos "pisos" de partículas
        double heightSpacing = 1.5; // espacio entre pisos

        for (int h = 0; h < heightSteps; h++) {
            double yOffset = h * heightSpacing;
            for (int i = 0; i < steps; i++) {
                double angle = (2 * Math.PI / steps) * i;
                double x = center.getX() + currentRadius * Math.cos(angle);
                double z = center.getZ() + currentRadius * Math.sin(angle);
                int baseY = world.getHighestBlockYAt((int) x, (int) z);
                world.spawnParticle(Particle.DUST, x, baseY + yOffset, z, 1,
                        new Particle.DustOptions(Color.fromRGB(180, 0, 255), 1.5f));
            }
        }
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        return center.distance(loc) <= currentRadius;
    }

    public void shrink(double amount) {
        currentRadius = Math.max(0, currentRadius - amount);
    }

    public Location getCenter() { return center; }
    public double getCurrentRadius() { return currentRadius; }
    public double getInitialRadius() { return initialRadius; }
}
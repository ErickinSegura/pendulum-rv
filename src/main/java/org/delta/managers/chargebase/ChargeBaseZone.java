package org.delta.managers.chargebase;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ChargeBaseZone {

    // ── Muro translúcido continuo ─────────────────────────────────────────
    private static final double   WALL_SEGMENT    = 4.0;     // largo de cada panel
    private static final float    WALL_HEIGHT     = 35.0f;    // alto del muro
    private static final float    WALL_THICKNESS  = 0.2f;    // grosor del muro
    private static final Material WALL_MATERIAL   = Material.MAGENTA_STAINED_GLASS;
    private static final float    WALL_VIEW_RANGE = 4.0f;

    // ── Beams (haces de luz) en los puntos mayores ────────────────────────
    private static final int      MAJOR_EVERY     = 5;       // 1 beam cada 5 paneles
    private static final float    BEAM_WIDTH      = 0.6f;
    private static final float    BEAM_HEIGHT     = 35.0f;
    private static final Material BEAM_MATERIAL   = Material.SEA_LANTERN;
    private static final float    BEAM_VIEW_RANGE = 8.0f;

    private static final AxisAngle4f NO_ROT = new AxisAngle4f(0, 0, 0, 1);

    private final Location           center;
    private final double             initialRadius;
    private double                   currentRadius;
    private final List<BlockDisplay> displays = new ArrayList<>();

    public ChargeBaseZone(Location center, double initialRadius) {
        this.center        = center;
        this.initialRadius = initialRadius;
        this.currentRadius = initialRadius;
        spawnDisplays();
    }

    // ─────────────────────────────────────────────────────────────────────

    private void spawnDisplays() {
        removeDisplays();
        World world = center.getWorld();
        if (world == null) return;

        int steps = (int) Math.max(48, (2 * Math.PI * currentRadius) / WALL_SEGMENT);

        double[] xs = new double[steps];
        double[] zs = new double[steps];
        double[] ys = new double[steps];

        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI / steps) * i;
            double x     = center.getX() + currentRadius * Math.cos(angle);
            double z     = center.getZ() + currentRadius * Math.sin(angle);
            xs[i] = x;
            zs[i] = z;
            ys[i] = world.getHighestBlockYAt((int) Math.floor(x), (int) Math.floor(z));
        }

        for (int i = 0; i < steps; i++) {
            int next = (i + 1) % steps;
            double dx = xs[next] - xs[i];
            double dz = zs[next] - zs[i];
            double width = Math.sqrt(dx * dx + dz * dz);
            float  yaw   = (float) Math.atan2(-dz, dx);

            Location base = new Location(world, xs[i], ys[i], zs[i]);
            spawnWallPanel(world, base, width, yaw);

            if (i % MAJOR_EVERY == 0) {
                spawnBeam(world, base);
            }
        }
    }

    // ── Panel del muro: cubo escalado y rotado tangente al círculo ────────

    private void spawnWallPanel(World world, Location base, double width, float yaw) {
        float cos = (float) Math.cos(yaw);
        float sin = (float) Math.sin(yaw);
        Vector3f translation = new Vector3f(-(WALL_THICKNESS / 2f) * sin, 0f, -(WALL_THICKNESS / 2f) * cos);
        AxisAngle4f rot = new AxisAngle4f(yaw, 0f, 1f, 0f);

        displays.add(world.spawn(base, BlockDisplay.class, e -> {
            e.setBlock(WALL_MATERIAL.createBlockData());
            e.setPersistent(false);
            e.setVisibleByDefault(true);
            e.setViewRange(WALL_VIEW_RANGE);
            e.setBrightness(new Display.Brightness(15, 15));
            e.setTransformation(new Transformation(
                    translation,
                    rot,
                    new Vector3f((float) width, WALL_HEIGHT, WALL_THICKNESS),
                    NO_ROT
            ));
        }));
    }

    // ── Beam: columna alta y brillante ────────────────────────────────────

    private void spawnBeam(World world, Location base) {
        float half = BEAM_WIDTH / 2f;
        displays.add(world.spawn(base, BlockDisplay.class, e -> {
            e.setBlock(BEAM_MATERIAL.createBlockData());
            e.setPersistent(false);
            e.setVisibleByDefault(true);
            e.setViewRange(BEAM_VIEW_RANGE);
            e.setBrightness(new Display.Brightness(15, 15));
            e.setTransformation(new Transformation(
                    new Vector3f(-half, 0f, -half),
                    NO_ROT,
                    new Vector3f(BEAM_WIDTH, BEAM_HEIGHT, BEAM_WIDTH),
                    NO_ROT
            ));
        }));
    }

    // ─────────────────────────────────────────────────────────────────────

    public void removeDisplays() {
        displays.forEach(e -> { if (e != null && !e.isDead()) e.remove(); });
        displays.clear();
    }

    public void shrink(double amount) {
        currentRadius = Math.max(0, currentRadius - amount);
        spawnDisplays();
    }

    public boolean isInside(Location loc) {
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double dx = loc.getX() - center.getX();
        double dz = loc.getZ() - center.getZ();
        return (dx * dx + dz * dz) <= (currentRadius * currentRadius);
    }

    public Location getCenter()        { return center; }
    public double   getCurrentRadius() { return currentRadius; }
    public double   getInitialRadius() { return initialRadius; }
}

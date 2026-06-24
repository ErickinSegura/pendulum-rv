package org.delta.managers.chargebase;

import org.bukkit.*;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

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

    private final Location center;
    private final double   initialRadius;
    private double         currentRadius;

    private int           steps;
    private double[]      segX;
    private double[]      segZ;
    private double[]      segWidth;
    private float[]       segYaw;
    private BlockDisplay[] wallDisplays;
    private BlockDisplay[] beamDisplays;

    public ChargeBaseZone(Location center, double initialRadius) {
        this.center        = center;
        this.initialRadius = initialRadius;
        this.currentRadius = initialRadius;
        recomputeGeometry();
        refresh();
    }

    // ─────────────────────────────────────────────────────────────────────

    private void recomputeGeometry() {
        removeDisplays();
        steps = (int) Math.max(48, (2 * Math.PI * currentRadius) / WALL_SEGMENT);

        segX     = new double[steps];
        segZ     = new double[steps];
        segWidth = new double[steps];
        segYaw   = new float[steps];
        wallDisplays = new BlockDisplay[steps];
        beamDisplays = new BlockDisplay[steps];

        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI / steps) * i;
            segX[i] = center.getX() + currentRadius * Math.cos(angle);
            segZ[i] = center.getZ() + currentRadius * Math.sin(angle);
        }
        for (int i = 0; i < steps; i++) {
            int next = (i + 1) % steps;
            double dx = segX[next] - segX[i];
            double dz = segZ[next] - segZ[i];
            segWidth[i] = Math.sqrt(dx * dx + dz * dz);
            segYaw[i]   = (float) Math.atan2(-dz, dx);
        }
    }

    public void refresh() {
        World world = center.getWorld();
        if (world == null || segX == null) return;

        for (int i = 0; i < steps; i++) {
            int bx = (int) Math.floor(segX[i]);
            int bz = (int) Math.floor(segZ[i]);
            boolean loaded = world.isChunkLoaded(bx >> 4, bz >> 4);

            if (loaded) {
                boolean needWall = wallDisplays[i] == null || !wallDisplays[i].isValid();
                boolean needBeam = i % MAJOR_EVERY == 0 && (beamDisplays[i] == null || !beamDisplays[i].isValid());
                if (!needWall && !needBeam) continue;

                double y = world.getHighestBlockYAt(bx, bz);
                Location base = new Location(world, segX[i], y, segZ[i]);
                if (needWall) wallDisplays[i] = spawnWallPanel(world, base, segWidth[i], segYaw[i]);
                if (needBeam) beamDisplays[i] = spawnBeam(world, base);
            } else {
                if (wallDisplays[i] != null) { if (wallDisplays[i].isValid()) wallDisplays[i].remove(); wallDisplays[i] = null; }
                if (beamDisplays[i] != null) { if (beamDisplays[i].isValid()) beamDisplays[i].remove(); beamDisplays[i] = null; }
            }
        }
    }

    // ── Panel del muro: cubo escalado y rotado tangente al círculo ────────

    private BlockDisplay spawnWallPanel(World world, Location base, double width, float yaw) {
        float cos = (float) Math.cos(yaw);
        float sin = (float) Math.sin(yaw);
        Vector3f translation = new Vector3f(-(WALL_THICKNESS / 2f) * sin, 0f, -(WALL_THICKNESS / 2f) * cos);
        AxisAngle4f rot = new AxisAngle4f(yaw, 0f, 1f, 0f);

        return world.spawn(base, BlockDisplay.class, e -> {
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
        });
    }

    // ── Beam: columna alta y brillante ────────────────────────────────────

    private BlockDisplay spawnBeam(World world, Location base) {
        float half = BEAM_WIDTH / 2f;
        return world.spawn(base, BlockDisplay.class, e -> {
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
        });
    }

    // ─────────────────────────────────────────────────────────────────────

    public void removeDisplays() {
        if (wallDisplays != null) {
            for (BlockDisplay e : wallDisplays) if (e != null && e.isValid()) e.remove();
        }
        if (beamDisplays != null) {
            for (BlockDisplay e : beamDisplays) if (e != null && e.isValid()) e.remove();
        }
        wallDisplays = null;
        beamDisplays = null;
    }

    public void shrink(double amount) {
        currentRadius = Math.max(0, currentRadius - amount);
        recomputeGeometry();
        refresh();
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

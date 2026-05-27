package org.delta.managers.chargebase;

import org.bukkit.*;
import org.bukkit.block.data.Orientable;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class ChargeBaseZone {

    // ── Escala SIEMPRE uniforme → sin stretch de textura ─────────────────
    private static final float  CUBE_SCALE  = 0.40f;   // cubo perfecto

    // ── Pilares menores: agujas de amatista ───────────────────────────────
    private static final int    MINOR_STACK = 4;        // 4 × 0.4 = 1.6 bl

    // ── Pilares mayores: columna purpur + orbe ────────────────────────────
    private static final int    MAJOR_STACK = 10;       // 10 × 0.4 = 4.0 bl
    private static final float  ORB_SIZE    = 0.45f;    // cubo uniforme
    private static final int    MAJOR_EVERY = 6;

    private static final double ARC_SPACING = 18.0;

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

        int steps = (int) Math.max(16, (2 * Math.PI * currentRadius) / ARC_SPACING);

        for (int i = 0; i < steps; i++) {
            double angle = (2 * Math.PI / steps) * i;
            double x     = center.getX() + currentRadius * Math.cos(angle);
            double z     = center.getZ() + currentRadius * Math.sin(angle);
            int    baseY = world.getHighestBlockYAt((int) x, (int) z);
            Location base = new Location(world, x, baseY, z);

            if (i % MAJOR_EVERY == 0) spawnMajorPillar(world, base);
            else                      spawnMinorPillar(world, base);
        }
    }

    // ── Apila n cubos uniformes de un material dado ───────────────────────

    private void spawnStack(World world, Location base,
                            Material mat, int count, float size, float viewRange) {
        float half = size / 2f;
        for (int j = 0; j < count; j++) {
            final float yOff = j * size;
            Location loc = base.clone().add(0, yOff, 0);
            displays.add(world.spawn(loc, BlockDisplay.class, e -> {
                e.setBlock(mat.createBlockData());
                e.setPersistent(false);
                e.setVisibleByDefault(true);
                e.setViewRange(viewRange);
                // translación centra X/Z; Y crece hacia arriba desde base
                e.setTransformation(new Transformation(
                        new Vector3f(-half, 0f, -half),
                        NO_ROT,
                        new Vector3f(size, size, size),
                        NO_ROT
                ));
                e.setBrightness(new Display.Brightness(15, 15));
            }));
        }
    }

    // ── Pilar menor — agujas de amatista ──────────────────────────────────

    private void spawnMinorPillar(World world, Location base) {
        spawnStack(world, base, Material.AMETHYST_BLOCK, MINOR_STACK, CUBE_SCALE, 1.8f);
    }

    // ── Pilar mayor — columna de purpur + orbe sea lantern ────────────────

    private void spawnMajorPillar(World world, Location base) {
        // Base de dos cubos de amatista (acento de color)
        spawnStack(world, base, Material.AMETHYST_BLOCK, 2, CUBE_SCALE, 2.5f);

        // Cuerpo de purpur encima
        Location shaftBase = base.clone().add(0, 2 * CUBE_SCALE, 0);
        spawnStack(world, shaftBase, Material.PURPUR_BLOCK, MAJOR_STACK - 2, CUBE_SCALE, 2.5f);

        // Orbe flotando en el tope (pequeño gap visual de 0.1)
        double topY   = base.getY() + MAJOR_STACK * CUBE_SCALE + 0.1;
        float  half   = ORB_SIZE / 2f;
        Location orbLoc = new Location(world, base.getX(), topY, base.getZ());
        displays.add(world.spawn(orbLoc, BlockDisplay.class, e -> {
            e.setBlock(Material.SEA_LANTERN.createBlockData());
            e.setPersistent(false);
            e.setVisibleByDefault(true);
            e.setViewRange(2.5f);
            e.setTransformation(new Transformation(
                    new Vector3f(-half, 0f, -half),
                    NO_ROT,
                    new Vector3f(ORB_SIZE, ORB_SIZE, ORB_SIZE),
                    NO_ROT
            ));
            e.setBrightness(new Display.Brightness(15, 15));
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
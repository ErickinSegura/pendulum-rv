package org.delta.worldgen.structures;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.Directional;
import org.bukkit.entity.EntityType;
import org.delta.worldgen.LootTable;
import org.delta.worldgen.StructureDef;
import org.delta.worldgen.StructureTemplate;

public class SantuarioVacio extends StructureTemplate {

    private static final Material ISLAND = Material.END_STONE;
    private static final Material FLOOR = Material.END_STONE_BRICKS;
    private static final Material PILLAR = Material.PURPUR_PILLAR;
    private static final Material ACCENT = Material.PURPUR_BLOCK;
    private static final Material BEAM = Material.END_STONE_BRICKS;
    private static final Material STAIR = Material.PURPUR_STAIRS;

    private static final LootTable LOOT_VACIO = new LootTable.Builder("santuario_vacio_cofre")
            .rolls(6, 10)
            .entry(Material.OBSIDIAN, 3, 8, 22)
            .entry(Material.ENDER_PEARL, 2, 6, 24)
            .entry(Material.CHORUS_FRUIT, 4, 12, 20)
            .entry(Material.FIREWORK_ROCKET, 4, 10, 18)
            .entry(Material.GOLDEN_APPLE, 2, 4, 16)
            .entry(Material.EXPERIENCE_BOTTLE, 3, 8, 18)
            .entry(Material.DIAMOND, 2, 5, 16)
            .entry(Material.ENDER_EYE, 1, 3, 14)
            .entry(Material.DRAGON_BREATH, 1, 3, 10)
            .entry(Material.SHULKER_SHELL, 1, 2, 8)
            .entry(Material.GHAST_TEAR, 1, 2, 6)
            .entry(Material.NETHERITE_SCRAP, 1, 2, 5)
            .entry(Material.DIAMOND_BLOCK, 1, 1, 4)
            .entry(Material.END_CRYSTAL, 1, 1, 4)
            .entry(Material.ELYTRA, 1, 1, 1)
            .entry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 3)
            .build();

    private final StructureDef.Rotation rotation;

    public SantuarioVacio() {
        this(StructureDef.Rotation.ROT_0);
    }

    public SantuarioVacio(StructureDef.Rotation rotation) {
        this.rotation = rotation;
    }

    @Override
    public StructureDef build() {
        StructureDef.Builder b = new StructureDef.Builder("santuario_vacio")
                .spawnChance(0.0001)
                .minDay(10)
                .airSpawn(30, 40, 62)
                .notifyOnGenerate("&dEl &5&lVacío &dse ha plegado: un Santuario flota cerca de ti...")
                .rotation(rotation)
                .biomes(
                        Biome.END_HIGHLANDS,
                        Biome.END_MIDLANDS
                );

        island(b);
        platform(b);
        peristyle(b);
        towerCore(b);
        topShrine(b);
        cornerSpires(b);
        stairShaft(b);
        lighting(b);
        loot(b);
        boss(b);

        return b.build();
    }

    private void island(StructureDef.Builder b) {
        b.fill(9, 0, 9, 11, 0, 11, ISLAND);
        b.fill(7, 1, 7, 13, 1, 13, ISLAND);
        b.fill(5, 2, 5, 15, 2, 15, ISLAND);
        b.fill(3, 3, 3, 17, 3, 17, ISLAND);
        b.fill(1, 4, 1, 19, 4, 19, ISLAND);

        b.block(3, 2, 10, ISLAND);
        b.block(17, 2, 10, ISLAND);
        b.block(10, 2, 3, ISLAND);
        b.block(10, 2, 17, ISLAND);
        b.block(6, 1, 6, ISLAND);
        b.block(14, 1, 14, ISLAND);

        downRod(b, 2, 3, 10);
        downRod(b, 18, 3, 10);
        downRod(b, 10, 3, 2);
        downRod(b, 10, 3, 18);
        downRod(b, 4, 2, 4);
        downRod(b, 16, 2, 16);
    }

    private void platform(StructureDef.Builder b) {
        b.fill(0, 5, 0, 20, 5, 20, FLOOR);

        b.layer(5, 0, 20, 0, 0, ACCENT);
        b.layer(5, 0, 20, 20, 20, ACCENT);
        b.layer(5, 0, 0, 0, 20, ACCENT);
        b.layer(5, 20, 20, 0, 20, ACCENT);

        b.fill(10, 5, 1, 10, 5, 19, ACCENT);
        b.fill(1, 5, 10, 19, 5, 10, ACCENT);

        b.layer(5, 5, 15, 5, 5, Material.CRYING_OBSIDIAN);
        b.layer(5, 5, 15, 15, 15, Material.CRYING_OBSIDIAN);
        b.layer(5, 5, 5, 5, 15, Material.CRYING_OBSIDIAN);
        b.layer(5, 15, 15, 5, 15, Material.CRYING_OBSIDIAN);
    }

    private void peristyle(StructureDef.Builder b) {
        int[][] pillars = {
                {2, 2}, {6, 2}, {10, 2}, {14, 2}, {18, 2},
                {2, 18}, {6, 18}, {10, 18}, {14, 18}, {18, 18},
                {2, 6}, {2, 10}, {2, 14},
                {18, 6}, {18, 10}, {18, 14}
        };
        for (int[] p : pillars) {
            b.column(p[0], 6, 10, p[1], PILLAR);
            b.block(p[0], 8, p[1], ACCENT);
        }

        b.fill(2, 11, 2, 18, 11, 2, BEAM);
        b.fill(2, 11, 18, 18, 11, 18, BEAM);
        b.fill(2, 11, 2, 2, 11, 18, BEAM);
        b.fill(18, 11, 2, 18, 11, 18, BEAM);

        for (int x = 3; x <= 17; x++) {
            b.blockData(x, 12, 2, stair(BlockFace.NORTH, true));
            b.blockData(x, 12, 18, stair(BlockFace.SOUTH, true));
        }
        for (int z = 3; z <= 17; z++) {
            b.blockData(2, 12, z, stair(BlockFace.WEST, true));
            b.blockData(18, 12, z, stair(BlockFace.EAST, true));
        }

        b.block(2, 12, 2, ACCENT);
        b.block(18, 12, 2, ACCENT);
        b.block(2, 12, 18, ACCENT);
        b.block(18, 12, 18, ACCENT);
    }

    private void towerCore(StructureDef.Builder b) {
        ringWalls(b, 6, 6, 14, 14, 6, 11, BEAM);
        bandRing(b, 6, 6, 14, 14, 8, ACCENT);
        b.fill(9, 6, 14, 11, 9, 14, Material.AIR);

        b.fill(6, 12, 6, 14, 12, 14, FLOOR);

        ringWalls(b, 6, 6, 14, 14, 13, 17, BEAM);
        b.fill(10, 14, 6, 10, 15, 6, Material.AIR);
        b.fill(10, 14, 14, 10, 15, 14, Material.AIR);
        b.fill(6, 14, 10, 6, 15, 10, Material.AIR);
        b.fill(14, 14, 10, 14, 15, 10, Material.AIR);
        bandRing(b, 6, 6, 14, 14, 16, ACCENT);

        b.fill(6, 18, 6, 14, 18, 14, FLOOR);

        b.spawner(10, 6, 9, EntityType.ENDERMAN);
        b.spawner(10, 13, 11, EntityType.ENDERMAN);
    }

    private void topShrine(StructureDef.Builder b) {
        b.fill(9, 18, 9, 11, 18, 11, Material.OBSIDIAN);
        b.block(10, 18, 10, Material.CRYING_OBSIDIAN);

        ringWalls(b, 6, 6, 14, 14, 19, 19, Material.END_STONE_BRICK_WALL);
        for (int i = 6; i <= 14; i += 2) {
            b.block(i, 20, 6, Material.END_ROD);
            b.block(i, 20, 14, Material.END_ROD);
            b.block(6, 20, i, Material.END_ROD);
            b.block(14, 20, i, Material.END_ROD);
        }

        b.column(8, 19, 23, 8, PILLAR);
        b.column(12, 19, 23, 8, PILLAR);
        b.column(8, 19, 23, 12, PILLAR);
        b.column(12, 19, 23, 12, PILLAR);

        b.fill(8, 24, 8, 12, 24, 12, BEAM);
        b.fill(9, 25, 9, 11, 25, 11, ACCENT);
        b.block(10, 26, 10, Material.CRYING_OBSIDIAN);
        b.block(10, 27, 10, Material.DRAGON_HEAD);

        b.block(8, 25, 8, Material.END_ROD);
        b.block(12, 25, 8, Material.END_ROD);
        b.block(8, 25, 12, Material.END_ROD);
        b.block(12, 25, 12, Material.END_ROD);
    }

    private void cornerSpires(StructureDef.Builder b) {
        spire(b, 6, 6);
        spire(b, 14, 6);
        spire(b, 6, 14);
        spire(b, 14, 14);
    }

    private void spire(StructureDef.Builder b, int x, int z) {
        b.column(x, 6, 23, z, PILLAR);
        b.block(x, 24, z, ACCENT);
        b.block(x, 25, z, Material.END_ROD);
    }

    private void stairShaft(StructureDef.Builder b) {
        BlockData ladder = Material.LADDER.createBlockData();
        ((Directional) ladder).setFacing(BlockFace.EAST);
        for (int y = 6; y <= 18; y++) {
            b.blockData(7, y, 7, ladder);
        }
    }

    private void lighting(StructureDef.Builder b) {
        b.block(4, 6, 4, Material.END_ROD);
        b.block(16, 6, 4, Material.END_ROD);
        b.block(4, 6, 16, Material.END_ROD);
        b.block(16, 6, 16, Material.END_ROD);

        b.block(8, 13, 8, Material.END_ROD);
        b.block(12, 13, 12, Material.END_ROD);
    }

    private void loot(StructureDef.Builder b) {
        b.chest(4, 6, 4, LOOT_VACIO);
        b.chest(16, 6, 4, LOOT_VACIO);
        b.chest(4, 6, 16, LOOT_VACIO);
        b.chest(16, 6, 16, LOOT_VACIO);

        b.chest(12, 13, 8, LOOT_VACIO);
        b.chest(8, 13, 12, LOOT_VACIO);
    }

    private void ringWalls(StructureDef.Builder b, int x0, int z0, int x1, int z1,
                           int y0, int y1, Material mat) {
        b.fill(x0, y0, z0, x1, y1, z0, mat);
        b.fill(x0, y0, z1, x1, y1, z1, mat);
        b.fill(x0, y0, z0, x0, y1, z1, mat);
        b.fill(x1, y0, z0, x1, y1, z1, mat);
    }

    private void bandRing(StructureDef.Builder b, int x0, int z0, int x1, int z1,
                          int y, Material mat) {
        b.fill(x0, y, z0, x1, y, z0, mat);
        b.fill(x0, y, z1, x1, y, z1, mat);
        b.fill(x0, y, z0, x0, y, z1, mat);
        b.fill(x1, y, z0, x1, y, z1, mat);
    }

    private void boss(StructureDef.Builder b) {
        b.entity(10, 19, 10, EntityType.ENDERMAN, entity -> {
            org.bukkit.Location location = entity.getLocation();
            entity.remove();
            new org.delta.customs.mobs.boss.CustodioVacio(org.delta.pendulum.getInstance(), location).build();
        });
    }

    private BlockData stair(BlockFace facing, boolean top) {
        BlockData data = STAIR.createBlockData();
        ((Directional) data).setFacing(facing);
        if (data instanceof Bisected bisected) {
            bisected.setHalf(top ? Bisected.Half.TOP : Bisected.Half.BOTTOM);
        }
        return data;
    }

    private void downRod(StructureDef.Builder b, int x, int y, int z) {
        BlockData data = Material.END_ROD.createBlockData();
        ((Directional) data).setFacing(BlockFace.DOWN);
        b.blockData(x, y, z, data);
    }
}

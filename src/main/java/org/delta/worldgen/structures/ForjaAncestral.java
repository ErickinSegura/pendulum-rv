package org.delta.worldgen.structures;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Lantern;
import org.bukkit.entity.EntityType;
import org.delta.customs.mobs.boss.GuardianForja;
import org.delta.pendulum;
import org.delta.worldgen.LootTable;
import org.delta.worldgen.StructureDef;
import org.delta.worldgen.StructureTemplate;

public class ForjaAncestral extends StructureTemplate {

    public enum Variant {
        CLASSIC(Material.POLISHED_BLACKSTONE_BRICKS, Material.POLISHED_BLACKSTONE, Material.POLISHED_BLACKSTONE_BRICKS,
                Material.POLISHED_BLACKSTONE, Material.GILDED_BLACKSTONE, Material.CHISELED_POLISHED_BLACKSTONE,
                Material.POLISHED_BLACKSTONE_BRICK_WALL, 0, 0),
        DEEPSLATE(Material.DEEPSLATE_BRICKS, Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_TILES,
                Material.DEEPSLATE_BRICKS, Material.GILDED_BLACKSTONE, Material.CHISELED_DEEPSLATE,
                Material.DEEPSLATE_BRICK_WALL, 1, 1),
        NETHER(Material.NETHER_BRICKS, Material.RED_NETHER_BRICKS, Material.NETHER_BRICKS,
                Material.RED_NETHER_BRICKS, Material.GILDED_BLACKSTONE, Material.CHISELED_NETHER_BRICKS,
                Material.NETHER_BRICK_WALL, 2, 2);

        final Material wall, pillar, floor, border, accent, band, rail;
        final int towerCap, terraceStyle;

        Variant(Material wall, Material pillar, Material floor, Material border,
                Material accent, Material band, Material rail, int towerCap, int terraceStyle) {
            this.wall = wall;
            this.pillar = pillar;
            this.floor = floor;
            this.border = border;
            this.accent = accent;
            this.band = band;
            this.rail = rail;
            this.towerCap = towerCap;
            this.terraceStyle = terraceStyle;
        }
    }

    private final Material WALL;
    private final Material PILLAR;
    private final Material FLOOR;
    private final Material BORDER;
    private final Material ACCENT;
    private final Material BAND;
    private final Material RAIL;

    private static final LootTable LOOT_FORJA = new LootTable.Builder("forja_ancestral_cofre")
            .rolls(6, 10)
            .entry(Material.IRON_INGOT, 4, 9, 36)
            .entry(Material.GOLD_INGOT, 3, 7, 28)
            .entry(Material.COAL_BLOCK, 1, 4, 24)
            .entry(Material.IRON_BLOCK, 1, 3, 16)
            .entry(Material.OBSIDIAN, 3, 6, 14)
            .entry(Material.DIAMOND, 2, 5, 20)
            .entry(Material.BLAZE_ROD, 2, 5, 16)
            .entry(Material.GOLDEN_CARROT, 4, 10, 14)
            .entry(Material.CARROT, 12, 32, 30)
            .entryCustom("papa_explosiva", 6, 16, 26)
            .entry(Material.EXPERIENCE_BOTTLE, 3, 8, 18)
            .entry(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 8)
            .entry(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 6)
            .entry(Material.NETHERITE_SCRAP, 1, 2, 7)
            .entry(Material.ANCIENT_DEBRIS, 1, 2, 5)
            .entry(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 1, 5)
            .entry(Material.DIAMOND_BLOCK, 1, 1, 4)
            .entry(Material.NETHERITE_INGOT, 1, 1, 2)
            .entry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 3)
            .build();

    private final Variant variant;
    private final StructureDef.Rotation rotation;

    public ForjaAncestral() {
        this(Variant.CLASSIC, StructureDef.Rotation.ROT_0);
    }

    public ForjaAncestral(StructureDef.Rotation rotation) {
        this(Variant.CLASSIC, rotation);
    }

    public ForjaAncestral(Variant variant, StructureDef.Rotation rotation) {
        this.variant = variant;
        this.rotation = rotation;
        this.WALL = variant.wall;
        this.PILLAR = variant.pillar;
        this.FLOOR = variant.floor;
        this.BORDER = variant.border;
        this.ACCENT = variant.accent;
        this.BAND = variant.band;
        this.RAIL = variant.rail;
    }

    @Override
    public StructureDef build() {
        String id = variant == Variant.CLASSIC
                ? "forja_ancestral"
                : "forja_ancestral_" + variant.name().toLowerCase();

        StructureDef.Builder b = new StructureDef.Builder(id)
                .spawnChance(0.00004)
                .minDay(15)
                .notifyOnGenerate("&eHas sentido el calor de una &6&lForja Ancestral &ecercana...")
                .rotation(rotation)
                .biomes(
                        Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.MEADOW,
                        Biome.FOREST, Biome.BIRCH_FOREST, Biome.DARK_FOREST,
                        Biome.TAIGA, Biome.SNOWY_TAIGA, Biome.OLD_GROWTH_PINE_TAIGA,
                        Biome.SAVANNA, Biome.DESERT, Biome.BADLANDS,
                        Biome.WINDSWEPT_HILLS, Biome.STONY_SHORE
                );

        plaza(b);
        groundFloor(b);
        secondFloor(b);
        terrace(b);
        cornerTowers(b);
        porch(b);
        stairShaft(b);
        boss(b);

        return b.build();
    }

    // -------------------------------------------------------------------------
    // Exterior
    // -------------------------------------------------------------------------

    private void plaza(StructureDef.Builder b) {
        Material court = Material.BLACKSTONE;
        b.fill(-2, 0, -2, 16, 0, -1, court);
        b.fill(-2, 0, 15, 16, 0, 16, court);
        b.fill(-2, 0, 0, -1, 0, 14, court);
        b.fill(15, 0, 0, 16, 0, 14, court);

        b.fill(6, 0, 15, 8, 0, 19, BORDER);

        brazier(b, -1, -1);
        brazier(b, 15, -1);
        brazier(b, -1, 15);
        brazier(b, 15, 15);

        b.block(2, 1, 16, Material.COAL_BLOCK);
        b.block(3, 1, 16, Material.COAL_BLOCK);
        b.block(2, 2, 16, Material.COAL_BLOCK);
        b.block(12, 1, 16, Material.CHIPPED_ANVIL);
        b.block(11, 1, -1, Material.DAMAGED_ANVIL);
    }

    private void brazier(StructureDef.Builder b, int x, int z) {
        b.column(x, 1, 2, z, PILLAR);
        b.block(x, 3, z, Material.LAVA_CAULDRON);
    }

    // -------------------------------------------------------------------------
    // Planta baja: la forja
    // -------------------------------------------------------------------------

    private void groundFloor(StructureDef.Builder b) {
        b.fill(0, 0, 0, 14, 0, 14, FLOOR);
        b.fill(7, 0, 1, 7, 0, 13, ACCENT);
        b.fill(1, 0, 7, 13, 0, 7, ACCENT);

        ringWalls(b, 0, 0, 14, 14, 1, 5, WALL);
        b.layer(3, 0, 14, 0, 0, BAND);
        b.layer(3, 0, 14, 14, 14, BAND);
        b.layer(3, 0, 0, 0, 14, BAND);
        b.layer(3, 14, 14, 0, 14, BAND);

        window(b, 0, 4, 2);
        window(b, 0, 10, 2);
        window(b, 14, 4, 2);
        window(b, 14, 10, 2);
        window(b, 4, 0, 2);
        window(b, 10, 0, 2);

        b.fill(6, 1, 14, 8, 4, 14, Material.AIR);

        forge(b);

        cagedSpawner(b, 2, 2, EntityType.BLAZE);
        cagedSpawner(b, 12, 2, EntityType.WITHER_SKELETON);

        b.chest(2, 1, 12, LOOT_FORJA);
        b.chest(12, 1, 12, LOOT_FORJA);

        b.fill(0, 6, 0, 14, 6, 14, FLOOR);

        hangingLantern(b, 4, 5, 4);
        hangingLantern(b, 10, 5, 4);
        hangingLantern(b, 4, 5, 10);
        hangingLantern(b, 10, 5, 10);
    }

    private void forge(StructureDef.Builder b) {
        b.fill(6, 1, 2, 8, 1, 4, PILLAR);
        b.block(7, 1, 3, Material.LAVA);

        BlockData blastFurnace = Material.BLAST_FURNACE.createBlockData();
        ((Directional) blastFurnace).setFacing(BlockFace.SOUTH);
        BlockData smoker = Material.SMOKER.createBlockData();
        ((Directional) smoker).setFacing(BlockFace.SOUTH);
        BlockData anvil = Material.ANVIL.createBlockData();
        ((Directional) anvil).setFacing(BlockFace.EAST);

        b.block(4, 1, 1, Material.GRINDSTONE);
        b.blockData(5, 1, 1, blastFurnace);
        b.blockData(6, 1, 1, anvil);
        b.block(7, 1, 1, Material.SMITHING_TABLE);
        b.blockData(8, 1, 1, anvil);
        b.blockData(9, 1, 1, blastFurnace);
        b.blockData(10, 1, 1, smoker);

        b.block(5, 2, 1, Material.LANTERN);
        b.block(9, 2, 1, Material.LANTERN);
    }

    private void cagedSpawner(StructureDef.Builder b, int x, int z, EntityType type) {
        b.spawner(x, 1, z, type);
        b.block(x - 1, 1, z, Material.COBWEB);
        b.block(x + 1, 1, z, Material.COBWEB);
        b.block(x, 1, z - 1, Material.COBWEB);
        b.block(x, 1, z + 1, Material.COBWEB);
        b.block(x, 2, z, Material.IRON_BARS);
    }

    // -------------------------------------------------------------------------
    // Segundo piso: armería retranqueada + balcón perimetral
    // -------------------------------------------------------------------------

    private void secondFloor(StructureDef.Builder b) {
        ringWalls(b, 0, 0, 14, 14, 7, 7, RAIL);

        ringWalls(b, 2, 2, 12, 12, 7, 10, WALL);
        b.fill(5, 7, 12, 9, 9, 12, Material.AIR);

        window(b, 2, 5, 8);
        window(b, 2, 9, 8);
        window(b, 12, 5, 8);
        window(b, 12, 9, 8);
        windowTall(b, 7, 2, 8);

        b.chest(3, 7, 11, LOOT_FORJA);
        b.chest(11, 7, 11, LOOT_FORJA);

        hangingLantern(b, 4, 10, 4);
        hangingLantern(b, 10, 10, 4);
        hangingLantern(b, 4, 10, 10);
        hangingLantern(b, 10, 10, 10);

        b.fill(2, 11, 2, 12, 11, 12, FLOOR);
    }

    // -------------------------------------------------------------------------
    // Terraza: el boss
    // -------------------------------------------------------------------------

    private void terrace(StructureDef.Builder b) {
        b.fill(6, 11, 6, 8, 11, 8, ACCENT);
        b.block(7, 11, 7, Material.NETHERITE_BLOCK);

        ringWalls(b, 2, 2, 12, 12, 12, 12, WALL);
        crenellations(b, 2, 2, 12, 12, 13, WALL);

        b.chest(7, 12, 4, LOOT_FORJA);
    }

    // -------------------------------------------------------------------------
    // Torres de esquina
    // -------------------------------------------------------------------------

    private void cornerTowers(StructureDef.Builder b) {
        tower(b, 0, 0);
        tower(b, 13, 0);
        tower(b, 0, 13);
        tower(b, 13, 13);
    }

    private void tower(StructureDef.Builder b, int x, int z) {
        b.fill(x, 1, z, x + 1, 13, z + 1, PILLAR);
        switch (variant.towerCap) {
            case 1 -> {
                b.fill(x, 14, z, x + 1, 16, z + 1, PILLAR);
                b.fill(x, 17, z, x + 1, 17, z + 1, BAND);
                b.block(x, 18, z, Material.SEA_LANTERN);
                b.block(x + 1, 18, z + 1, Material.SEA_LANTERN);
            }
            case 2 -> {
                b.fill(x, 14, z, x + 1, 14, z + 1, BAND);
                b.block(x, 15, z, Material.SOUL_CAMPFIRE);
                b.block(x + 1, 15, z + 1, Material.SOUL_CAMPFIRE);
            }
            default -> {
                b.fill(x, 14, z, x + 1, 14, z + 1, BAND);
                b.block(x, 15, z, Material.LANTERN);
                b.block(x + 1, 15, z + 1, Material.LANTERN);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Pórtico de entrada
    // -------------------------------------------------------------------------

    private void porch(StructureDef.Builder b) {
        b.column(5, 1, 4, 15, PILLAR);
        b.column(9, 1, 4, 15, PILLAR);
        b.fill(5, 5, 15, 9, 5, 16, WALL);
        b.block(5, 4, 16, Material.LANTERN);
        b.block(9, 4, 16, Material.LANTERN);
    }

    // -------------------------------------------------------------------------
    // Hueco de escalera (escaleras de mano que cruzan todos los pisos)
    // -------------------------------------------------------------------------

    private void stairShaft(StructureDef.Builder b) {
        b.column(2, 1, 11, 3, PILLAR);

        BlockData ladder = Material.LADDER.createBlockData();
        ((Directional) ladder).setFacing(BlockFace.EAST);
        for (int y = 1; y <= 11; y++) {
            b.blockData(3, y, 3, ladder);
        }
    }

    // -------------------------------------------------------------------------
    // Boss en la terraza
    // -------------------------------------------------------------------------

    private void boss(StructureDef.Builder b) {
        b.entity(7, 12, 7, EntityType.WITHER_SKELETON, entity -> {
            Location location = entity.getLocation();
            entity.remove();
            new GuardianForja(pendulum.getInstance(), location).build();
        });
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void ringWalls(StructureDef.Builder b, int x0, int z0, int x1, int z1,
                           int y0, int y1, Material mat) {
        b.fill(x0, y0, z0, x1, y1, z0, mat);
        b.fill(x0, y0, z1, x1, y1, z1, mat);
        b.fill(x0, y0, z0, x0, y1, z1, mat);
        b.fill(x1, y0, z0, x1, y1, z1, mat);
    }

    private void crenellations(StructureDef.Builder b, int x0, int z0, int x1, int z1,
                               int y, Material mat) {
        for (int x = x0; x <= x1; x++) {
            if (((x + z0) & 1) == 0) b.block(x, y, z0, mat);
            if (((x + z1) & 1) == 0) b.block(x, y, z1, mat);
        }
        for (int z = z0; z <= z1; z++) {
            if (((x0 + z) & 1) == 0) b.block(x0, y, z, mat);
            if (((x1 + z) & 1) == 0) b.block(x1, y, z, mat);
        }
    }

    private void window(StructureDef.Builder b, int x, int z, int y) {
        b.block(x, y, z, Material.IRON_BARS);
        b.block(x, y + 1, z, Material.IRON_BARS);
    }

    private void windowTall(StructureDef.Builder b, int x, int z, int y) {
        b.block(x, y, z, Material.IRON_BARS);
        b.block(x, y + 1, z, Material.IRON_BARS);
    }

    private void hangingLantern(StructureDef.Builder b, int x, int y, int z) {
        BlockData hanging = Material.LANTERN.createBlockData();
        ((Lantern) hanging).setHanging(true);
        b.blockData(x, y, z, hanging);
    }
}

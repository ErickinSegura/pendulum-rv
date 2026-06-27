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

    private static final Material WALL = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material PILLAR = Material.POLISHED_BLACKSTONE;
    private static final Material FLOOR = Material.POLISHED_BLACKSTONE_BRICKS;
    private static final Material BORDER = Material.POLISHED_BLACKSTONE;
    private static final Material ACCENT = Material.GILDED_BLACKSTONE;
    private static final Material ROOF = Material.DEEPSLATE_TILES;

    private static final LootTable LOOT_FORJA = new LootTable.Builder("forja_ancestral_cofre")
            .rolls(5, 9)
            .entry(Material.IRON_INGOT, 3, 8, 40)
            .entry(Material.GOLD_INGOT, 2, 6, 30)
            .entry(Material.COAL_BLOCK, 1, 3, 25)
            .entry(Material.IRON_BLOCK, 1, 2, 16)
            .entry(Material.OBSIDIAN, 2, 5, 14)
            .entry(Material.DIAMOND, 1, 4, 18)
            .entry(Material.ENCHANTED_BOOK, 1, 1, 12)
            .entry(Material.EXPERIENCE_BOTTLE, 2, 6, 18)
            .entry(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 8)
            .entry(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 1, 1, 6)
            .entry(Material.NETHERITE_SCRAP, 1, 2, 6)
            .entry(Material.LAVA_BUCKET, 1, 1, 6)
            .entry(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 1, 1, 5)
            .entry(Material.ANCIENT_DEBRIS, 1, 2, 4)
            .entry(Material.DIAMOND_BLOCK, 1, 1, 3)
            .entry(Material.ENCHANTED_GOLDEN_APPLE, 1, 1, 2)
            .build();

    private final StructureDef.Rotation rotation;

    public ForjaAncestral() {
        this(StructureDef.Rotation.ROT_0);
    }

    public ForjaAncestral(StructureDef.Rotation rotation) {
        this.rotation = rotation;
    }

    @Override
    public StructureDef build() {
        StructureDef.Builder b = new StructureDef.Builder("forja_ancestral")
                .spawnChance(0.0007)
                .rotation(rotation)
                .biomes(
                        Biome.PLAINS, Biome.SUNFLOWER_PLAINS, Biome.MEADOW,
                        Biome.FOREST, Biome.BIRCH_FOREST, Biome.DARK_FOREST,
                        Biome.TAIGA, Biome.SNOWY_TAIGA, Biome.OLD_GROWTH_PINE_TAIGA,
                        Biome.SAVANNA, Biome.DESERT, Biome.BADLANDS,
                        Biome.WINDSWEPT_HILLS, Biome.STONY_SHORE
                );

        floor(b);
        walls(b);
        forge(b);
        lighting(b);
        roof(b);
        loot(b);
        boss(b);

        return b.build();
    }

    private void floor(StructureDef.Builder b) {
        b.fill(0, 0, 0, 16, 0, 16, FLOOR);
        b.layer(0, 0, 16, 0, 0, BORDER);
        b.layer(0, 0, 16, 16, 16, BORDER);
        b.layer(0, 0, 0, 0, 16, BORDER);
        b.layer(0, 16, 16, 0, 16, BORDER);
        b.fill(7, 0, 7, 9, 0, 9, ACCENT);
        b.fill(8, 0, 10, 8, 0, 15, ACCENT);
        b.block(8, 0, 8, Material.NETHERITE_BLOCK);
    }

    private void walls(StructureDef.Builder b) {
        b.fill(0, 1, 0, 16, 6, 0, WALL);
        b.fill(0, 1, 16, 16, 6, 16, WALL);
        b.fill(0, 1, 0, 0, 6, 16, WALL);
        b.fill(16, 1, 0, 16, 6, 16, WALL);

        int[][] pillars = {
                {0, 0}, {16, 0}, {0, 16}, {16, 16},
                {8, 0}, {8, 16}, {0, 8}, {16, 8}
        };
        for (int[] p : pillars) {
            b.column(p[0], 1, 6, p[1], PILLAR);
            b.block(p[0], 6, p[1], Material.CHISELED_POLISHED_BLACKSTONE);
        }

        b.fill(7, 1, 16, 9, 4, 16, Material.AIR);

        int[] windowX = {4, 12};
        int[] windowZ = {4, 12};
        for (int z : windowZ) {
            b.block(0, 2, z, Material.IRON_BARS);
            b.block(0, 3, z, Material.IRON_BARS);
            b.block(16, 2, z, Material.IRON_BARS);
            b.block(16, 3, z, Material.IRON_BARS);
        }
        for (int x : windowX) {
            b.block(x, 2, 0, Material.IRON_BARS);
            b.block(x, 3, 0, Material.IRON_BARS);
            b.block(x, 2, 16, Material.IRON_BARS);
            b.block(x, 3, 16, Material.IRON_BARS);
        }
    }

    private void forge(StructureDef.Builder b) {
        b.fill(7, 1, 2, 9, 1, 4, PILLAR);
        b.block(8, 1, 3, Material.LAVA);

        BlockData blastFurnace = Material.BLAST_FURNACE.createBlockData();
        ((Directional) blastFurnace).setFacing(BlockFace.SOUTH);
        BlockData smoker = Material.SMOKER.createBlockData();
        ((Directional) smoker).setFacing(BlockFace.SOUTH);
        BlockData anvil = Material.ANVIL.createBlockData();
        ((Directional) anvil).setFacing(BlockFace.EAST);

        b.block(5, 1, 1, Material.GRINDSTONE);
        b.blockData(6, 1, 1, blastFurnace);
        b.blockData(7, 1, 1, anvil);
        b.block(8, 1, 1, Material.SMITHING_TABLE);
        b.blockData(9, 1, 1, anvil);
        b.blockData(10, 1, 1, blastFurnace);
        b.blockData(11, 1, 1, smoker);

        b.block(6, 2, 1, Material.LANTERN);
        b.block(10, 2, 1, Material.LANTERN);
        b.block(8, 2, 1, Material.IRON_CHAIN);
    }

    private void lighting(StructureDef.Builder b) {
        BlockData hangingLantern = Material.LANTERN.createBlockData();
        ((Lantern) hangingLantern).setHanging(true);

        int[][] lamps = {{4, 4}, {12, 4}, {4, 12}, {12, 12}};
        for (int[] l : lamps) {
            b.block(l[0], 6, l[1], Material.IRON_CHAIN);
            b.blockData(l[0], 5, l[1], hangingLantern);
        }
    }

    private void roof(StructureDef.Builder b) {
        b.fill(0, 7, 0, 16, 7, 16, ROOF);

        b.block(8, 7, 3, Material.AIR);
        b.fill(7, 8, 2, 9, 9, 4, WALL);
        b.column(8, 8, 9, 3, Material.AIR);
        b.block(8, 9, 3, Material.IRON_BARS);
    }

    private void loot(StructureDef.Builder b) {
        b.chest(2, 1, 2, LOOT_FORJA);
        b.chest(14, 1, 2, LOOT_FORJA);
        b.chest(2, 1, 14, LOOT_FORJA);
        b.chest(14, 1, 14, LOOT_FORJA);
    }

    private void boss(StructureDef.Builder b) {
        b.entity(8, 1, 8, EntityType.WITHER_SKELETON, entity -> {
            Location location = entity.getLocation();
            entity.remove();
            new GuardianForja(pendulum.getInstance(), location).build();
        });
    }
}

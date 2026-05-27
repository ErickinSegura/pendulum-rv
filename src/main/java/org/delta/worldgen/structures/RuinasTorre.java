package org.delta.worldgen.structures;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.delta.worldgen.StructureDef;
import org.delta.worldgen.StructureTemplate;

public class RuinasTorre extends StructureTemplate {

    @Override
    public StructureDef build() {
        return new StructureDef.Builder("ruinas_torre")
                .spawnChance(0.9)
                .biomes(
                        Biome.PLAINS,
                        Biome.SUNFLOWER_PLAINS,
                        Biome.FOREST,
                        Biome.BIRCH_FOREST,
                        Biome.OLD_GROWTH_BIRCH_FOREST
                )
                .layer(0, 0, 4, 0, 4, Material.COBBLESTONE)
                .layer(0, 1, 3, 1, 3, Material.STONE_BRICKS)
                .column(0, 1, 5, 0, Material.STONE_BRICKS)
                .column(4, 1, 3, 0, Material.CRACKED_STONE_BRICKS)
                .column(0, 1, 3, 4, Material.CRACKED_STONE_BRICKS)
                .column(4, 1, 4, 4, Material.STONE_BRICKS)
                .block(1, 1, 0, Material.STONE_BRICK_WALL)
                .block(2, 1, 0, Material.STONE_BRICK_WALL)
                .block(3, 1, 0, Material.STONE_BRICK_WALL)
                .block(1, 2, 0, Material.STONE_BRICK_WALL)
                .block(2, 2, 0, Material.STONE_BRICK_WALL)
                .block(0, 1, 1, Material.STONE_BRICK_WALL)
                .block(0, 1, 2, Material.STONE_BRICK_WALL)
                .block(0, 1, 3, Material.STONE_BRICK_WALL)
                .block(5, 0, 2, Material.COBBLESTONE)
                .block(-1, 0, 1, Material.MOSSY_COBBLESTONE)
                .block(2, 0, 5, Material.MOSSY_COBBLESTONE)
                .build();
    }
}
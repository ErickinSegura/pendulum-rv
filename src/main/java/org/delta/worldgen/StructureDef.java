package org.delta.worldgen;

import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.*;

public class StructureDef {

    public record BlockEntry(int relX, int relY, int relZ, Material material) {}

    private final String id;
    private final List<BlockEntry> blocks;
    private final Set<Biome> allowedBiomes;
    private final double spawnChance;
    private final int maxRelX;
    private final int maxRelZ;

    private StructureDef(String id, List<BlockEntry> blocks, Set<Biome> allowedBiomes, double spawnChance) {
        this.id             = id;
        this.blocks         = Collections.unmodifiableList(new ArrayList<>(blocks));
        this.allowedBiomes  = Collections.unmodifiableSet(new HashSet<>(allowedBiomes));
        this.spawnChance    = spawnChance;
        this.maxRelX        = blocks.stream().mapToInt(BlockEntry::relX).max().orElse(0);
        this.maxRelZ        = blocks.stream().mapToInt(BlockEntry::relZ).max().orElse(0);
    }

    public String           getId()            { return id; }
    public List<BlockEntry> getBlocks()        { return blocks; }
    public Set<Biome>       getAllowedBiomes() { return allowedBiomes; }
    public double           getSpawnChance()   { return spawnChance; }
    public int              getMaxRelX()       { return maxRelX; }
    public int              getMaxRelZ()       { return maxRelZ; }

    public boolean allowedIn(Biome biome) {
        return allowedBiomes.isEmpty() || allowedBiomes.contains(biome);
    }

    public static class Builder {
        private final String          id;
        private final List<BlockEntry> blocks        = new ArrayList<>();
        private final Set<Biome>      allowedBiomes = new HashSet<>();
        private double spawnChance = 0.015;

        public Builder(String id) { this.id = id; }

        public Builder biomes(Biome... biomes) {
            allowedBiomes.addAll(Arrays.asList(biomes));
            return this;
        }

        public Builder spawnChance(double chance) {
            this.spawnChance = chance;
            return this;
        }

        public Builder block(int relX, int relY, int relZ, Material material) {
            blocks.add(new BlockEntry(relX, relY, relZ, material));
            return this;
        }

        public Builder layer(int relY, int fromX, int toX, int fromZ, int toZ, Material material) {
            for (int x = fromX; x <= toX; x++)
                for (int z = fromZ; z <= toZ; z++)
                    blocks.add(new BlockEntry(x, relY, z, material));
            return this;
        }

        public Builder column(int relX, int fromY, int toY, int relZ, Material material) {
            for (int y = fromY; y <= toY; y++)
                blocks.add(new BlockEntry(relX, y, relZ, material));
            return this;
        }

        public StructureDef build() {
            return new StructureDef(id, blocks, allowedBiomes, spawnChance);
        }
    }
}
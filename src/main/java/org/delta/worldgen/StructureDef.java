package org.delta.worldgen;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.function.Consumer;

public class StructureDef {

    public enum SpawnMode { GROUND, AIR }

    public enum Rotation { ROT_0, ROT_90, ROT_180, ROT_270 }

    public record BlockEntry(int relX, int relY, int relZ, Material material) {}

    public record EntityEntry(int relX, int relY, int relZ,
                              EntityType type, Consumer<Entity> customizer) {}


    private final String               id;
    private final List<BlockEntry>     blocks;
    private final List<EntityEntry>    entities;
    private final Map<Long, LootTable> chestLoot;
    private final Set<Biome>           allowedBiomes;
    private final double               spawnChance;
    private final int                  maxRelX;
    private final int                  maxRelZ;
    private final SpawnMode            spawnMode;
    private final int                  minClearance;
    private final int                  minAirY;
    private final int                  maxAirY;
    private final Rotation             rotation;

    private StructureDef(Builder b) {
        this.id            = b.id;
        this.allowedBiomes = Collections.unmodifiableSet(new HashSet<>(b.allowedBiomes));
        this.spawnChance   = b.spawnChance;
        this.spawnMode     = b.spawnMode;
        this.minClearance  = b.minClearance;
        this.minAirY       = b.minAirY;
        this.maxAirY       = b.maxAirY;
        this.rotation      = b.rotation;

        List<BlockEntry>     rotatedBlocks   = rotateBlocks(b.blocks, b.rotation);
        List<EntityEntry> rotatedEntities = rotateEntities(b.entities, b.blocks, b.rotation);
        Map<Long, LootTable> rotatedLoot     = rotateLoot(b.chestLoot, b.blocks, b.rotation);

        this.blocks    = Collections.unmodifiableList(rotatedBlocks);
        this.entities  = Collections.unmodifiableList(rotatedEntities);
        this.chestLoot = Collections.unmodifiableMap(rotatedLoot);

        this.maxRelX = this.blocks.stream().mapToInt(BlockEntry::relX).max().orElse(0);
        this.maxRelZ = this.blocks.stream().mapToInt(BlockEntry::relZ).max().orElse(0);
    }

    private static int[] rotatePoint(int relX, int relZ, Rotation rot) {
        return switch (rot) {
            case ROT_0   -> new int[]{  relX,  relZ };
            case ROT_90  -> new int[]{ -relZ,  relX };
            case ROT_180 -> new int[]{ -relX, -relZ };
            case ROT_270 -> new int[]{  relZ, -relX };
        };
    }

    public static List<StructureDef> allRotations(Builder b) {
        List<StructureDef> variants = new ArrayList<>(4);
        for (Rotation rot : Rotation.values()) {
            b.rotation(rot);
            variants.add(b.build());
        }
        return variants;
    }



    private static int[] normOffset(List<BlockEntry> blocks, Rotation rot) {
        int minX = Integer.MAX_VALUE;
        int minZ = Integer.MAX_VALUE;
        for (BlockEntry e : blocks) {
            int[] r = rotatePoint(e.relX(), e.relZ(), rot);
            if (r[0] < minX) minX = r[0];
            if (r[1] < minZ) minZ = r[1];
        }
        return new int[]{ Math.min(minX, 0), Math.min(minZ, 0) };
    }

    private static List<BlockEntry> rotateBlocks(List<BlockEntry> original, Rotation rot) {
        if (rot == Rotation.ROT_0) return new ArrayList<>(original);
        int[] norm = normOffset(original, rot);
        List<BlockEntry> result = new ArrayList<>(original.size());
        for (BlockEntry e : original) {
            int[] r = rotatePoint(e.relX(), e.relZ(), rot);
            result.add(new BlockEntry(r[0] - norm[0], e.relY(), r[1] - norm[1], e.material()));
        }
        return result;
    }


    private static List<EntityEntry> rotateEntities(List<EntityEntry> original,
                                                    List<BlockEntry> originalBlocks,
                                                    Rotation rot) {
        if (rot == Rotation.ROT_0) return new ArrayList<>(original);
        int[] norm = normOffset(originalBlocks, rot);
        List<EntityEntry> result = new ArrayList<>(original.size());
        for (EntityEntry e : original) {
            int[] r = rotatePoint(e.relX(), e.relZ(), rot);
            result.add(new EntityEntry(r[0] - norm[0], e.relY(), r[1] - norm[1],
                    e.type(), e.customizer()));
        }
        return result;
    }

    private static Map<Long, LootTable> rotateLoot(Map<Long, LootTable> originalLoot,
                                                   List<BlockEntry> originalBlocks,
                                                   Rotation rot) {
        if (rot == Rotation.ROT_0) return new HashMap<>(originalLoot);
        int[] norm = normOffset(originalBlocks, rot);
        Map<Long, LootTable> result = new HashMap<>();

        for (BlockEntry e : originalBlocks) {
            long oldKey = posKey(e.relX(), e.relY(), e.relZ());
            LootTable loot = originalLoot.get(oldKey);
            if (loot == null) continue;
            int[] r = rotatePoint(e.relX(), e.relZ(), rot);
            long newKey = posKey(r[0] - norm[0], e.relY(), r[1] - norm[1]);
            result.put(newKey, loot);
        }
        return result;
    }


    public String            getId()           { return id; }
    public List<BlockEntry>  getBlocks()       { return blocks; }
    public List<EntityEntry> getEntities()     { return entities; }
    public Set<Biome>        getAllowedBiomes() { return allowedBiomes; }
    public double            getSpawnChance()  { return spawnChance; }
    public int               getMaxRelX()      { return maxRelX; }
    public int               getMaxRelZ()      { return maxRelZ; }
    public SpawnMode         getSpawnMode()    { return spawnMode; }
    public int               getMinClearance() { return minClearance; }
    public int               getMinAirY()      { return minAirY; }
    public int               getMaxAirY()      { return maxAirY; }
    public Rotation          getRotation()     { return rotation; }

    public LootTable getChestLoot(int relX, int relY, int relZ) {
        return chestLoot.get(posKey(relX, relY, relZ));
    }

    public boolean allowedIn(Biome biome) {
        return allowedBiomes.isEmpty() || allowedBiomes.contains(biome);
    }

    static long posKey(int x, int y, int z) {
        return ((long)(x & 0xFFFF) << 32) | ((long)(y & 0xFFFF) << 16) | (z & 0xFFFF);
    }


    public static class Builder {
        private final String               id;
        private final List<BlockEntry>     blocks        = new ArrayList<>();
        private final List<EntityEntry>    entities      = new ArrayList<>();
        private final Map<Long, LootTable> chestLoot     = new HashMap<>();
        private final Set<Biome>           allowedBiomes = new HashSet<>();
        private double    spawnChance  = 0.015;
        private SpawnMode spawnMode    = SpawnMode.GROUND;
        private int       minClearance = 10;
        private int       minAirY      = 80;
        private int       maxAirY      = 180;
        private Rotation  rotation     = Rotation.ROT_0;

        public Builder(String id) { this.id = id; }

        public Builder biomes(Biome... biomes) {
            allowedBiomes.addAll(Arrays.asList(biomes));
            return this;
        }

        public Builder spawnChance(double chance) {
            this.spawnChance = chance;
            return this;
        }

        public Builder rotation(Rotation rotation) {
            this.rotation = rotation;
            return this;
        }

        public Builder airSpawn() {
            this.spawnMode = SpawnMode.AIR;
            return this;
        }

        public Builder airSpawn(int minClearance, int minAirY, int maxAirY) {
            this.spawnMode    = SpawnMode.AIR;
            this.minClearance = minClearance;
            this.minAirY      = minAirY;
            this.maxAirY      = maxAirY;
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

        public Builder chest(int relX, int relY, int relZ, LootTable lootTable) {
            return chest(relX, relY, relZ, Material.CHEST, lootTable);
        }

        public Builder chest(int relX, int relY, int relZ, Material chestType, LootTable lootTable) {
            blocks.add(new BlockEntry(relX, relY, relZ, chestType));
            chestLoot.put(posKey(relX, relY, relZ), lootTable);
            return this;
        }

        public Builder entity(int relX, int relY, int relZ, EntityType type) {
            return entity(relX, relY, relZ, type, null);
        }

        public Builder entity(int relX, int relY, int relZ, EntityType type,
                              Consumer<Entity> customizer) {
            entities.add(new EntityEntry(relX, relY, relZ, type, customizer));
            return this;
        }

        public Builder fill(int fromX, int fromY, int fromZ,
                            int toX,   int toY,   int toZ, Material material) {
            for (int x = fromX; x <= toX; x++)
                for (int y = fromY; y <= toY; y++)
                    for (int z = fromZ; z <= toZ; z++)
                        blocks.add(new BlockEntry(x, y, z, material));
            return this;
        }

        public StructureDef build() {
            if (blocks.isEmpty()) throw new IllegalStateException("Estructura sin bloques: " + id);
            return new StructureDef(this);
        }
    }
}
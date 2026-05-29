package org.delta.worldgen;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.function.Consumer;

public class StructureDef {

    // ─── Entries ────────────────────────────────────────────────────────────────

    /** Un bloque de la estructura con su posición relativa. */
    public record BlockEntry(int relX, int relY, int relZ, Material material) {}

    /**
     * Una entidad declarada en la estructura.
     * El customizer es opcional (puede ser null); se ejecuta justo después del spawn.
     */
    public record EntityEntry(int relX, int relY, int relZ,
                              EntityType type, Consumer<Entity> customizer) {}

    // ─── Campos ─────────────────────────────────────────────────────────────────

    private final String              id;
    private final List<BlockEntry>    blocks;
    private final List<EntityEntry>   entities;
    private final Map<Long, LootTable> chestLoot;   // key = posKey(relX,relY,relZ)
    private final Set<Biome>          allowedBiomes;
    private final double              spawnChance;
    private final int                 maxRelX;
    private final int                 maxRelZ;

    private StructureDef(String id,
                         List<BlockEntry> blocks,
                         List<EntityEntry> entities,
                         Map<Long, LootTable> chestLoot,
                         Set<Biome> allowedBiomes,
                         double spawnChance) {
        this.id           = id;
        this.blocks       = Collections.unmodifiableList(new ArrayList<>(blocks));
        this.entities     = Collections.unmodifiableList(new ArrayList<>(entities));
        this.chestLoot    = Collections.unmodifiableMap(new HashMap<>(chestLoot));
        this.allowedBiomes = Collections.unmodifiableSet(new HashSet<>(allowedBiomes));
        this.spawnChance  = spawnChance;
        this.maxRelX      = blocks.stream().mapToInt(BlockEntry::relX).max().orElse(0);
        this.maxRelZ      = blocks.stream().mapToInt(BlockEntry::relZ).max().orElse(0);
    }

    // ─── Getters ────────────────────────────────────────────────────────────────

    public String             getId()            { return id; }
    public List<BlockEntry>   getBlocks()        { return blocks; }
    public List<EntityEntry>  getEntities()      { return entities; }
    public Set<Biome>         getAllowedBiomes()  { return allowedBiomes; }
    public double             getSpawnChance()   { return spawnChance; }
    public int                getMaxRelX()       { return maxRelX; }
    public int                getMaxRelZ()       { return maxRelZ; }

    /** Devuelve la LootTable para un cofre en esa posición relativa, o null si no tiene. */
    public LootTable getChestLoot(int relX, int relY, int relZ) {
        return chestLoot.get(posKey(relX, relY, relZ));
    }

    public boolean allowedIn(Biome biome) {
        return allowedBiomes.isEmpty() || allowedBiomes.contains(biome);
    }

    // ─── Utilidad ───────────────────────────────────────────────────────────────

    static long posKey(int x, int y, int z) {
        // Empaqueta coordenadas relativas (todas < 64) en un long
        return ((long) (x & 0xFFFF) << 32) | ((long) (y & 0xFFFF) << 16) | (z & 0xFFFF);
    }

    // ─── Builder ────────────────────────────────────────────────────────────────

    public static class Builder {
        private final String              id;
        private final List<BlockEntry>    blocks        = new ArrayList<>();
        private final List<EntityEntry>   entities      = new ArrayList<>();
        private final Map<Long, LootTable> chestLoot    = new HashMap<>();
        private final Set<Biome>          allowedBiomes = new HashSet<>();
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

        // ── Bloques ─────────────────────────────────────────────────────────────

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

        // ── Cofres con loot ─────────────────────────────────────────────────────

        /**
         * Coloca un cofre en la posición dada y asocia una LootTable.
         * El material puede ser CHEST o TRAPPED_CHEST.
         */
        public Builder chest(int relX, int relY, int relZ, LootTable lootTable) {
            return chest(relX, relY, relZ, Material.CHEST, lootTable);
        }

        public Builder chest(int relX, int relY, int relZ, Material chestType, LootTable lootTable) {
            if (chestType != Material.CHEST && chestType != Material.TRAPPED_CHEST
                    && chestType != Material.BARREL && chestType != Material.SHULKER_BOX) {
                throw new IllegalArgumentException("Material no es un contenedor válido: " + chestType);
            }
            blocks.add(new BlockEntry(relX, relY, relZ, chestType));
            chestLoot.put(posKey(relX, relY, relZ), lootTable);
            return this;
        }

        // ── Entidades ───────────────────────────────────────────────────────────

        /** Programa el spawn de una entidad en la posición relativa dada. */
        public Builder entity(int relX, int relY, int relZ, EntityType type) {
            return entity(relX, relY, relZ, type, null);
        }

        /**
         * Programa el spawn de una entidad con un customizer.
         * El customizer recibe la entidad ya spawneada para setear nombre,
         * equipo, atributos, etc.
         *
         * Ejemplo:
         * <pre>
         *   .entity(2, 1, 2, EntityType.ZOMBIE, e -> {
         *       Zombie z = (Zombie) e;
         *       z.customName(Component.text("Guardián"));
         *       z.setCustomNameVisible(true);
         *   })
         * </pre>
         */
        public Builder entity(int relX, int relY, int relZ, EntityType type,
                              Consumer<Entity> customizer) {
            entities.add(new EntityEntry(relX, relY, relZ, type, customizer));
            return this;
        }

        public StructureDef build() {
            return new StructureDef(id, blocks, entities, chestLoot, allowedBiomes, spawnChance);
        }
    }
}
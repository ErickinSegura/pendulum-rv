package org.delta.worldgen;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.bukkit.plugin.Plugin;
import org.delta.listeners.worldgen.PendingEntitySpawner;
import org.delta.worldgen.structures.ForjaAncestral;
import org.delta.worldgen.structures.RuinasTorre;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructurePopulator extends BlockPopulator {

    private static final int BORDER_PADDING = 3;
    private static final int MIN_Y          = 60;

    private static final Set<Material> GROUND_MATERIALS = EnumSet.of(
            Material.GRASS_BLOCK, Material.DIRT, Material.COARSE_DIRT,
            Material.PODZOL, Material.ROOTED_DIRT, Material.STONE,
            Material.DEEPSLATE, Material.ANDESITE, Material.DIORITE,
            Material.GRANITE, Material.SAND, Material.RED_SAND,
            Material.GRAVEL, Material.SANDSTONE, Material.RED_SANDSTONE,
            Material.SNOW_BLOCK, Material.ICE, Material.PACKED_ICE,
            Material.MYCELIUM, Material.MUD, Material.MUDDY_MANGROVE_ROOTS
    );

    private static final Set<Material> VEGETATION = EnumSet.of(
            Material.OAK_LOG, Material.BIRCH_LOG, Material.SPRUCE_LOG,
            Material.JUNGLE_LOG, Material.ACACIA_LOG, Material.DARK_OAK_LOG,
            Material.MANGROVE_LOG, Material.CHERRY_LOG,
            Material.OAK_LEAVES, Material.BIRCH_LEAVES, Material.SPRUCE_LEAVES,
            Material.JUNGLE_LEAVES, Material.ACACIA_LEAVES, Material.DARK_OAK_LEAVES,
            Material.MANGROVE_LEAVES, Material.CHERRY_LEAVES, Material.AZALEA_LEAVES,
            Material.FLOWERING_AZALEA_LEAVES,
            Material.OAK_WOOD, Material.BIRCH_WOOD, Material.SPRUCE_WOOD,
            Material.JUNGLE_WOOD, Material.ACACIA_WOOD, Material.DARK_OAK_WOOD,
            Material.SHORT_GRASS, Material.TALL_GRASS, Material.FERN, Material.LARGE_FERN,
            Material.DEAD_BUSH, Material.DANDELION, Material.POPPY,
            Material.BLUE_ORCHID, Material.ALLIUM, Material.AZURE_BLUET,
            Material.RED_TULIP, Material.ORANGE_TULIP, Material.WHITE_TULIP,
            Material.PINK_TULIP, Material.OXEYE_DAISY, Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY, Material.SUNFLOWER, Material.LILAC,
            Material.ROSE_BUSH, Material.PEONY, Material.PITCHER_PLANT,
            Material.TORCHFLOWER, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM,
            Material.SUGAR_CANE, Material.BAMBOO, Material.VINE,
            Material.SNOW, Material.SEAGRASS, Material.TALL_SEAGRASS
    );

    private final List<StructureDef>   structures;
    private final PendingEntitySpawner entitySpawner;
    private final Logger               logger;

    private final Plugin plugin;  // añadir campo

    public StructurePopulator(Logger logger, PendingEntitySpawner entitySpawner, Plugin plugin) {
        this.logger        = logger;
        this.entitySpawner = entitySpawner;
        this.plugin        = plugin;
        this.structures    = new ArrayList<>();
        registerDefaultStructures();
    }


    private void registerDefaultStructures() {
        // Estructuras en código
        for (StructureDef.Rotation rot : StructureDef.Rotation.values()) {
            //register(new RuinasTorre(rot));
            register(new ForjaAncestral(rot));
        }

        // Estructuras desde JSON
        JsonLootTableLoader lootLoader = new JsonLootTableLoader(plugin.getDataFolder(), logger);
        lootLoader.loadAll();

        File structuresFolder = new File(plugin.getDataFolder(), "structure_exports");
        if (!structuresFolder.exists()) structuresFolder.mkdirs();

        File[] files = structuresFolder.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return;

        for (File f : files) {
            for (StructureDef.Rotation rot : StructureDef.Rotation.values()) {
                register(new JsonStructure(f, logger, lootLoader).rotation(rot));
            }
        }
    }

    public void register(StructureTemplate template) {
        StructureDef def = template.build();
        structures.add(def);
        logger.info("[StructurePopulator] Registrada: " + def.getId()
                + (def.getAllowedBiomes().isEmpty() ? " (cualquier bioma)" : " → " + def.getAllowedBiomes()));
    }

    public List<StructureDef> getStructures() {
        return Collections.unmodifiableList(structures);
    }


    @Override
    public void populate(WorldInfo worldInfo, Random random,
                         int chunkX, int chunkZ, LimitedRegion limitedRegion) {
        if (structures.isEmpty()) return;

        long seed = worldInfo.getSeed()
                ^ ((long) chunkX * 341873128712L)
                ^ ((long) chunkZ * 132897987541L);
        Random chunkRandom = new Random(seed);

        // 1. Get the biome using the center of the chunk first
        int chunkCenterX = (chunkX << 4) + 8;
        int chunkCenterZ = (chunkZ << 4) + 8;
        Biome biome = limitedRegion.getBiome(chunkCenterX, 64, chunkCenterZ);

        // ── Construcción de candidatos sin stream ──────────────────────────
        List<StructureDef> candidates = new ArrayList<>();

        for (StructureDef s : structures) {
            if (!s.allowedIn(biome)) {
                logger.info("[DEBUG] " + s.getId() + " RECHAZADO por bioma: " + biome);
                continue;
            }
            double roll = chunkRandom.nextDouble();
            logger.info("[DEBUG] " + s.getId() + " roll=" + roll + " chance=" + s.getSpawnChance());
            if (roll <= s.getSpawnChance()) {
                logger.info("[DEBUG] " + s.getId() + " ACEPTADO");
                candidates.add(s);
            }
        }
        // ──────────────────────────────────────────────────────────────────

        if (candidates.isEmpty()) {
            logger.info("[DEBUG] Estructuras totales: " + structures.size()
                    + " | bioma: " + biome
                    + " | candidatos finales: 0");
            return;
        }

        // 2. Select the structure
        StructureDef structure = candidates.get(chunkRandom.nextInt(candidates.size()));
        logger.info("[DEBUG] Estructura elegida: " + structure.getId());

        // 3. NOW calculate worldX and worldZ since 'structure' has been defined
        int localX = 8 - (structure.getMaxRelX() / 2);
        int localZ = 8 - (structure.getMaxRelZ() / 2);
        int worldX = (chunkX << 4) + localX;
        int worldZ = (chunkZ << 4) + localZ;

        int originY;
        if (structure.getSpawnMode() == StructureDef.SpawnMode.AIR) {
            originY = findAirLevel(limitedRegion, worldX, worldZ, structure, chunkRandom);
            logger.info("[DEBUG] findAirLevel retornó: " + originY);
        } else {
            originY = findGroundLevel(limitedRegion, worldX, worldZ,
                    structure.getMaxRelX(), structure.getMaxRelZ());
            logger.info("[DEBUG] findGroundLevel retornó: " + originY + " (MIN_Y=" + MIN_Y + ")");
            if (originY < MIN_Y) {
                logger.info("[DEBUG] ABORTADO: originY < MIN_Y");
                return;
            }
        }

        if (originY < 0) {
            logger.info("[DEBUG] ABORTADO: originY < 0");
            return;
        }

        placeStructure(limitedRegion, structure, worldX, originY, worldZ, chunkX, chunkZ);
    }

    private int findGroundLevel(LimitedRegion region, int originX, int originZ,
                                int maxRelX, int maxRelZ) {
        int cx = maxRelX / 2;
        int cz = maxRelZ / 2;
        int[][] samples = {
                {originX,           originZ},
                {originX + maxRelX, originZ},
                {originX,           originZ + maxRelZ},
                {originX + maxRelX, originZ + maxRelZ},
                {originX + cx,      originZ + cz}
        };

        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;

        for (int[] s : samples) {
            if (!region.isInRegion(s[0], 64, s[1])) return -1;
            int y = getSolidGroundY(region, s[0], s[1]);
            if (y < 0) return -1;
            if (y < minY) minY = y;
            if (y > maxY) maxY = y;
        }

        if (minY == Integer.MAX_VALUE) return -1;
        if (maxY - minY > 6) return -1;
        return minY;
    }

    private int findAirLevel(LimitedRegion region, int originX, int originZ,
                             StructureDef structure, Random random) {
        int minY = structure.getMinAirY();
        int maxY = structure.getMaxAirY();
        if (minY >= maxY) return -1;

        int candidateY = minY + random.nextInt(maxY - minY);

        int cx = originX + structure.getMaxRelX() / 2;
        int cz = originZ + structure.getMaxRelZ() / 2;

        if (!region.isInRegion(cx, candidateY, cz)) return -1;

        int clearance = structure.getMinClearance();
        for (int dy = 0; dy < clearance; dy++) {
            int checkY = candidateY + dy;
            if (!region.isInRegion(cx, checkY, cz)) return -1;
            if (!region.getType(cx, checkY, cz).isAir()) return -1;
        }

        return candidateY;
    }

    private int getSolidGroundY(LimitedRegion region, int x, int z) {
        int startY = region.getHighestBlockYAt(x, z, HeightMap.WORLD_SURFACE);
        for (int y = startY; y >= MIN_Y - 10; y--) {
            if (!region.isInRegion(x, y, z)) continue;
            if (GROUND_MATERIALS.contains(region.getType(x, y, z))) return y;
        }
        return -1;
    }

    private void placeStructure(LimitedRegion region, StructureDef structure,
                                int originX, int originY, int originZ,
                                int chunkX, int chunkZ) {

        boolean isAir = structure.getSpawnMode() == StructureDef.SpawnMode.AIR;

        if (!isAir) clearVegetation(region, structure, originX, originY, originZ);

        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            int wx = originX + entry.relX();
            int wy = originY + 1 + entry.relY();
            int wz = originZ + entry.relZ();
            if (!region.isInRegion(wx, wy, wz)) continue;
            region.setType(wx, wy, wz, entry.material());
        }

        if (!isAir) fillUnderBase(region, structure, originX, originY, originZ);

        scheduleChests(structure, originX, originY, originZ, chunkX, chunkZ);
        scheduleEntities(structure, originX, originY, originZ, chunkX, chunkZ);
        scheduleBlockData(structure, originX, originY, originZ, chunkX, chunkZ); // <- nuevo

        logger.info("[StructurePopulator] Colocando " + structure.getId()
                + " en " + originX + ", " + (originY + 1) + ", " + originZ);
    }

    private void scheduleBlockData(StructureDef structure,
                                   int originX, int originY, int originZ,
                                   int chunkX, int chunkZ) {
        for (StructureDef.BlockDataEntry entry : structure.getBlockDataEntries()) {
            int wx = originX + entry.relX();
            int wy = originY + 1 + entry.relY();
            int wz = originZ + entry.relZ();
            entitySpawner.scheduleBlockData(chunkX, chunkZ, wx, wy, wz, entry.blockData());
        }
    }

    private void clearVegetation(LimitedRegion region, StructureDef structure,
                                 int originX, int originY, int originZ) {
        Set<Long> footprint = new HashSet<>();
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            if (entry.relY() >= 0)
                footprint.add(((long) entry.relX() << 32) | (entry.relZ() & 0xFFFFFFFFL));
        }
        for (long key : footprint) {
            int relX = (int) (key >> 32);
            int relZ = (int) (key & 0xFFFFFFFFL);
            int wx = originX + relX;
            int wz = originZ + relZ;
            for (int wy = originY + 1; wy <= originY + 30; wy++) {
                if (!region.isInRegion(wx, wy, wz)) break;
                Material m = region.getType(wx, wy, wz);
                if (m.isAir()) break;
                if (VEGETATION.contains(m)) region.setType(wx, wy, wz, Material.AIR);
            }
        }
    }

    private void fillUnderBase(LimitedRegion region, StructureDef structure,
                               int originX, int originY, int originZ) {
        Set<Long> basePrint = new HashSet<>();
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            if (entry.relY() == 0)
                basePrint.add(((long) entry.relX() << 32) | (entry.relZ() & 0xFFFFFFFFL));
        }
        for (long key : basePrint) {
            int relX = (int) (key >> 32);
            int relZ = (int) (key & 0xFFFFFFFFL);
            int wx = originX + relX;
            int wz = originZ + relZ;
            for (int wy = originY; wy >= MIN_Y - 5; wy--) {
                if (!region.isInRegion(wx, wy, wz)) break;
                Material m = region.getType(wx, wy, wz);
                if (GROUND_MATERIALS.contains(m)) break;
                if (!m.isAir() && !VEGETATION.contains(m)) break;
                region.setType(wx, wy, wz, Material.DIRT);
            }
        }
    }

    private void scheduleChests(StructureDef structure,
                                int originX, int originY, int originZ,
                                int chunkX, int chunkZ) {
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            LootTable loot = structure.getChestLoot(entry.relX(), entry.relY(), entry.relZ());
            if (loot == null) continue;

            int wx = originX + entry.relX();
            int wy = originY + 1 + entry.relY();
            int wz = originZ + entry.relZ();

            entitySpawner.scheduleChest(chunkX, chunkZ, wx, wy, wz, loot);
        }
    }

    private void scheduleEntities(StructureDef structure,
                                  int originX, int originY, int originZ,
                                  int chunkX, int chunkZ) {
        for (StructureDef.EntityEntry entry : structure.getEntities()) {
            int wx = originX + entry.relX();
            int wy = originY + 1 + entry.relY();
            int wz = originZ + entry.relZ();

            entitySpawner.scheduleEntity(chunkX, chunkZ, wx, wy, wz,
                    entry.type(), entry.customizer());
        }
    }
}
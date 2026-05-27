package org.delta.worldgen;

import org.bukkit.HeightMap;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.generator.BlockPopulator;
import org.bukkit.generator.LimitedRegion;
import org.bukkit.generator.WorldInfo;
import org.delta.worldgen.structures.RuinasTorre;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StructurePopulator extends BlockPopulator {

    private static final double SPAWN_CHANCE   = 0.9;
    private static final int    BORDER_PADDING = 3;
    private static final int    MIN_Y          = 60;

    private final List<StructureDef> structures = new ArrayList<>();
    private final Logger logger;

    public StructurePopulator(Logger logger) {
        this.logger = logger;
        registerDefaultStructures();
    }

    private void registerDefaultStructures() {
        register(new RuinasTorre());
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

        int localX = BORDER_PADDING + chunkRandom.nextInt(16 - BORDER_PADDING * 2);
        int localZ = BORDER_PADDING + chunkRandom.nextInt(16 - BORDER_PADDING * 2);
        int worldX = (chunkX << 4) + localX;
        int worldZ = (chunkZ << 4) + localZ;

        Biome biome = limitedRegion.getBiome(worldX, 64, worldZ);
        List<StructureDef> candidates = structures.stream()
                .filter(s -> s.allowedIn(biome))
                .filter(s -> localX + s.getMaxRelX() < 16 - BORDER_PADDING)
                .filter(s -> localZ + s.getMaxRelZ() < 16 - BORDER_PADDING)
                .filter(s -> chunkRandom.nextDouble() <= s.getSpawnChance())
                .collect(Collectors.toList());

        if (candidates.isEmpty()) return;

        StructureDef structure = candidates.get(chunkRandom.nextInt(candidates.size()));

        int groundY = findGroundLevel(limitedRegion, worldX, worldZ,
                structure.getMaxRelX(), structure.getMaxRelZ());

        if (groundY < MIN_Y) return;

        if (!isValidSurface(limitedRegion, worldX, groundY, worldZ)) return;

        placeStructure(limitedRegion, structure, worldX, groundY, worldZ);
    }


    private int findGroundLevel(LimitedRegion region, int originX, int originZ,
                                int maxRelX, int maxRelZ) {
        int halfX = maxRelX / 2;
        int halfZ = maxRelZ / 2;

        int[][] samples = {
                {originX,         originZ},
                {originX + maxRelX, originZ},
                {originX,         originZ + maxRelZ},
                {originX + maxRelX, originZ + maxRelZ},
                {originX + halfX, originZ + halfZ}   // centro
        };

        int minY = Integer.MAX_VALUE;
        for (int[] sample : samples) {
            if (!region.isInRegion(sample[0], 64, sample[1])) continue;
            int y = region.getHighestBlockYAt(sample[0], sample[1],
                    HeightMap.MOTION_BLOCKING_NO_LEAVES);
            if (y < minY) minY = y;
        }
        return minY == Integer.MAX_VALUE ? 64 : minY;
    }


    private boolean isValidSurface(LimitedRegion region, int x, int y, int z) {
        if (!region.isInRegion(x, y, z)) return false;
        Material surface = region.getType(x, y, z);

        if (!surface.isSolid())                  return false;
        if (surface == Material.WATER)           return false;
        if (surface == Material.LAVA)            return false;
        if (surface.name().contains("LEAVES"))   return false;
        if (surface.name().contains("LOG"))      return false;  // copa de árbol
        if (surface == Material.LILY_PAD)        return false;

        return true;
    }


    private void placeStructure(LimitedRegion region, StructureDef structure,
                                int originX, int originY, int originZ) {
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            int wx = originX + entry.relX();
            int wy = originY + entry.relY();
            int wz = originZ + entry.relZ();
            if (!region.isInRegion(wx, wy, wz)) continue;
            region.setType(wx, wy, wz, entry.material());
        }

        Set<Long> basePrint = new HashSet<>();
        for (StructureDef.BlockEntry entry : structure.getBlocks()) {
            if (entry.relY() == 0) {
                basePrint.add(((long)(entry.relX()) << 32) | (entry.relZ() & 0xFFFFFFFFL));
            }
        }

        for (long key : basePrint) {
            int relX = (int)(key >> 32);
            int relZ = (int)(key & 0xFFFFFFFFL);
            int wx   = originX + relX;
            int wz   = originZ + relZ;

            for (int wy = originY - 1; wy >= MIN_Y - 5; wy--) {
                if (!region.isInRegion(wx, wy, wz)) break;
                Material m = region.getType(wx, wy, wz);
                if (m.isSolid()) break;  // ya hay suelo, parar
                region.setType(wx, wy, wz, Material.DIRT); // rellenar hueco
            }
        }

        logger.info("[StructurePopulator] Colocando " + structure.getId()
                + " en " + originX + ", " + originY + ", " + originZ);
    }
}
package org.delta.listeners.worldgen;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.plugin.Plugin;
import org.delta.worldgen.LootTable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class PendingEntitySpawner implements Listener {

    public record PendingSpawn(int x, int y, int z,
                               EntityType type,
                               Consumer<Entity> customizer) {}

    public record PendingChestFill(int x, int y, int z, LootTable lootTable) {}

    public record PendingBlockData(int x, int y, int z, BlockData blockData) {}

    private final Map<Long, List<PendingSpawn>>     pendingSpawns      = new ConcurrentHashMap<>();
    private final Map<Long, List<PendingChestFill>> pendingChests      = new ConcurrentHashMap<>();
    private final Map<Long, List<PendingBlockData>> pendingBlockData   = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final Logger logger;

    public PendingEntitySpawner(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    public void scheduleEntity(int chunkX, int chunkZ,
                               int x, int y, int z,
                               EntityType type, Consumer<Entity> customizer) {
        pendingSpawns
                .computeIfAbsent(chunkKey(chunkX, chunkZ),
                        k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PendingSpawn(x, y, z, type, customizer));
    }

    public void scheduleEntity(int chunkX, int chunkZ, int x, int y, int z, EntityType type) {
        scheduleEntity(chunkX, chunkZ, x, y, z, type, null);
    }

    public void scheduleChest(int chunkX, int chunkZ,
                              int x, int y, int z, LootTable lootTable) {
        pendingChests
                .computeIfAbsent(chunkKey(chunkX, chunkZ),
                        k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PendingChestFill(x, y, z, lootTable));
    }

    public void scheduleBlockData(int chunkX, int chunkZ,
                                  int x, int y, int z, BlockData blockData) {
        pendingBlockData
                .computeIfAbsent(chunkKey(chunkX, chunkZ),
                        k -> Collections.synchronizedList(new ArrayList<>()))
                .add(new PendingBlockData(x, y, z, blockData));
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;

        final World world = event.getChunk().getWorld();
        final long  key   = chunkKey(event.getChunk().getX(), event.getChunk().getZ());

        final List<PendingSpawn>     spawns     = pendingSpawns.remove(key);
        final List<PendingChestFill> chests     = pendingChests.remove(key);
        final List<PendingBlockData> blockDatas = pendingBlockData.remove(key);

        if (spawns == null && chests == null && blockDatas == null) return;

        Bukkit.getScheduler().runTask(plugin, () -> {

            // BlockData primero — antes que cofres y entidades
            if (blockDatas != null) {
                for (PendingBlockData pending : blockDatas) {
                    try {
                        Block block = world.getBlockAt(pending.x(), pending.y(), pending.z());
                        block.setBlockData(pending.blockData(), false);
                    } catch (Exception e) {
                        logger.warning("[PendingEntitySpawner] Error BlockData: " + e.getMessage());
                    }
                }
            }

            if (chests != null) {
                Random random = new Random();
                for (PendingChestFill fill : chests) {
                    try {
                        Block      block = world.getBlockAt(fill.x(), fill.y(), fill.z());
                        BlockState state = block.getState();
                        if (state instanceof Container container) {
                            fill.lootTable().fill(container.getInventory(), random);
                        } else {
                            logger.warning(String.format(
                                    "[PendingEntitySpawner] Esperaba cofre en %d,%d,%d pero hay %s",
                                    fill.x(), fill.y(), fill.z(), block.getType()));
                        }
                    } catch (Exception e) {
                        logger.warning("[PendingEntitySpawner] Error cofre: " + e.getMessage());
                    }
                }
            }

            if (spawns != null) {
                for (PendingSpawn spawn : spawns) {
                    try {
                        Entity entity = world.spawnEntity(
                                new org.bukkit.Location(world, spawn.x(), spawn.y(), spawn.z()),
                                spawn.type()
                        );
                        entity.setPersistent(true);
                        if (spawn.customizer() != null) spawn.customizer().accept(entity);
                    } catch (Exception e) {
                        logger.warning("[PendingEntitySpawner] Error entidad "
                                + spawn.type() + ": " + e.getMessage());
                    }
                }
            }
        });
    }

    private long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}
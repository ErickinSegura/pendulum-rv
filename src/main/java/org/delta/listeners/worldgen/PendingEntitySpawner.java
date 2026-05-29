package org.delta.listeners.worldgen;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
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

    private final Map<Long, List<PendingSpawn>>     pendingSpawns = new ConcurrentHashMap<>();
    private final Map<Long, List<PendingChestFill>> pendingChests = new ConcurrentHashMap<>();

    private final Plugin plugin;
    private final Logger logger;

    public PendingEntitySpawner(Plugin plugin) {
        this.plugin = plugin;
        this.logger = plugin.getLogger();
    }

    // ─── API pública ────────────────────────────────────────────────────────────

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

    // ─── Listener ───────────────────────────────────────────────────────────────

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        if (!event.isNewChunk()) return;

        final World world  = event.getChunk().getWorld();
        final long  key    = chunkKey(event.getChunk().getX(), event.getChunk().getZ());

        final List<PendingSpawn>     spawns = pendingSpawns.remove(key);
        final List<PendingChestFill> chests = pendingChests.remove(key);

        if (spawns == null && chests == null) return;

        // Diferir un tick: en este momento el chunk aún está siendo finalizado
        // por Paper y los tile entities / entity tracking no están listos todavía.
        Bukkit.getScheduler().runTask(plugin, () -> {

            // ── Cofres ────────────────────────────────────────────────────────
            if (chests != null) {
                Random random = new Random();
                for (PendingChestFill fill : chests) {
                    try {
                        Block      block = world.getBlockAt(fill.x(), fill.y(), fill.z());
                        BlockState state = block.getState();

                        if (state instanceof Container container) {
                            fill.lootTable().fill(container.getInventory(), random);
                            // update() NO es necesario para inventarios: la referencia
                            // ya es al inventory vivo del tile entity.
                            logger.info(String.format(
                                    "[PendingEntitySpawner] Cofre '%s' llenado en %d, %d, %d",
                                    fill.lootTable().getId(), fill.x(), fill.y(), fill.z()));
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

            // ── Entidades ──────────────────────────────────────────────────────
            if (spawns != null) {
                for (PendingSpawn spawn : spawns) {
                    try {
                        Entity entity = world.spawnEntity(
                                new org.bukkit.Location(world, spawn.x(), spawn.y(), spawn.z()),
                                spawn.type()
                        );
                        if (spawn.customizer() != null) spawn.customizer().accept(entity);
                        logger.info(String.format(
                                "[PendingEntitySpawner] Spawneado %s en %d, %d, %d",
                                spawn.type(), spawn.x(), spawn.y(), spawn.z()));
                    } catch (Exception e) {
                        logger.warning("[PendingEntitySpawner] Error entidad "
                                + spawn.type() + ": " + e.getMessage());
                    }
                }
            }
        });
    }

    // ─── Interno ────────────────────────────────────────────────────────────────

    private long chunkKey(int chunkX, int chunkZ) {
        return ((long) chunkX << 32) | (chunkZ & 0xFFFFFFFFL);
    }
}
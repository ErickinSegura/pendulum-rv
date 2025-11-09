package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Chest.Type;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ChestEvents {
    public static void placeDeathChest(Player player, Location location, PlayerDeathEvent event) {
        if (location == null || event == null) return;

        List<ItemStack> drops = event.getDrops();

        if (drops.isEmpty()) return;

        int slotsNeeded = countInventorySlots(drops);
        boolean needsDoubleChest = slotsNeeded > 27;

        Location chestLocation = findSafeChestLocation(location);

        if (chestLocation == null) {
            player.sendMessage(Component.text("No se pudo colocar el cofre de muerte", NamedTextColor.RED));
            return;
        }

        Inventory chestInventory;

        if (needsDoubleChest) {
            chestInventory = createDoubleChest(chestLocation);
            if (chestInventory == null) {
                // Si no se pudo crear cofre doble, crear cofre simple
                Block block = chestLocation.getBlock();
                block.setType(Material.CHEST);
                Chest chest = (Chest) block.getState();
                chestInventory = chest.getInventory();
            }
        } else {
            Block block = chestLocation.getBlock();
            block.setType(Material.CHEST);
            Chest chest = (Chest) block.getState();
            chestInventory = chest.getInventory();
        }

        List<ItemStack> overflow = new ArrayList<>();
        for (ItemStack item : drops) {
            if (item != null && item.getType() != Material.AIR) {
                HashMap<Integer, ItemStack> leftover = chestInventory.addItem(item);

                if (!leftover.isEmpty()) {
                    overflow.addAll(leftover.values());
                }
            }
        }

        drops.clear();

        if (!overflow.isEmpty()) {
            handleOverflowItems(player, chestLocation, overflow, drops);
        }

        // Mensaje al jugador
        String chestType = (chestInventory.getSize() > 27) ? "cofre doble" : "cofre";
        String coordMessage = String.format("§eTus items están en un %s en: §l%d/%d/%d",
                chestType,
                chestLocation.getBlockX(),
                chestLocation.getBlockY(),
                chestLocation.getBlockZ());
        player.sendMessage(coordMessage);
    }

    private static int countInventorySlots(List<ItemStack> items) {
        int slots = 0;
        for (ItemStack item : items) {
            if (item != null && item.getType() != Material.AIR) {
                slots++;
            }
        }
        return slots;
    }

    private static Inventory createDoubleChest(Location firstChestLocation) {
        Location[] adjacentLocations = {
                firstChestLocation.clone().add(1, 0, 0),  // Este
                firstChestLocation.clone().add(-1, 0, 0), // Oeste
                firstChestLocation.clone().add(0, 0, 1),  // Sur
                firstChestLocation.clone().add(0, 0, -1)  // Norte
        };

        // Buscar ubicación adyacente válida para el segundo cofre
        for (Location secondLocation : adjacentLocations) {
            Block secondBlock = secondLocation.getBlock();
            if (secondBlock.getType() == Material.AIR || !secondBlock.getType().isSolid()) {
                // Colocar primer cofre
                Block firstBlock = firstChestLocation.getBlock();
                firstBlock.setType(Material.CHEST);

                // Colocar segundo cofre adyacente
                secondBlock.setType(Material.CHEST);

                // Configurar BlockData para conectar los cofres
                BlockData firstData = firstBlock.getBlockData();
                if (firstData instanceof org.bukkit.block.data.type.Chest) {
                    org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) firstData;
                    chestData.setType(Type.LEFT);
                    firstBlock.setBlockData(chestData);
                }

                BlockData secondData = secondBlock.getBlockData();
                if (secondData instanceof org.bukkit.block.data.type.Chest) {
                    org.bukkit.block.data.type.Chest chestData = (org.bukkit.block.data.type.Chest) secondData;
                    chestData.setType(Type.RIGHT);
                    secondBlock.setBlockData(chestData);
                }

                // Actualizar estados
                firstBlock.getState().update(true, false);
                secondBlock.getState().update(true, false);

                // Obtener el inventario del cofre doble
                Chest chest = (Chest) firstBlock.getState();
                return chest.getInventory();
            }
        }

        return null;
    }

    private static void handleOverflowItems(Player player, Location chestLocation,
                                            List<ItemStack> overflow, List<ItemStack> originalDrops) {
        Location secondLocation = chestLocation.clone().add(0, 1, 0);
        if (secondLocation.getBlock().getType() == Material.AIR) {
            Block secondBlock = secondLocation.getBlock();
            secondBlock.setType(Material.CHEST);

            Chest secondChest = (Chest) secondBlock.getState();
            Inventory secondInventory = secondChest.getInventory();

            List<ItemStack> stillOverflow = new ArrayList<>();
            for (ItemStack item : overflow) {
                HashMap<Integer, ItemStack> leftover = secondInventory.addItem(item);
                if (!leftover.isEmpty()) {
                    stillOverflow.addAll(leftover.values());
                }
            }

            if (!stillOverflow.isEmpty()) {
                originalDrops.addAll(stillOverflow);
                player.sendMessage(Component.text("§6Algunos items cayeron al suelo cerca del cofre", NamedTextColor.GOLD));
            }

            player.sendMessage(Component.text("§eSe creó un segundo cofre arriba para más items", NamedTextColor.GOLD));
        } else {
            originalDrops.addAll(overflow);
            player.sendMessage(Component.text("§6Algunos items cayeron al suelo porque no había espacio", NamedTextColor.GOLD));
        }
    }

    private static Location findSafeChestLocation(Location deathLocation) {
        if (isSafeForChest(deathLocation)) {
            return deathLocation;
        }

        Location above = deathLocation.clone().add(0, 1, 0);
        if (isSafeForChest(above)) {
            return above;
        }

        Location below = deathLocation.clone().add(0, -1, 0);
        if (isSafeForChest(below)) {
            return below;
        }

        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (x == 0 && z == 0) continue;
                Location nearby = deathLocation.clone().add(x, 0, z);
                if (isSafeForChest(nearby)) {
                    return nearby;
                }
            }
        }

        return deathLocation;
    }

    private static boolean isSafeForChest(Location location) {
        Block block = location.getBlock();
        return block.getType() == Material.AIR ||
                block.getType().isAir() ||
                !block.getType().isSolid();
    }
}

package org.delta.listeners.player;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class PotionListener implements Listener {

    private static final int MAX_STACK_SIZE = 16;

    @EventHandler
    public void onPlayerPickup(PlayerAttemptPickupItemEvent event) {
        ItemStack item = event.getItem().getItemStack();
        if (isPotion(item.getType())) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(MAX_STACK_SIZE);
                item.setItemMeta(meta);
            }
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null && isPotion(event.getCurrentItem().getType())) {
            ItemStack item = event.getCurrentItem();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(MAX_STACK_SIZE);
                item.setItemMeta(meta);
            }
        }

        event.getCursor();
        if (isPotion(event.getCursor().getType())) {
            ItemStack item = event.getCursor();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(MAX_STACK_SIZE);
                item.setItemMeta(meta);
            }
        }
    }

    private boolean isPotion(Material material) {
        return material == Material.POTION ||
                material == Material.SPLASH_POTION ||
                material == Material.LINGERING_POTION;
    }
}
package org.delta.listeners.items;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.commands.menu.GiveMenuHolder;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.ItemRegistry;
import org.delta.libs.MessageUtils;

public class GiveMenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GiveMenuHolder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        ItemMeta meta = clicked.getItemMeta();
        String key = meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING);
        if (key == null) return;

        CustomItem customItem = ItemRegistry.get(key).orElse(null);
        if (customItem == null) return;

        ItemStack stack = customItem.build();
        stack.setAmount(stack.getMaxStackSize());
        player.getInventory().addItem(stack);

        player.sendMessage(MessageUtils.color("&a✔ Recibiste un stack de &f" + key));
        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f);
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof GiveMenuHolder) {
            event.setCancelled(true);
        }
    }
}

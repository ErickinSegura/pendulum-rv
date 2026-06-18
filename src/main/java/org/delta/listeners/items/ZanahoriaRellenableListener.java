package org.delta.listeners.items;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.consumables.ZanahoriaEncantada;
import org.delta.libs.MessageUtils;

/**
 * Maneja la Zanahoria Rellenable: comerla gasta una carga (sin consumir el item)
 * y restaura comida como una zanahoria dorada; agacharse + click derecho la
 * rellena con zanahorias normales del inventario.
 */
public class ZanahoriaRellenableListener implements Listener {

    // Valores de una zanahoria dorada vanilla.
    private static final int HUNGER_RESTORE = 6;
    private static final float SATURATION_RESTORE = 14.4f;

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!isCarrot(item)) return;

        // Nunca se consume el item: lo cancelamos y aplicamos el efecto a mano.
        event.setCancelled(true);

        Player player = event.getPlayer();
        int charges = ZanahoriaEncantada.getCharges(item);
        if (charges <= 0) {
            denied(player, "&cLa zanahoria está vacía. Rellénala en la mesa de crafteo.");
            return;
        }

        player.setFoodLevel(Math.min(20, player.getFoodLevel() + HUNGER_RESTORE));
        player.setSaturation(Math.min(player.getFoodLevel(), player.getSaturation() + SATURATION_RESTORE));

        // event.getItem() es una copia: hay que reescribir el item en la mano.
        ZanahoriaEncantada.setCharges(item, charges - 1);
        if (event.getHand() == EquipmentSlot.OFF_HAND) {
            player.getInventory().setItemInOffHand(item);
        } else {
            player.getInventory().setItemInMainHand(item);
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_BURP, 0.6f, 1.0f);
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isCarrot(item)) return;

        // Si está vacía, evita la animación de comer inútil.
        if (ZanahoriaEncantada.getCharges(item) <= 0) {
            event.setCancelled(true);
            denied(event.getPlayer(), "&cLa zanahoria está vacía. Rellénala en la mesa de crafteo.");
        }
        // Con cargas, dejamos que el evento de consumo la maneje.
    }

    private void denied(Player player, String message) {
        player.sendMessage(MessageUtils.color(message));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
    }

    private boolean isCarrot(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "zanahoria_rellenable".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

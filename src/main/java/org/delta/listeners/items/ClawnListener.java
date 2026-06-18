package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.pendulum;

/**
 * Aplica el bono del Clawn de forma dinámica: +4 de rango de acción y -5 de daño
 * mientras el jugador lleva el Clawn (en cualquier mano), pero se desactiva si
 * empuña un mazo o una lanza en cualquier mano.
 */
public class ClawnListener implements Listener {

    private static final double RANGE_BONUS = 4.0;
    private static final double DAMAGE_PENALTY = -5.0;

    private static final NamespacedKey KEY_BLOCK_RANGE =
            new NamespacedKey("delta", "clawn_block_range");
    private static final NamespacedKey KEY_ENTITY_RANGE =
            new NamespacedKey("delta", "clawn_entity_range");
    private static final NamespacedKey KEY_DAMAGE =
            new NamespacedKey("delta", "clawn_damage");

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        update(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeAll(event.getPlayer());
    }

    @EventHandler
    public void onHeld(PlayerItemHeldEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) scheduleUpdate(player);
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) scheduleUpdate(player);
    }

    @EventHandler
    public void onClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player player) scheduleUpdate(player);
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) scheduleUpdate(player);
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        scheduleUpdate(event.getPlayer());
    }

    // Los eventos se disparan antes de que el inventario refleje el cambio,
    // así que recalculamos en el siguiente tick.
    private void scheduleUpdate(Player player) {
        Bukkit.getScheduler().runTask(pendulum.getInstance(), () -> {
            if (player.isOnline()) update(player);
        });
    }

    private void update(Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        ItemStack off = player.getInventory().getItemInOffHand();

        boolean hasClawn = isClawn(main) || isClawn(off);
        boolean blockedWeapon = isBlockedWeapon(main) || isBlockedWeapon(off);

        if (hasClawn && !blockedWeapon) {
            applyAll(player);
        } else {
            removeAll(player);
        }
    }

    private void applyAll(Player player) {
        apply(player, Attribute.BLOCK_INTERACTION_RANGE, KEY_BLOCK_RANGE, RANGE_BONUS);
        apply(player, Attribute.ENTITY_INTERACTION_RANGE, KEY_ENTITY_RANGE, RANGE_BONUS);
        apply(player, Attribute.ATTACK_DAMAGE, KEY_DAMAGE, DAMAGE_PENALTY);
    }

    private void removeAll(Player player) {
        remove(player, Attribute.BLOCK_INTERACTION_RANGE, KEY_BLOCK_RANGE);
        remove(player, Attribute.ENTITY_INTERACTION_RANGE, KEY_ENTITY_RANGE);
        remove(player, Attribute.ATTACK_DAMAGE, KEY_DAMAGE);
    }

    private void apply(Player player, Attribute attribute, NamespacedKey key, double amount) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (hasModifier(instance, key)) return;
        instance.addModifier(new AttributeModifier(
                key, amount, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.ANY));
    }

    private void remove(Player player, Attribute attribute, NamespacedKey key) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;
        if (!hasModifier(instance, key)) return;
        instance.removeModifier(key);
    }

    private boolean hasModifier(AttributeInstance instance, NamespacedKey key) {
        for (AttributeModifier modifier : instance.getModifiers()) {
            if (modifier.getKey().equals(key)) return true;
        }
        return false;
    }

    private boolean isClawn(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "clawn".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }

    private boolean isBlockedWeapon(ItemStack item) {
        if (item == null) return false;
        Material type = item.getType();
        return type == Material.MACE || type.name().endsWith("_SPEAR");
    }
}

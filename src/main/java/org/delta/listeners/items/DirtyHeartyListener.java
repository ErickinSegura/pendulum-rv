package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.libs.MessageUtils;
import org.delta.managers.dirtyhearty.DirtyHeartyManager;

public class DirtyHeartyListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DirtyHeartyManager.applyModifier(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        DirtyHeartyManager.applyModifier(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isDirtyHearty(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        if (DirtyHeartyManager.getCount(player) >= DirtyHeartyManager.MAX_HEARTS) {
            player.sendMessage(MessageUtils.color("&cHas alcanzado el máximo de corazones de Dirty Hearty."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
            return;
        }

        if (!DirtyHeartyManager.addHeart(player)) return;

        consumeOne(player, event.getHand());

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            player.setHealth(Math.min(attr.getValue(), player.getHealth() + DirtyHeartyManager.HEART_VALUE));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.4f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.05);
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);

        player.sendMessage(MessageUtils.color("&c❤ &7Has ganado un corazón permanente. Corazones de Dirty Hearty: &c" + DirtyHeartyManager.getCount(player)));
    }

    private void consumeOne(Player player, EquipmentSlot hand) {
        if (hand == EquipmentSlot.OFF_HAND) {
            ItemStack off = player.getInventory().getItemInOffHand();
            off.setAmount(off.getAmount() - 1);
            player.getInventory().setItemInOffHand(off);
        } else {
            ItemStack main = player.getInventory().getItemInMainHand();
            main.setAmount(main.getAmount() - 1);
            player.getInventory().setItemInMainHand(main);
        }
    }

    private boolean isDirtyHearty(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "dirty_hearty".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

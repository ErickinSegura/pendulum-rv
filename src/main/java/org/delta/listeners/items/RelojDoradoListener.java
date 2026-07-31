package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;

public class RelojDoradoListener implements Listener {

    private static final String KEY = "reloj_dorado";

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isReloj(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();

        ejecutarEfecto(player);
        consumirUno(player, event.getHand());
        efectos(player);
    }

    private void ejecutarEfecto(Player player) {
        String comando = "tellraw @a [\"\",{\"text\":\"⏳ \",\"color\":\"gold\"},"
                + "{\"text\":\"" + player.getName() + "\",\"color\":\"yellow\",\"bold\":true},"
                + "{\"text\":\" usó el \",\"color\":\"gray\"},"
                + "{\"text\":\"Reloj Dorado del Péndulo\",\"color\":\"gold\",\"bold\":true},"
                + "{\"text\":\" (+1 hora de día).\",\"color\":\"gray\"}]";

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), comando);
    }

    private void consumirUno(Player player, EquipmentSlot hand) {
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

    private void efectos(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.7f, 1.6f);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.4f);
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.END_ROD, loc, 20, 0.4, 0.6, 0.4, 0.05);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 12, 0.3, 0.5, 0.3, 0.1);
    }

    private boolean isReloj(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return KEY.equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

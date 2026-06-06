package org.delta.listeners.items;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class LanzapapasListener implements Listener {

    @EventHandler
    public void onCrossbowLoad(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!isCustomItem(held, "lanzapapas")) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        CrossbowMeta meta = (CrossbowMeta) held.getItemMeta();
        if (meta == null || meta.hasChargedProjectiles()) return;

        event.setCancelled(true);

        if (!hasItem(player, "papa_explosiva")) {
            player.sendActionBar(
                    ItemBuilder.format("&cNecesitas &6Papas Explosivas &cpara cargar esto!")
            );
            return;
        }

        // Forzar carga via NMS con una flecha real (no EMPTY)
        net.minecraft.world.item.ItemStack nmsItem =
                org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(held);

        net.minecraft.world.item.ItemStack nmsArrow =
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW);

        net.minecraft.world.item.component.ChargedProjectiles charged =
                net.minecraft.world.item.component.ChargedProjectiles.of(nmsArrow);

        nmsItem.set(
                net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,
                charged
        );

        ItemStack loadedItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(nmsItem);
        player.getInventory().setItemInMainHand(loadedItem);
        player.playSound(player.getLocation(),
                org.bukkit.Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isCustomItem(event.getBow(), "lanzapapas")) return;

        event.setCancelled(true);

        if (player.getGameMode() != GameMode.CREATIVE) {
            player.getInventory().remove(Material.ARROW);
        }

        if (!consumeItem(player, "papa_explosiva")) {
            player.sendActionBar(ItemBuilder.format("&cNo tienes &6Papas Explosivas&c!"));
            unloadCrossbow(event.getBow());
            return;
        }

        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setVelocity(event.getProjectile().getVelocity());
        projectile.getPersistentDataContainer().set(
                new NamespacedKey("delta", "papa_explosiva"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        unloadCrossbow(event.getBow());
    }


    private boolean isCustomItem(ItemStack item, String key) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return key.equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING)
        );
    }

    private boolean hasItem(Player player, String key) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isCustomItem(item, key)) return true;
        }
        return false;
    }

    private boolean consumeItem(Player player, String key) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isCustomItem(item, key)) {
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    private void unloadCrossbow(ItemStack crossbow) {
        if (crossbow == null) return;
        CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta();
        if (meta == null) return;
        meta.setChargedProjectiles(null);
        crossbow.setItemMeta(meta);
    }
}
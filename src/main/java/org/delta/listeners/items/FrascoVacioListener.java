package org.delta.listeners.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Animals;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.WaterMob;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FrascoVacioListener implements Listener {

    private static final NamespacedKey TYPE_KEY = new NamespacedKey("delta", "frasco_type");
    private static final NamespacedKey NAME_KEY = new NamespacedKey("delta", "frasco_name");
    private static final NamespacedKey BABY_KEY = new NamespacedKey("delta", "frasco_baby");

    @EventHandler
    public void onCapture(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        ItemStack item = player.getInventory().getItemInMainHand();
        if (!isFrasco(item)) return;

        Entity target = event.getEntity();
        if (!allowed(target)) return;

        event.setCancelled(true);

        if (isFilled(item)) {
            denied(player, "&cEl frasco ya contiene un mob.");
            return;
        }
        if (item.getAmount() > 1) {
            denied(player, "&cEl frasco debe estar solo en la mano.");
            return;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();
        pdc.set(TYPE_KEY, PersistentDataType.STRING, target.getType().name());
        if (target.getCustomName() != null) {
            pdc.set(NAME_KEY, PersistentDataType.STRING, target.getCustomName());
        }
        if (target instanceof Ageable ageable && !ageable.isAdult()) {
            pdc.set(BABY_KEY, PersistentDataType.BYTE, (byte) 1);
        }
        meta.setCustomModelData(2);
        meta.setLore(List.of(
                ItemBuilder.format("&7Contiene: &f" + pretty(target.getType())),
                ItemBuilder.format("&7Click derecho al suelo para liberarlo.")
        ));
        item.setItemMeta(meta);

        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 0.5, 0), 25, 0.3, 0.5, 0.3, 0.2);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.3f);
        target.remove();
    }

    @EventHandler
    public void onRelease(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isFrasco(item)) return;

        event.setCancelled(true);
        if (!isFilled(item)) return;

        Player player = event.getPlayer();
        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        String typeName = pdc.get(TYPE_KEY, PersistentDataType.STRING);
        EntityType type;
        try {
            type = EntityType.valueOf(typeName);
        } catch (IllegalArgumentException | NullPointerException e) {
            resetFrasco(item, meta, pdc);
            return;
        }

        Location loc;
        if (event.getClickedBlock() != null) {
            loc = event.getClickedBlock().getRelative(event.getBlockFace()).getLocation().add(0.5, 0, 0.5);
        } else {
            loc = player.getEyeLocation().add(player.getLocation().getDirection().multiply(2));
        }

        Entity spawned = loc.getWorld().spawnEntity(loc, type);
        if (pdc.has(BABY_KEY, PersistentDataType.BYTE) && spawned instanceof Ageable ageable) {
            ageable.setBaby();
        }
        String name = pdc.get(NAME_KEY, PersistentDataType.STRING);
        if (name != null) {
            spawned.setCustomName(name);
            spawned.setCustomNameVisible(true);
        }

        resetFrasco(item, meta, pdc);

        loc.getWorld().spawnParticle(Particle.PORTAL, loc.clone().add(0, 0.5, 0), 25, 0.3, 0.5, 0.3, 0.2);
        loc.getWorld().playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 0.8f, 1.0f);
    }

    private void resetFrasco(ItemStack item, ItemMeta meta, PersistentDataContainer pdc) {
        pdc.remove(TYPE_KEY);
        pdc.remove(NAME_KEY);
        pdc.remove(BABY_KEY);
        meta.setCustomModelData(1);
        meta.setDisplayName(ItemBuilder.format("&5Frasco del Vacío"));
        meta.setLore(List.of(
                ItemBuilder.format("&7Click izquierdo a un mob pasivo o"),
                ItemBuilder.format("&7neutral para guardarlo."),
                ItemBuilder.format("&7Click derecho al suelo para liberarlo.")
        ));
        item.setItemMeta(meta);
    }

    private boolean allowed(Entity entity) {
        if (entity instanceof Player) return false;
        if (entity instanceof Monster) return false;
        if (!entity.getScoreboardTags().isEmpty()) return false;
        return entity instanceof Animals
                || entity instanceof WaterMob
                || entity instanceof Allay
                || entity instanceof AbstractVillager
                || entity instanceof IronGolem
                || entity instanceof Snowman
                || entity instanceof HappyGhast;
    }

    private String pretty(EntityType type) {
        String raw = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }

    private boolean isFrasco(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "frasco_vacio".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }

    private boolean isFilled(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(TYPE_KEY, PersistentDataType.STRING);
    }

    private void denied(Player player, String message) {
        player.sendMessage(MessageUtils.color(message));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
    }
}

package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.managers.dirtyhearty.DirtyHeartyManager;

public class DirtyHeartyListener implements Listener {

    private static final int DIA_DESBLOQUEO = 10;

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        DirtyHeartyManager.applyModifier(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        DirtyHeartyManager.applyModifier(event.getPlayer());
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (!isDirtyHearty(item)) return;

        Player player = event.getPlayer();

        if (PendulumSettings.getInstance().getDia() < DIA_DESBLOQUEO) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.color("&cLa Dirty Hearty no está disponible hasta el día " + DIA_DESBLOQUEO + "."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
            return;
        }

        if (DirtyHeartyManager.getCount(player) >= DirtyHeartyManager.MAX_HEARTS) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.color("&cHas alcanzado el máximo de corazones de Dirty Hearty."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
            org.delta.pendulum.getInstance().getAchievementManager()
                    .unlock(player, org.delta.managers.achievements.Achievement.YA_NO_CABE);
            return;
        }

        if (!DirtyHeartyManager.addHeart(player)) {
            event.setCancelled(true);
            return;
        }

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null) {
            player.setHealth(Math.min(attr.getValue(), player.getHealth() + DirtyHeartyManager.HEART_VALUE));
        }

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 0.8f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.6f, 1.4f);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0), 12, 0.4, 0.5, 0.4, 0.05);
        player.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, player.getLocation().add(0, 1, 0), 8, 0.3, 0.5, 0.3, 0.05);

        player.sendMessage(MessageUtils.color("&c❤ &7Has ganado un corazón permanente. Corazones de Dirty Hearty: &c" + DirtyHeartyManager.getCount(player)));

        var logros = org.delta.pendulum.getInstance().getAchievementManager();
        logros.unlock(player, org.delta.managers.achievements.Achievement.PRIMER_LATIDO);
        if (DirtyHeartyManager.getCount(player) >= DirtyHeartyManager.MAX_HEARTS) {
            logros.unlock(player, org.delta.managers.achievements.Achievement.CORAZON_COMPLETO);
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

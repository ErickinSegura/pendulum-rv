package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.Team;
import org.delta.customs.items.CustomItem;
import org.delta.managers.achievements.Achievement;
import org.delta.pendulum;

public class VaritaCurativaListener implements Listener {

    private static final double HEAL_AMOUNT = 8.0;
    private static final double RADIUS = 5.0;
    private static final int ABSORPTION_TICKS = 400;
    private static final int ABSORPTION_AMP = 0;
    private static final int COOLDOWN_TICKS = 200;

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isWand(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasCooldown(item.getType())) return;

        Team team = player.getScoreboard().getEntryTeam(player.getName());

        restore(player);
        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof Player other)) continue;
            if (team == null || !team.hasEntry(other.getName())) continue;
            restore(other);
            pendulum.getInstance().getAchievementManager().unlock(player, Achievement.TOQUE_SANADOR);
        }

        player.setCooldown(item.getType(), COOLDOWN_TICKS);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0),
                20, RADIUS / 2, 1.0, RADIUS / 2, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.6f);
    }

    private void restore(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attribute.MAX_HEALTH);
        double max = maxHealth != null ? maxHealth.getValue() : 20.0;
        player.setHealth(Math.min(max, player.getHealth() + HEAL_AMOUNT));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, ABSORPTION_TICKS, ABSORPTION_AMP, false, true));
        player.removePotionEffect(PotionEffectType.POISON);
        player.removePotionEffect(PotionEffectType.WITHER);
        player.getWorld().spawnParticle(Particle.HEART, player.getLocation().add(0, 1, 0),
                8, 0.4, 0.5, 0.4, 0.0);
    }

    private boolean isWand(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "varita_curativa".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

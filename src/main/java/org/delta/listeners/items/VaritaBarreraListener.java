package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
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
import org.bukkit.util.Vector;
import org.delta.customs.items.CustomItem;
import org.delta.managers.achievements.Achievement;
import org.delta.pendulum;

public class VaritaBarreraListener implements Listener {

    private static final double RADIUS = 6.0;
    private static final double KNOCKBACK_HORIZONTAL = 1.4;
    private static final double KNOCKBACK_VERTICAL = 0.45;
    private static final int SLOW_TICKS = 40;
    private static final int RESISTANCE_TICKS = 100;
    private static final int COOLDOWN_TICKS = 300;

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
        player.setCooldown(item.getType(), COOLDOWN_TICKS);

        Team team = player.getScoreboard().getEntryTeam(player.getName());

        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof LivingEntity target)) continue;
            if (target.equals(player)) continue;
            if (target instanceof Player other && team != null && team.hasEntry(other.getName())) continue;

            repel(player, target);
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, SLOW_TICKS, 0, false, true));
        }

        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, RESISTANCE_TICKS, 0, false, true));
        pendulum.getInstance().getAchievementManager().unlock(player, Achievement.TRAS_EL_CRISTAL);

        player.getWorld().spawnParticle(Particle.SWEEP_ATTACK, player.getLocation().add(0, 1, 0),
                24, RADIUS / 2, 0.6, RADIUS / 2, 0.0);
        player.getWorld().spawnParticle(Particle.END_ROD, player.getLocation().add(0, 1, 0),
                40, RADIUS / 2, 0.6, RADIUS / 2, 0.05);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_GENERIC_EXPLODE, 0.7f, 1.6f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.6f, 2.0f);
    }

    private void repel(Player source, LivingEntity target) {
        Vector direction = target.getLocation().toVector().subtract(source.getLocation().toVector());
        direction.setY(0);
        if (direction.lengthSquared() < 1.0e-6) {
            direction = source.getLocation().getDirection().setY(0);
        }
        if (direction.lengthSquared() < 1.0e-6) {
            direction = new Vector(1, 0, 0);
        }
        direction.normalize().multiply(KNOCKBACK_HORIZONTAL).setY(KNOCKBACK_VERTICAL);
        target.setVelocity(target.getVelocity().add(direction));
    }

    private boolean isWand(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "varita_barrera".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

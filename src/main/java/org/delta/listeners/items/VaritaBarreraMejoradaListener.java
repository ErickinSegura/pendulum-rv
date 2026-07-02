package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
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

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VaritaBarreraMejoradaListener implements Listener {

    private static final double RADIUS = 10.0;
    private static final int DURATION_TICKS = 200;
    private static final int COOLDOWN_TICKS = 600;
    private static final int RESISTANCE_AMP = 1;
    private static final double REFLECT_FRACTION = 0.25;

    private final Map<UUID, Long> reflectUntil = new HashMap<>();

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
        fortify(player);
        reflectUntil.put(player.getUniqueId(), System.currentTimeMillis() + DURATION_TICKS * 50L);

        for (Entity entity : player.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (entity instanceof Player other) {
                if (team != null && team.hasEntry(other.getName())) fortify(other);
            } else if (entity instanceof Mob mob) {
                mob.setTarget(player);
            }
        }

        pendulum.getInstance().getAchievementManager().unlock(player, Achievement.BASTION_DE_CRISTAL);

        player.getWorld().spawnParticle(Particle.ANGRY_VILLAGER, player.getLocation().add(0, 2, 0), 20, 1.0, 0.5, 1.0);
        player.getWorld().spawnParticle(Particle.ENCHANTED_HIT, player.getLocation().add(0, 1, 0),
                40, 1.2, 1.0, 1.2, 0.0);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_ATTACK, 0.9f, 0.8f);
        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 0.7f, 1.4f);
    }

    @EventHandler
    public void onReflect(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        Long until = reflectUntil.get(defender.getUniqueId());
        if (until == null || System.currentTimeMillis() > until) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (attacker.equals(defender)) return;

        double reflect = event.getFinalDamage() * REFLECT_FRACTION;
        if (reflect <= 0) return;

        attacker.damage(reflect, defender);
        defender.getWorld().spawnParticle(Particle.ENCHANTED_HIT, defender.getLocation().add(0, 1, 0),
                6, 0.3, 0.5, 0.3, 0.0);
    }

    private void fortify(Player player) {
        player.addPotionEffect(new PotionEffect(
                PotionEffectType.RESISTANCE, DURATION_TICKS, RESISTANCE_AMP, false, true));
    }

    private boolean isWand(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "varita_barrera_mejorada".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

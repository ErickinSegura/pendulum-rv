package org.delta.listeners.perks.impl;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class LifestealListener extends BasePerkListener {

    private static final double HEAL_PERCENT = 0.10; // 10% del HP máximo del objetivo
    private static final long COOLDOWN_TICKS = 100L;  // 1 segundo = 20 ticks

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player attacker = getAttacker(event);
        if (attacker == null) return;
        if (!hasTeamPerk(attacker, Perk.LIFE_STEAL)) return;
        if (isOnCooldown(attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        double healAmount = event.getFinalDamage() * HEAL_PERCENT;
        double newHp = Math.min(attacker.getHealth() + healAmount, getMaxHealth(attacker));

        attacker.setHealth(newHp);
        attacker.getWorld().spawnParticle(
                Particle.HEART,
                attacker.getLocation().add(0, 1.5, 0),
                8,
                0.4,
                0.4,
                0.4,
                0
        );
        cooldowns.put(attacker.getUniqueId(), System.currentTimeMillis());
    }

    private Player getAttacker(EntityDamageByEntityEvent event) {
        Entity damager = event.getDamager();
        if (damager instanceof Player player) return player;
        return null;
    }

    private boolean isOnCooldown(Player player) {
        if (!cooldowns.containsKey(player.getUniqueId())) return false;
        long elapsed = System.currentTimeMillis() - cooldowns.get(player.getUniqueId());
        return elapsed < (COOLDOWN_TICKS * 50L); // ticks a ms
    }

    private double getMaxHealth(LivingEntity entity) {
        return Objects.requireNonNull(entity.getAttribute(Attribute.MAX_HEALTH)).getValue();
    }
}
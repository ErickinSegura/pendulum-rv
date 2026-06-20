package org.delta.listeners.perks.impl;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.scoreboard.Team;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

public class SanacionCompartidaListener extends BasePerkListener {

    private static final double SHARE_PERCENT = 0.5;
    private static final double RADIUS = 8.0;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player source)) return;
        if (!hasTeamPerk(source, Perk.SANACION_COMPARTIDA)) return;

        double shared = event.getAmount() * SHARE_PERCENT;
        if (shared <= 0) return;

        Team team = getTeam(source);
        if (team == null) return;

        for (Entity entity : source.getNearbyEntities(RADIUS, RADIUS, RADIUS)) {
            if (!(entity instanceof Player other)) continue;
            if (!team.hasEntry(other.getName())) continue;

            AttributeInstance maxHealth = other.getAttribute(Attribute.MAX_HEALTH);
            double max = maxHealth != null ? maxHealth.getValue() : 20.0;
            other.setHealth(Math.min(max, other.getHealth() + shared));
            other.getWorld().spawnParticle(Particle.HEART, other.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
        }
    }
}

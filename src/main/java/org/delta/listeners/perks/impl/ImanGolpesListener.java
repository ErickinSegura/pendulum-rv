package org.delta.listeners.perks.impl;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

public class ImanGolpesListener extends BasePerkListener {

    private static final double KNOCKBACK_MULTIPLIER = 2.0;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.IMAN_GOLPES)) return;
        event.setKnockback(event.getKnockback().multiply(KNOCKBACK_MULTIPLIER));
    }
}

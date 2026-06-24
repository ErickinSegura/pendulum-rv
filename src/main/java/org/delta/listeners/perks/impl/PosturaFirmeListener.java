package org.delta.listeners.perks.impl;

import io.papermc.paper.event.entity.EntityKnockbackEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

public class PosturaFirmeListener extends BasePerkListener {

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onKnockback(EntityKnockbackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.POSTURA_FIRME)) return;
        event.setCancelled(true);
    }
}

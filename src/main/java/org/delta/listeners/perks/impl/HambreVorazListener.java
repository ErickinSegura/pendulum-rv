package org.delta.listeners.perks.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

public class HambreVorazListener extends BasePerkListener {

    private static final int EXTRA_LOSS_MULTIPLIER = 2;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFoodChange(FoodLevelChangeEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.HAMBRE_VORAZ)) return;

        int current = player.getFoodLevel();
        int next = event.getFoodLevel();
        if (next >= current) return;

        int loss = (current - next) * EXTRA_LOSS_MULTIPLIER;
        event.setFoodLevel(Math.max(0, current - loss));
    }
}

package org.delta.listeners.perks.impl;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerItemDamageEvent;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

import java.util.Random;

public class ForjaEficienteListener extends BasePerkListener {

    private static final double SAVE_CHANCE = 0.5;

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent event) {
        if (!hasTeamPerk(event.getPlayer(), Perk.FORJA_EFICIENTE)) return;
        if (random.nextDouble() >= SAVE_CHANCE) return;
        event.setCancelled(true);
    }
}

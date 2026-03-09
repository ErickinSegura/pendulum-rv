package org.delta.listeners.perks.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

import java.util.Random;


public class FumbleListener extends BasePerkListener {

    private static final double DROP_CHANCE = 0.20;

    private final Random random = new Random();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!hasTeamPerk(defender, Perk.FUMBLE)) return;
        if (random.nextDouble() >= DROP_CHANCE) return;

        ItemStack held = defender.getInventory().getItemInMainHand();
        if (held.getType().isAir()) return;

        defender.getWorld().dropItemNaturally(defender.getLocation(), held.clone());
        defender.getInventory().setItemInMainHand(null);
    }
}
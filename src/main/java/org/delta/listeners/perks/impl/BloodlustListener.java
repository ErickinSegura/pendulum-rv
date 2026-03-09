package org.delta.listeners.perks.impl;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;


public class BloodlustListener extends BasePerkListener {

    private static final int DURATION = 100; // 5 s
    private static final int SPEED_AMP = 1;
    private static final int REGEN_AMP = 0;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (!hasTeamPerk(killer, Perk.BLOODLUST)) return;

        killer.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION, SPEED_AMP));
        killer.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, DURATION, REGEN_AMP));
    }
}
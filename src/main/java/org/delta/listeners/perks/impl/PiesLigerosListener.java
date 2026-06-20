package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

public class PiesLigerosListener extends BasePerkListener {

    private static final int SPEED_AMP = 0;

    public PiesLigerosListener() {
        Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (hasTeamPerk(player, Perk.PIES_LIGEROS)) {
                    player.addPotionEffect(new PotionEffect(
                            PotionEffectType.SPEED, 100, SPEED_AMP, true, false, false));
                }
            }
        }, 0L, 60L);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.PIES_LIGEROS)) return;
        event.setCancelled(true);
    }
}

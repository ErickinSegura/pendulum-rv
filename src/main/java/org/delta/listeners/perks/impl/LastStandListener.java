package org.delta.listeners.perks.impl;

import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

public class LastStandListener extends BasePerkListener {

    private static final double THRESHOLD = 0.25;
    private static final int DURATION = 100; // 5 s
    private static final int RESISTANCE_AMP = 1;
    private static final int STRENGTH_AMP = 0;

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player defender)) return;
        if (!hasTeamPerk(defender, Perk.LAST_STAND)) return;

        double maxHp = defender.getAttribute(Attribute.MAX_HEALTH).getValue();
        double hpAfterHit = defender.getHealth() - event.getFinalDamage();

        boolean belowThreshold = hpAfterHit > 0 && (hpAfterHit / maxHp) <= THRESHOLD;
        boolean alreadyActive = defender.hasPotionEffect(PotionEffectType.RESISTANCE);

        if (belowThreshold && !alreadyActive) {
            defender.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, DURATION, RESISTANCE_AMP));
            defender.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, DURATION, STRENGTH_AMP));
            org.delta.pendulum.getInstance().getAchievementManager()
                    .unlock(defender, org.delta.managers.achievements.Achievement.ULTIMO_ALIENTO);
        }
    }
}
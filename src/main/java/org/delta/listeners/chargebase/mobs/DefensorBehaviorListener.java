package org.delta.listeners.chargebase.mobs;

import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class DefensorBehaviorListener implements Listener {

    private final Set<UUID> lastStandActive = new HashSet<>();

    @EventHandler
    public void onDefensorDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!entity.getScoreboardTags().contains("defensor_basico") &&
                !entity.getScoreboardTags().contains("defensor_avanzado")) return;

        double healthAfter = entity.getHealth() - event.getFinalDamage();
        double threshold = entity.getAttribute(Attribute.MAX_HEALTH).getValue() * 0.30;

        if (healthAfter <= threshold && !lastStandActive.contains(entity.getUniqueId())) {
            lastStandActive.add(entity.getUniqueId());
            entity.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 200, 1, false, true));
            entity.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 200, 0, false, true));
            entity.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, entity.getLocation().add(0, 1, 0), 30);
        }
    }

    @EventHandler
    public void onDefensorHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof LivingEntity defender)) return;
        if (!defender.getScoreboardTags().contains("defensor_avanzado")) return;
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;

        double reflectDamage = event.getFinalDamage() * 0.25;
        attacker.damage(reflectDamage, defender);
    }

    @EventHandler
    public void onDefensorDeath(EntityDeathEvent event) {
        lastStandActive.remove(event.getEntity().getUniqueId());
    }
}

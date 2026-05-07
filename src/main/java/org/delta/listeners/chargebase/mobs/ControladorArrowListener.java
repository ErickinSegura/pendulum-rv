package org.delta.listeners.chargebase.mobs;

import org.bukkit.entity.Arrow;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class ControladorArrowListener implements Listener {

    private final Random rng = new Random();

    @EventHandler
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) return;
        if (!(arrow.getShooter() instanceof LivingEntity shooter)) return;
        if (!shooter.getScoreboardTags().contains("controlador_basico")) return;
        if (!(event.getHitEntity() instanceof LivingEntity target)) return;

        if (rng.nextBoolean()) {
            target.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 100, 1, false, true));
        } else {
            target.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 60, 0, false, true));
        }
    }
}
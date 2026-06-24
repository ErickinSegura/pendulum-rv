package org.delta.listeners.spawns;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.delta.libs.builders.ItemBuilder;

public class UpgradedMobListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double UPGRADE_CHANCE = 0.01;
    private static final double HEALTH_MULTIPLIER = 2.0;
    private static final double DAMAGE_MULTIPLIER = 2.0;
    private static final double SPEED_MULTIPLIER = 1.3;

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;
        if (random.nextDouble() >= UPGRADE_CHANCE) return;

        LivingEntity entity = event.getEntity();

        multiplyAttribute(entity, Attribute.MAX_HEALTH, HEALTH_MULTIPLIER);
        AttributeInstance health = entity.getAttribute(Attribute.MAX_HEALTH);
        if (health != null) entity.setHealth(health.getValue());

        multiplyAttribute(entity, Attribute.ATTACK_DAMAGE, DAMAGE_MULTIPLIER);
        multiplyAttribute(entity, Attribute.MOVEMENT_SPEED, SPEED_MULTIPLIER);

        entity.setGlowing(true);
        entity.setCustomName(ItemBuilder.format("&6" + prettyName(entity.getType()) + " Mejorado"));
        entity.setCustomNameVisible(true);
    }

    private void multiplyAttribute(LivingEntity entity, Attribute attribute, double factor) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) instance.setBaseValue(instance.getBaseValue() * factor);
    }

    private String prettyName(EntityType type) {
        String raw = type.name().toLowerCase().replace('_', ' ');
        return Character.toUpperCase(raw.charAt(0)) + raw.substring(1);
    }
}

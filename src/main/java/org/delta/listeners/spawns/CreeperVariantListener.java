package org.delta.listeners.spawns;

import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Creeper;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class CreeperVariantListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double MINI_CHANCE = 0.25;
    private static final double MINI_SCALE = 0.5;
    private static final int MINI_EXPLOSION_RADIUS = 6;

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;
        if (!(event.getEntity() instanceof Creeper creeper)) return;

        creeper.setSilent(true);

        if (random.nextDouble() < MINI_CHANCE) {
            AttributeInstance scale = creeper.getAttribute(Attribute.SCALE);
            if (scale != null) scale.setBaseValue(MINI_SCALE);
            creeper.setExplosionRadius(MINI_EXPLOSION_RADIUS);
        }
    }
}

package org.delta.listeners.spawns;

import org.bukkit.World;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.delta.libs.builders.ItemBuilder;

public class EndCreeperListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double CHANCE = 0.10;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;

        LivingEntity entity = event.getEntity();
        if (entity.getType() != EntityType.ENDERMAN) return;

        World world = entity.getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;
        if (random.nextDouble() >= CHANCE) return;

        event.setCancelled(true);
        world.spawn(entity.getLocation(), Creeper.class, creeper -> {
            creeper.setPowered(true);
            creeper.setSilent(true);
            creeper.setCustomName(ItemBuilder.format("&bCreeper Eléctrico"));
            creeper.setCustomNameVisible(true);
        });
    }
}

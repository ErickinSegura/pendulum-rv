package org.delta.listeners.spawns;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.world.EntitiesLoadEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

public class PolarBearListener implements Listener {
    private static final String ARMED_KEY = "pendulum_oso_armado";

    private final PendulumSettings settings = PendulumSettings.getInstance();
    private final pendulum plugin;

    public PolarBearListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (event.getEntity().getType() != EntityType.POLAR_BEAR) return;
        armar(event.getEntity());
    }

    @EventHandler
    public void onEntitiesLoad(EntitiesLoadEvent event) {
        for (Entity entity : event.getEntities()) {
            if (entity.getType() == EntityType.POLAR_BEAR) {
                armar((LivingEntity) entity);
            }
        }
    }

    private void armar(LivingEntity bear) {
        if (settings.getDia() < 5) return;
        if (bear.hasMetadata(ARMED_KEY)) return;
        bear.setMetadata(ARMED_KEY, new FixedMetadataValue(plugin, true));

        new BukkitRunnable() {
            @Override
            public void run() {
                if (bear.isDead() || !bear.isValid()) {
                    cancel();
                    return;
                }

                for (Player player : bear.getWorld().getPlayers()) {
                    if (bear.getLocation().distance(player.getLocation()) <= 4.0) {
                        bear.getWorld().createExplosion(bear.getLocation(), 4f, true, true, bear);
                        bear.remove();
                        cancel();
                        return;
                    }
                }
            }
        }.runTaskTimer(plugin, 0L, 10L);
    }
}

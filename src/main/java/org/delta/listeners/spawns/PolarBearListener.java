package org.delta.listeners.spawns;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

public class PolarBearListener implements Listener {
    private final PendulumSettings settings = PendulumSettings.getInstance();
    private final pendulum plugin;

    public PolarBearListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onSpawn(CreatureSpawnEvent event) {
        if (settings.getDia() < 5 || event.getEntity().getType() != EntityType.POLAR_BEAR) return;

        LivingEntity bear = event.getEntity();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (bear.isDead()) {
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

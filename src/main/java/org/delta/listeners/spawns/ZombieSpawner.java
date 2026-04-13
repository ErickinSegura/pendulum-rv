package org.delta.listeners.spawns;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.delta.customs.mobs.zombie_test.ZombieTest;
import org.delta.pendulum;

public class ZombieSpawner implements Listener {

    private final pendulum plugin;

    public ZombieSpawner(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        new ZombieTest(plugin, event.getEntity().getLocation()).build();
    }
}
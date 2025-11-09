package org.delta.listeners.players;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.delta.libs.death.LifeManager;

public class LifeListener implements Listener {

    private final LifeManager lifeManager;

    public LifeListener(LifeManager lifeManager) {
        this.lifeManager = lifeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        lifeManager.initializePlayer(event.getPlayer());
        lifeManager.updateHealthDisplay(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        lifeManager.removeLife(event.getPlayer());
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        lifeManager.updateHealthDisplay(event.getPlayer());
    }
}
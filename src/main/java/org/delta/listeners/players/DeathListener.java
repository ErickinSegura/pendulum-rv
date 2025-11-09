package org.delta.listeners.players;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.delta.libs.death.ClockEvents;
import org.delta.libs.death.DeathEvents;
import org.delta.libs.death.LifeManager;

public class DeathListener implements Listener {
    private final LifeManager lifeManager;


    public DeathListener(LifeManager lifeManager) {
        this.lifeManager = lifeManager;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = getDeathLocation(player);
        int currentLives = lifeManager.getLives(event.getPlayer());
        if (currentLives == 0) {
            DeathEvents deathEvents = new DeathEvents();
            deathEvents.handlePlayerDeath(player, location, event);
        }
        else {
            ClockEvents.handlePlayerClockLoss(player, currentLives, location, event);
        }
    }

    private Location getDeathLocation(Player player) {
        Location location = player.getLocation();
        if (player.getLastDamageCause().getCause() == org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
            location.setY(location.getWorld().getMinHeight() + 1);
        }
        return location;
    }
}

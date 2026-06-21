package org.delta.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.delta.database.repositories.PlayerRepository;
import org.delta.managers.death.LifeManager;
import org.delta.pendulum;

public class JoinLeaveListener implements Listener {

    private final LifeManager lifeManager;

    public JoinLeaveListener(LifeManager lifeManager) {
        this.lifeManager = lifeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        var db = pendulum.getInstance().getDatabaseManager();
        if (db == null || !db.isConnected()) {
            return;
        }

        var dataPlayer = new PlayerRepository.PlayerData(
                player.getUniqueId(),
                player.getName(),
                lifeManager.getLives(player),
                player.getLocation().getX(),
                player.getLocation().getY(),
                player.getLocation().getZ(),
                player.getWorld().getName()
        );

        db.players().upsert(player.getUniqueId(), dataPlayer)
                .exceptionally(err -> {
                    pendulum.getInstance().getLogger().severe(
                            "Error al registrar jugador " + player.getName() + ": " + err.getMessage()
                    );
                    return null;
                });
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();

        var db = pendulum.getInstance().getDatabaseManager();
        if (db == null || !db.isConnected()) {
            return;
        }

        var dataPlayer = new PlayerRepository.PlayerData(
                player.getUniqueId(),
                player.getName(),
                lifeManager.getLives(event.getPlayer()),
                x, y, z,
                player.getWorld().getName()
        );

        db.players().update(player.getUniqueId(), dataPlayer)
                .exceptionally(err -> {
                    pendulum.getInstance().getLogger().severe(
                            "Error al registrar jugador " + player.getName() + ": " + err.getMessage()
                    );
                    return null;
                });

    }
}

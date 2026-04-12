package org.delta.listeners.death;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Team;
import org.delta.database.repositories.DeathRepository;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.DeathEvents;
import org.delta.managers.death.LifeManager;
import org.delta.pendulum;

public class DeathListener implements Listener {

    private final LifeManager lifeManager;
    private final DeathRepository deathRepo;

    public DeathListener(LifeManager lifeManager) {
        this.lifeManager = lifeManager;
        this.deathRepo = new DeathRepository(pendulum.getInstance().getDatabaseManager());
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        Location location = getDeathLocation(player);
        int currentLives = lifeManager.getLives(event.getPlayer());

        if (currentLives == 0) {
            DeathEvents deathEvents = new DeathEvents();
            deathEvents.handlePlayerDeath(player, location, event);
        } else {
            ClockEvents.handlePlayerClockLoss(player, currentLives, location, event);
        }

        syncDeath(event, location);
    }

    private void syncDeath(PlayerDeathEvent event, Location location) {
        Player victim = event.getEntity();

        String cause = "UNKNOWN";
        if (victim.getLastDamageCause() != null) {
            cause = victim.getLastDamageCause().getCause().name();
        }

        String killerUuid = null;
        String killerName = null;
        Long killerTeamId = null;

        Entity killerEntity = victim.getKiller();
        if (killerEntity instanceof Player killer) {
            killerUuid   = killer.getUniqueId().toString();
            killerName   = killer.getName();
            killerTeamId = resolveTeamId(killer.getName());
        }

        String deathMessage = null;
        if (event.deathMessage() != null) {
            deathMessage = PlainTextComponentSerializer.plainText()
                    .serialize(event.deathMessage());
        }

        DeathRepository.DeathData data = new DeathRepository.DeathData(
                victim.getUniqueId().toString(),
                victim.getName(),
                cause,
                deathMessage,
                killerUuid,
                killerName,
                resolveTeamId(victim.getName()),
                killerTeamId,
                location.getX(),
                location.getY(),
                location.getZ(),
                location.getWorld() != null ? location.getWorld().getName() : null
        );

        deathRepo.recordDeath(data)
                .exceptionally(e -> {
                    pendulum.getInstance().getLogger()
                            .warning("[DeathSync] Error al registrar muerte de "
                                    + victim.getName() + ": " + e.getMessage());
                    return null;
                });
    }

    private Location getDeathLocation(Player player) {
        Location location = player.getLocation();
        if (player.getLastDamageCause().getCause() ==
                org.bukkit.event.entity.EntityDamageEvent.DamageCause.VOID) {
            location.setY(location.getWorld().getMinHeight() + 1);
        }
        return location;
    }

    private Long resolveTeamId(String playerName) {
        Team scoreboardTeam = BingoProgressManager.getInstance().getPlayerTeam(playerName);
        if (scoreboardTeam == null) return null;

        if (!pendulum.getInstance().getDatabaseManager().isConnected()) return null;

        try (var conn = pendulum.getInstance().getDatabaseManager().getConnection();
             var stmt = conn.prepareStatement("SELECT id FROM teams WHERE name = ? LIMIT 1")) {
            stmt.setString(1, scoreboardTeam.getName());
            var rs = stmt.executeQuery();
            if (rs.next()) return rs.getLong("id");
        } catch (Exception e) {
            pendulum.getInstance().getLogger()
                    .warning("[DeathSync] No se pudo resolver team para: " + playerName);
        }
        return null;
    }
}
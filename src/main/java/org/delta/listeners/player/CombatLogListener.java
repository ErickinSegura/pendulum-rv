package org.delta.listeners.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.scoreboard.Team;
import org.delta.database.repositories.DeathRepository;
import org.delta.libs.MessageUtils;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.CombatTagManager;
import org.delta.managers.death.LifeManager;
import org.delta.pendulum;

import java.util.UUID;

public class CombatLogListener implements Listener {

    private final LifeManager lifeManager;
    private final CombatTagManager combatTagManager;
    private final DeathRepository deathRepo;

    public CombatLogListener(LifeManager lifeManager, CombatTagManager combatTagManager) {
        this.lifeManager = lifeManager;
        this.combatTagManager = combatTagManager;
        this.deathRepo = new DeathRepository(pendulum.getInstance().getDatabaseManager());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPvpDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) return;
        if (attacker.getUniqueId().equals(victim.getUniqueId())) return;

        if (combatTagManager.tag(attacker)) notifyEnteredCombat(attacker);
        if (combatTagManager.tag(victim, attacker)) notifyEnteredCombat(victim);
    }

    private void notifyEnteredCombat(Player player) {
        player.sendMessage(MessageUtils.color(
                "&c⚔ Entraste en combate. No te desconectes o perderás un reloj."));
    }

    private Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) return player;
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            if (source instanceof Player shooter) return shooter;
        }
        return null;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (!combatTagManager.isTagged(player)) return;

        if (ClockEvents.isPendingBan(player)) {
            combatTagManager.clear(player);
            return;
        }

        UUID killerUuid = combatTagManager.getLastAttackerUuid(player);
        String killerName = combatTagManager.getLastAttackerName(player);
        combatTagManager.clear(player);

        int livesBefore = lifeManager.getLives(player);
        if (livesBefore <= 0) return;

        lifeManager.removeLife(player);
        int currentLives = lifeManager.getLives(player);

        if (currentLives <= 0) {
            ClockEvents.handleCombatLogElimination(player);
        } else {
            ClockEvents.handleCombatLogClockLoss(player, currentLives);
        }
        recordCombatLog(player, killerUuid, killerName);
    }

    private void recordCombatLog(Player player, UUID killerUuid, String killerName) {
        var loc = player.getLocation();

        DeathRepository.DeathData data = new DeathRepository.DeathData(
                player.getUniqueId().toString(),
                player.getName(),
                "COMBAT_LOG",
                player.getName() + " hizo combat log",
                killerUuid != null ? killerUuid.toString() : null,
                killerName,
                resolveTeamId(player.getName()),
                killerName != null ? resolveTeamId(killerName) : null,
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getWorld() != null ? loc.getWorld().getName() : null
        );

        deathRepo.recordDeath(data)
                .exceptionally(e -> {
                    pendulum.getInstance().getLogger()
                            .warning("[CombatLog] Error al registrar combat log de "
                                    + player.getName() + ": " + e.getMessage());
                    return null;
                });
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
                    .warning("[CombatLog] No se pudo resolver team para: " + playerName);
        }
        return null;
    }
}

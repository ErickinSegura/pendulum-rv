package org.delta.listeners;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.delta.libs.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RetoListener implements Listener {
    // Singleton para acceder al listener desde otros comandos
    private static RetoListener instance;

    // Set para llevar registro de quiénes ya completaron el reto actual
    private Set<UUID> jugadoresCompletados = new HashSet<>();

    public RetoListener() {
        instance = this;
    }

    public static RetoListener getInstance() {
        return instance;
    }

    // Método para resetear el registro cuando cambie el reto
    public void resetearCompletados() {
        jugadoresCompletados.clear();
    }

    // Método para resetear un jugador específico
    public void resetearJugadorCompletado(UUID uuid) {
        jugadoresCompletados.remove(uuid);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        Player killer = event.getEntity().getKiller();
        Reto retoActual = PendulumSettings.getInstance().getRetoActual();
        if (retoActual instanceof RetoMobs) {
            RetoMobs retoMobs = (RetoMobs) retoActual;
            if (event.getEntity().getType() == retoMobs.getMobType()) {
                String scoreboardName = retoMobs.getScoreboardName();
                Objective obj = killer.getScoreboard().getObjective(scoreboardName);
                if (obj == null) {
                    obj = killer.getScoreboard().registerNewObjective(
                            scoreboardName, "dummy", "Matar " + retoMobs.getMobType().toString()
                    );
                }
                int current = obj.getScore(killer.getName()).getScore();
                obj.getScore(killer.getName()).setScore(current + 1);

                // Mensaje de progreso
                String progreso = retoMobs.obtenerProgreso(killer);
                killer.sendMessage("§8[§dReto§8] §7Progreso: §d" + progreso);

                // Verificar si completó (solo mostrar mensaje si no lo había completado antes)
                if (retoMobs.verificarCompletado(killer) && !jugadoresCompletados.contains(killer.getUniqueId())) {
                    jugadoresCompletados.add(killer.getUniqueId());
                    killer.sendMessage("§8[§dReto§8] §a¡Has completado el reto! Usa §d/pdl entregar");
                    killer.playSound(killer.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Reto retoActual = PendulumSettings.getInstance().getRetoActual();
        if (retoActual instanceof RetoMinar retoMinar) {
            if (event.getBlock().getType() == retoMinar.getMaterial()) {
                String scoreboardName = retoMinar.getScoreboardName();
                Objective obj = player.getScoreboard().getObjective(scoreboardName);
                if (obj == null) {
                    obj = player.getScoreboard().registerNewObjective(
                            scoreboardName, "dummy", "Minar " + retoMinar.getMaterial().toString()
                    );
                }
                int current = obj.getScore(player.getName()).getScore();
                obj.getScore(player.getName()).setScore(current + 1);

                // Mensaje de progreso (cada 10 bloques para no spamear)
                if ((current + 1) % 10 == 0 || (retoMinar.verificarCompletado(player) && !jugadoresCompletados.contains(player.getUniqueId()))) {
                    String progreso = retoMinar.obtenerProgreso(player);
                    player.sendMessage("§8[§dReto§8] §7Progreso: §d" + progreso);
                }

                // Verificar si completó (solo mostrar mensaje si no lo había completado antes)
                if (retoMinar.verificarCompletado(player) && !jugadoresCompletados.contains(player.getUniqueId())) {
                    jugadoresCompletados.add(player.getUniqueId());
                    player.sendMessage("§8[§dReto§8] §a¡Has completado el reto! Usa §d/pdl entregar");
                    player.playSound(player.getLocation(), org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
                }
            }
        }
    }
}
package org.delta.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.scheduler.BukkitTask;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Set;

public class BedListener implements Listener {

    private final Set<Player> jugadoresDurmiendo = new HashSet<>();
    private boolean nochePasando = false;
    private BukkitTask tareaDeNoche = null;

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        int jugadoresNecesarios = PendulumSettings.getInstance().getJugadoresNoche();

        if (jugadoresNecesarios == 0) {
            event.setCancelled(true);

            world.spawnParticle(
                    Particle.EXPLOSION,
                    event.getBed().getLocation().add(0.5, 1, 0.5),
                    1
            );

            player.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);

            return;
        }

        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            jugadoresDurmiendo.add(player);

            if (jugadoresNecesarios == 1) {
                if (!nochePasando) {
                    nochePasando = true;
                    Bukkit.broadcastMessage("§e" + player.getName() + " §7está durmiendo...");

                    tareaDeNoche = Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), () -> {
                        world.setTime(0);
                        world.setStorm(false);
                        world.setThundering(false);
                        Bukkit.broadcastMessage("Ha pasado la noche...");
                        jugadoresDurmiendo.clear();
                        nochePasando = false;
                        tareaDeNoche = null;
                    }, 100L);
                }
                return;
            }

            if (jugadoresDurmiendo.size() >= jugadoresNecesarios && !nochePasando) {
                nochePasando = true;

                tareaDeNoche = Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), () -> {
                    world.setTime(0);
                    world.setStorm(false);
                    world.setThundering(false);
                    Bukkit.broadcastMessage("Ha pasado la noche...");
                    jugadoresDurmiendo.clear();
                    nochePasando = false;
                    tareaDeNoche = null;
                }, 100L);
            } else if (!nochePasando) {
                Bukkit.broadcastMessage("§e" + player.getName() + " §7está durmiendo. §e(" +
                        jugadoresDurmiendo.size() + "/" + jugadoresNecesarios + ")");
            }
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        jugadoresDurmiendo.remove(event.getPlayer());

        if (tareaDeNoche != null && !tareaDeNoche.isCancelled()) {
            tareaDeNoche.cancel();
            tareaDeNoche = null;
            nochePasando = false;
        }
    }
}
package org.delta.managers.reto;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.delta.libs.PendulumSettings;
import org.delta.libs.reto.Reto;
import org.delta.libs.reto.RetoItem;
import org.delta.listeners.player.RetoListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RetoManager {
    private static RetoManager instance;

    private RetoManager() {}

    public static RetoManager getInstance() {
        if (instance == null) {
            instance = new RetoManager();
        }
        return instance;
    }

    public boolean yaEntrego(Player player) {
        Objective retoObj = player.getScoreboard().getObjective("reto");
        if (retoObj == null) {
            retoObj = player.getScoreboard().registerNewObjective("reto", "dummy", "Reto Completado");
        }

        Score score = retoObj.getScore(player.getName());
        return score.getScore() > 0;
    }

    public void marcarComoEntregado(Player player) {
        Objective retoObj = player.getScoreboard().getObjective("reto");
        if (retoObj == null) {
            retoObj = player.getScoreboard().registerNewObjective("reto", "dummy", "Reto Completado");
        }

        Score score = retoObj.getScore(player.getName());
        score.setScore(1);
    }

    public boolean verificarCompletado(Player player) {
        Reto reto = PendulumSettings.getInstance().getRetoActual();
        if (reto == null) return false;

        return reto.verificarCompletado(player);
    }

    public String obtenerProgreso(Player player) {
        Reto reto = PendulumSettings.getInstance().getRetoActual();
        if (reto == null) return "0%";

        return reto.obtenerProgreso(player);
    }

    public boolean consumirItems(Player player, RetoItem retoItem) {
        Material material = retoItem.getMaterial();
        int cantidadNecesaria = retoItem.getCantidad();
        int cantidadEncontrada = 0;

        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                cantidadEncontrada += item.getAmount();
            }
        }

        if (cantidadEncontrada < cantidadNecesaria) {
            return false;
        }

        int restante = cantidadNecesaria;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            org.bukkit.inventory.ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= restante) {
                    restante -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - restante);
                    restante = 0;
                }

                if (restante == 0) break;
            }
        }

        return true;
    }

    public void resetearJugador(Player player) {
        Objective retoObj = player.getScoreboard().getObjective("reto");
        if (retoObj != null) {
            retoObj.getScore(player.getName()).setScore(0);
        }

        RetoListener listener = RetoListener.getInstance();
        if (listener != null) {
            listener.resetearJugadorCompletado(player.getUniqueId());
        }
    }

    public void resetearJugadorOffline(OfflinePlayer player) {
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective mainRetoObj = mainScoreboard.getObjective("reto");

        if (mainRetoObj != null) {
            mainRetoObj.getScore(player.getName()).setScore(0);
        }

        RetoListener listener = RetoListener.getInstance();
        if (listener != null) {
            listener.resetearJugadorCompletado(player.getUniqueId());
        }
    }

    public ResetResult resetearTodos() {
        int countOnline = 0;
        int countOffline = 0;

        for (Player online : Bukkit.getOnlinePlayers()) {
            Objective retoObj = online.getScoreboard().getObjective("reto");
            if (retoObj != null) {
                retoObj.getScore(online.getName()).setScore(0);
                countOnline++;
            }
        }

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective mainRetoObj = mainScoreboard.getObjective("reto");

        if (mainRetoObj != null) {
            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (!offline.isOnline() && mainRetoObj.getScore(offline.getName()).isScoreSet()) {
                    mainRetoObj.getScore(offline.getName()).setScore(0);
                    countOffline++;
                }
            }
        }

        RetoListener listener = RetoListener.getInstance();
        if (listener != null) {
            listener.resetearCompletados();
        }

        Bukkit.getLogger().info("[Pendulum] Retos reseteados para todos los jugadores.");

        return new ResetResult(countOnline, countOffline);
    }


    public static class ResetResult {
        private final int online;
        private final int offline;

        public ResetResult(int online, int offline) {
            this.online = online;
            this.offline = offline;
        }

        public int getOnline() { return online; }
        public int getOffline() { return offline; }
        public int getTotal() { return online + offline; }
    }

    public Set<String> obtenerNombresCompletados() {
        Set<String> nombres = new HashSet<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (yaEntrego(online)) {
                nombres.add(online.getName());
            }
        }

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective mainRetoObj = mainScoreboard.getObjective("reto");

        if (mainRetoObj != null) {
            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (!offline.isOnline() && offline.getName() != null
                        && mainRetoObj.getScore(offline.getName()).getScore() > 0) {
                    nombres.add(offline.getName());
                }
            }
        }

        return nombres;
    }

    public List<String> obtenerJugadoresCompletados() {
        List<String> completados = new ArrayList<>();

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (yaEntrego(online)) {
                completados.add(online.getName() + " §a(Online)");
            }
        }

        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective mainRetoObj = mainScoreboard.getObjective("reto");

        if (mainRetoObj != null) {
            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (!offline.isOnline() && offline.hasPlayedBefore()) {
                    Score score = mainRetoObj.getScore(offline.getName());
                    if (score.getScore() > 0) {
                        completados.add(offline.getName() + " §7(Offline)");
                    }
                }
            }
        }

        return completados;
    }
}
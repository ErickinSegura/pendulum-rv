package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.delta.libs.MessageUtils;
import org.delta.listeners.RetoListener;

public class ResetRetoCommand implements SubCommand {

    @Override
    public String getName() {
        return "reset_reto";
    }

    @Override
    public void execute(Player player, String[] args) {
        // /pdl reset_reto - resetear todos (online y offline)
        // /pdl reset_reto <jugador> - resetear específico

        if (args.length == 1) {
            resetearTodos(player);
        } else {
            String targetName = args[1];
            resetearJugador(targetName, player);
        }
    }

    private void resetearTodos(Player executor) {
        int countOnline = 0;
        int countOffline = 0;

        // Resetear jugadores en línea
        for (Player online : Bukkit.getOnlinePlayers()) {
            Objective retoObj = online.getScoreboard().getObjective("reto");
            if (retoObj != null) {
                retoObj.getScore(online.getName()).setScore(0);
                countOnline++;
            }
        }

        // Resetear jugadores offline
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

        // NUEVO: Resetear el registro de completados en el listener
        RetoListener listener = RetoListener.getInstance();
        if (listener != null) {
            listener.resetearCompletados();
        }

        int totalCount = countOnline + countOffline;

        // Mensaje al ejecutor
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &6&lRESET DE RETOS &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&7Se han reseteado los retos de &d" + totalCount + " jugadores&7."));
        executor.sendMessage(MessageUtils.color("&8  ├ &aEn línea: &f" + countOnline));
        executor.sendMessage(MessageUtils.color("&8  └ &7Offline: &f" + countOffline));
        executor.sendMessage("");
        executor.playSound(executor.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

        // Anuncio global solo a jugadores en línea
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(executor)) {
                online.sendMessage("");
                online.sendMessage(MessageUtils.color(
                        "&8[&6&l!&8] &d" + executor.getName() + " &7ha reseteado los retos de todos los jugadores."
                ));
                online.sendMessage(MessageUtils.color("&7¡Prepárate para un nuevo desafío!"));
                online.sendMessage("");
                online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
            }
        }
    }

    private void resetearJugador(String targetName, Player executor) {
        // Intentar encontrar al jugador (online o offline)
        Player targetOnline = Bukkit.getPlayer(targetName);
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);

        boolean reseteado = false;

        // Si está en línea, usar su scoreboard personal
        if (targetOnline != null) {
            Objective retoObj = targetOnline.getScoreboard().getObjective("reto");
            if (retoObj != null) {
                retoObj.getScore(targetOnline.getName()).setScore(0);
                reseteado = true;
            }

            // NUEVO: Resetear el registro de completado para este jugador específico
            RetoListener listener = RetoListener.getInstance();
            if (listener != null) {
                listener.resetearJugadorCompletado(targetOnline.getUniqueId());
            }
        }
        // Si está offline, usar el scoreboard principal
        else if (targetOffline.hasPlayedBefore()) {
            Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Objective mainRetoObj = mainScoreboard.getObjective("reto");

            if (mainRetoObj != null) {
                mainRetoObj.getScore(targetName).setScore(0);
                reseteado = true;
            }

            // NUEVO: Resetear el registro de completado para el jugador offline
            RetoListener listener = RetoListener.getInstance();
            if (listener != null) {
                listener.resetearJugadorCompletado(targetOffline.getUniqueId());
            }
        } else {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador &e" + targetName + " &cnunca ha jugado en el servidor."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        if (!reseteado) {
            executor.sendMessage(MessageUtils.color("&c✘ No se pudo resetear el reto de &e" + targetName + "&c."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Mensaje al ejecutor
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &6&lRESET DE RETO &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&7Se ha reseteado el reto de &d" + targetName + "&7."));
        executor.sendMessage(MessageUtils.color("&8  └ Estado: " + (targetOnline != null ? "&aEn línea" : "&7Offline")));
        executor.sendMessage("");
        executor.playSound(executor.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);

        // Notificar al jugador si está en línea
        if (targetOnline != null && !targetOnline.equals(executor)) {
            targetOnline.sendMessage("");
            targetOnline.sendMessage(MessageUtils.color("&8[&6&l!&8] &d" + executor.getName() + " &7ha reseteado tu reto."));
            targetOnline.sendMessage(MessageUtils.color("&7¡Prepárate para un nuevo desafío!"));
            targetOnline.sendMessage("");
            targetOnline.playSound(targetOnline.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
        }
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {

    }
}
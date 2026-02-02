package org.delta.managers.reto;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.reto.Reto;

public class RetoNotificationManager {
    private static RetoNotificationManager instance;

    private RetoNotificationManager() {}

    public static RetoNotificationManager getInstance() {
        if (instance == null) {
            instance = new RetoNotificationManager();
        }
        return instance;
    }

    public void enviarMensajeNoActivo(Player player) {
        player.sendMessage(MessageUtils.color("&c✘ No hay un reto activo en este momento."));
        RetoEffectsManager.getInstance().reproducirSonidoError(player);
    }

    public void enviarMensajeYaEntregado(Player player) {
        player.sendMessage(MessageUtils.color("&c✘ Ya has entregado tu reto."));
        player.sendMessage(MessageUtils.color("&7Espera al siguiente bloque para un nuevo reto."));
        RetoEffectsManager.getInstance().reproducirSonidoError(player);
    }

    public void enviarMensajeNoCompletado(Player player, String progreso) {
        player.sendMessage(MessageUtils.color("&c✘ Aún no has completado el reto."));
        player.sendMessage(MessageUtils.color("&7Progreso actual: &d" + progreso));
        RetoEffectsManager.getInstance().reproducirSonidoError(player);
    }

    public void enviarMensajeItemsInsuficientes(Player player) {
        player.sendMessage(MessageUtils.color("&c✘ No tienes suficientes items en tu inventario."));
        RetoEffectsManager.getInstance().reproducirSonidoError(player);
    }

    public void enviarMensajeCompletado(Player player, String tituloReto, String premio) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &a&l✔ RETO COMPLETADO &6&l&k|||&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7¡Felicidades! Has completado el reto:"));
        player.sendMessage(MessageUtils.color("&8└ &d" + tituloReto));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Recompensa recibida:"));
        player.sendMessage(MessageUtils.color("&8└ &a" + premio));
        player.sendMessage("");
    }

    public void anunciarEntrega(Player jugador, Reto reto) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage("");
            online.sendMessage(MessageUtils.color("&8&m                                                    "));
            online.sendMessage(MessageUtils.color(
                    "&8[&6&l!&8] &d" + jugador.getName() + " &7ha completado el reto: &a" + reto.getTitulo()
            ));
            online.sendMessage(MessageUtils.color("&8&m                                                    "));
            online.sendMessage("");

            // Sonido para todos excepto el que entregó
            if (!online.equals(jugador)) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }
        }
    }

    public void enviarMensajeResetExitoso(Player executor, int total, int online, int offline) {
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &6&lRESET DE RETOS &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&7Se han reseteado los retos de &d" + total + " jugadores&7."));
        executor.sendMessage(MessageUtils.color("&8  ├ &aEn línea: &f" + online));
        executor.sendMessage(MessageUtils.color("&8  └ &7Offline: &f" + offline));
        executor.sendMessage("");
        RetoEffectsManager.getInstance().reproducirSonidoExito(executor);
    }

    public void anunciarResetGlobal(Player executor) {
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

    public void enviarMensajeResetIndividual(Player executor, String targetName, boolean isOnline) {
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &6&lRESET DE RETO &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&7Se ha reseteado el reto de &d" + targetName + "&7."));
        executor.sendMessage(MessageUtils.color("&8  └ Estado: " + (isOnline ? "&aEn línea" : "&7Offline")));
        executor.sendMessage("");
        RetoEffectsManager.getInstance().reproducirSonidoExito(executor);
    }


    public void notificarResetIndividual(Player target, Player executor) {
        if (target != null && !target.equals(executor)) {
            target.sendMessage("");
            target.sendMessage(MessageUtils.color("&8[&6&l!&8] &d" + executor.getName() + " &7ha reseteado tu reto."));
            target.sendMessage(MessageUtils.color("&7¡Prepárate para un nuevo desafío!"));
            target.sendMessage("");
            target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.8f, 1.2f);
        }
    }

    public void enviarMensajeJugadorNoEncontrado(Player executor, String targetName) {
        executor.sendMessage(MessageUtils.color("&c✘ El jugador &e" + targetName + " &cnunca ha jugado en el servidor."));
        RetoEffectsManager.getInstance().reproducirSonidoError(executor);
    }

    public void enviarMensajeErrorReset(Player executor, String targetName) {
        executor.sendMessage(MessageUtils.color("&c✘ No se pudo resetear el reto de &e" + targetName + "&c."));
        RetoEffectsManager.getInstance().reproducirSonidoError(executor);
    }


    public void anunciarRuleta(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &d&lGIRANDO LA RULETA&r &6&l&k|||&r &8&l≪"));
        player.sendMessage("");

        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(MessageUtils.color("&8[&6&l!&8] &d" + player.getName() + " &7está girando la ruleta de retos y castigos..."));
            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 0.8f);
        }
    }

    public void anunciarResultadoRuleta(Player player, String tituloReto, String castigo) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &a&lRESULTADO&r &6&l&k|||&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7El nuevo reto es:"));
        player.sendMessage(MessageUtils.color("&8└ &d&l" + tituloReto));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7El castigo será:"));
        player.sendMessage(MessageUtils.color("&8└ &c&l" + castigo));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &eRetos reseteados para todos los jugadores"));
        player.sendMessage("");

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.sendMessage("");
                online.sendMessage(MessageUtils.color("&8&m                                                    "));
                online.sendMessage(MessageUtils.color(
                        "&8[&6&l!&8] &7Nuevo reto: &d" + tituloReto
                ));
                online.sendMessage(MessageUtils.color(
                        "&8[&6&l!&8] &7Castigo: &c" + castigo
                ));
                online.sendMessage(MessageUtils.color("&7¡Los retos han sido reseteados para todos!"));
                online.sendMessage(MessageUtils.color("&8&m                                                    "));
                online.sendMessage("");
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
            }
        }
    }
}
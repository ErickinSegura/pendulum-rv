package org.delta.listeners.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

public class CommandVisibilityListener implements Listener {

    private static final Set<String> COMANDOS_VISIBLES = Set.of(
            "tell",
            "w",
            "msg",
            "whisper",
            "me",
            "pendulum",
            "pdl"
    );

    private static final Set<String> COMANDOS_OCULTOS_PERMITIDOS = Set.of(
            "ptl"
    );

    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        Player player = event.getPlayer();
        if (esOp(player)) return;

        event.getCommands().removeIf(comando -> !COMANDOS_VISIBLES.contains(sinNamespace(comando)));
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        if (esOp(player)) return;

        String comando = sinNamespace(etiquetaComando(event.getMessage()));
        if (!COMANDOS_VISIBLES.contains(comando) && !COMANDOS_OCULTOS_PERMITIDOS.contains(comando)) {
            event.setCancelled(true);
            player.sendMessage(MessageUtils.color("&c✘ Ese comando no está disponible."));
        }
    }

    private String etiquetaComando(String mensaje) {
        String sinBarra = mensaje.startsWith("/") ? mensaje.substring(1) : mensaje;
        int espacio = sinBarra.indexOf(' ');
        if (espacio >= 0) sinBarra = sinBarra.substring(0, espacio);
        return sinBarra.toLowerCase(Locale.ROOT);
    }

    private String sinNamespace(String comando) {
        int separador = comando.indexOf(':');
        return separador >= 0 ? comando.substring(separador + 1) : comando;
    }

    private boolean esOp(Player player) {
        return player.hasPermission("pendulum.admin") ||
                Arrays.asList(PendulumSettings.getInstance().getOp()).contains(player.getName());
    }
}

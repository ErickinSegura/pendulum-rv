package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.libs.Reto;

public class RetoCommand implements SubCommand {
    @Override
    public String getName() {
        return "reto";
    }

    @Override
    public void execute(Player player, String[] args) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto reto = settings.getRetoActual();

        if (reto == null) {
            player.sendMessage(MessageUtils.color("&c¡No hay un reto activo!"));
            return;
        }

        boolean retoCumplido = reto.verificarCompletado(player);

        // Sonido inicial
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        // Cabecera
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lDETALLES DEL RETO&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");

        // Información principal
        sendRetoDetail(player, "⚔", "Desafío", reto.getTitulo());
        sendRetoDetail(player, "✨", "Recompensa", settings.getPremio());
        sendRetoDetail(player, "☠", "Castigo", settings.getCastigo());

        // Progreso
        player.sendMessage("");
        String progreso = reto.obtenerProgreso(player);
        player.sendMessage(MessageUtils.color("&8└ 📊 &7Progreso: &d" + progreso));

        // Estado
        player.sendMessage("");
        if (retoCumplido) {
            player.sendMessage(MessageUtils.color("&8└ &7Estado: &a✔ Completado"));
            player.sendMessage(MessageUtils.color("&8   &7¡Felicitaciones por completar el reto!"));
        } else {
            player.sendMessage(MessageUtils.color("&8└ &7Estado: &c✘ Pendiente"));
            player.sendMessage(MessageUtils.color("&8   &7Usa &d/pdl entregar &7cuando completes el reto"));
        }

        // Descripción del reto
        if (!retoCumplido) {
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8└ &7Objetivo:"));
            player.sendMessage(MessageUtils.color("&8   &d" + reto.getDescripcion()));
        }

        // Sonido final
        float pitch = retoCumplido ? 1.5f : 1.0f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, pitch);
    }

    private void sendRetoDetail(Player player, String icon, String label, String value) {
        player.sendMessage(MessageUtils.color("&8└ " + icon + " &7" + label + ": &d" + value));
    }

    private String getCurrentTime() {
        java.time.LocalTime now = java.time.LocalTime.now();
        return String.format("%02d:%02d", now.getHour(), now.getMinute());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }
}
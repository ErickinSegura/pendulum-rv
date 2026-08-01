package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

public class InfoCommand implements SubCommand {

    private final PendulumSettings settings = PendulumSettings.getInstance();

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void execute(Player player, String[] args) {

        Team team = player.getScoreboard().getEntryTeam(player.getName());
        ChargeBaseManager charge = pendulum.getInstance().getChargeBaseManager();
        String equipo = (team != null) ? team.getPrefix() : "&cSin equipo";
        int dia = PendulumSettings.getInstance().getDia();

        boolean retoCumplido = false;
        Objective retoObjective = player.getScoreboard().getObjective("reto");

        if (retoObjective != null) {
            Score retoScore = retoObjective.getScore(player.getName());
            retoCumplido = retoScore.getScore() > 0;
        }

        int playersOnline = Bukkit.getServer().getOnlinePlayers().size();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lESTADÍSTICAS DEL SERVIDOR&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");

        // Información principal
        sendStatistic(player, "Jugadores Online", "&d" + playersOnline + " &7conectados");
        sendStatistic(player, "Tu Equipo", equipo);
        sendStatistic(player, "Estado del Reto", getRetoStatus(retoCumplido));
        sendStatistic(player, "Día del servidor", "&e" + dia);

        int bloque = settings.getBloque();
        int inicioBloque = (bloque - 1) * 5 + 1;
        int finBloque = bloque * 5;
        int diaEnBloque = ((dia - 1) % 5) + 1;
        sendStatistic(player, "Bloque", "&e" + bloque + " &7(días &e" + inicioBloque + "&7-&e" + finBloque + "&7, día &e" + diaEnBloque + "&7/&e5&7)");

        // Información Zona de Carga
        if (settings.getDia() >= 5) {
            if (charge.isActive()) {
                var zone = charge.getActiveZone();
                var center = zone.getCenter();
                sendStatistic(player, "Base de Carga", "&aActiva en &e" + (int)center.getX() + ", " + (int)center.getZ());
                sendStatistic(player, "Radio actual", "&e" + String.format("%.1f", zone.getCurrentRadius()));
                sendStatistic(player, "Tiempo restante", "&e" + (charge.getRemainingTicks() / 20) / 60 + "m " + (charge.getRemainingTicks() / 20) % 60 + "s");
            } else {
                sendStatistic(player, "Base de Carga", "&cNo hay ninguna activa");
                sendStatistic(player, "Proxima", "&c" + charge.getTimeUntilNext());
            }
        }


        // Sonido de finalización
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);

        Bukkit.dispatchCommand(player, "ptl info");
    }

    private void sendStatistic(Player player, String label, String value) {
        player.sendMessage(MessageUtils.color("&8└ &7" + label + ": " + value));
    }

    private String getRetoStatus(boolean completed) {
        return completed ? "&a✔ Completado" : "&c✘ Pendiente";
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}
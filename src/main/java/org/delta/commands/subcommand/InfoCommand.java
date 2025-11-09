package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;

public class InfoCommand implements SubCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void execute(Player player, String[] args) {
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        String equipo = (team != null) ? team.getPrefix() : "&cSin equipo";

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

        // Sonido de finalización
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
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
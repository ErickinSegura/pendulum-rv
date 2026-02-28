package org.delta.listeners.perks;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Team;
import org.delta.managers.perks.PerkManager;

public class PerkListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        var player = event.getPlayer();
        Team team = getTeam(player);
        if (team == null) return;

        PerkManager.getInstance().applyPerksToPlayer(player, team.getName());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        var player = event.getPlayer();
        Team team = getTeam(player);
        if (team == null) return;

        PerkManager.getInstance().removePerksFromPlayer(player, team.getName());
    }

    private Team getTeam(org.bukkit.entity.Player player) {
        return org.bukkit.Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getEntryTeam(player.getName());
    }
}
package org.delta.listeners.perks;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scoreboard.Team;
import org.delta.managers.perks.Perk;
import org.delta.managers.perks.PerkManager;

import java.util.Set;

public abstract class BasePerkListener implements Listener {

    protected boolean hasTeamPerk(Player player, Perk perk) {
        return getTeamPerks(player).contains(perk);
    }

    protected Set<Perk> getTeamPerks(Player player) {
        Team team = getTeam(player);
        if (team == null) return Set.of();
        return PerkManager.getInstance().getTeamPerks(team.getName());
    }

    protected Team getTeam(Player player) {
        return Bukkit.getScoreboardManager()
                .getMainScoreboard()
                .getEntryTeam(player.getName());
    }
}
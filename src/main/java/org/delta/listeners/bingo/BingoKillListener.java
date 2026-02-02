package org.delta.listeners.bingo;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scoreboard.Team;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;

import java.util.Map;

public class BingoKillListener implements Listener {

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(killer.getName());
        if (team == null) return;

        EntityType entityType = event.getEntityType();
        String mobName = entityType.name();

        trackKillProgress(team, mobName);
    }

    private void trackKillProgress(Team team, String mobName) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.KILL_MOB) continue;

            if (progressManager.isChallengeCompleted(team.getName(), challenge.id())) continue;

            if (!challenge.target().equalsIgnoreCase(mobName)) continue;

            progressManager.addProgress(team.getName(), challenge.id(), 1);

            BingoDataManager.getInstance().saveProgress();
        }
    }
}
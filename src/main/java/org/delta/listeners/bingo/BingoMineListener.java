package org.delta.listeners.bingo;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scoreboard.Team;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;

import java.util.Map;

public class BingoMineListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.CREATIVE) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        if (team == null) return;

        String blockName = event.getBlock().getType().name();

        trackMineProgress(team, blockName);
    }

    private void trackMineProgress(Team team, String blockName) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.MINE_BLOCK) continue;

            if (progressManager.isChallengeCompleted(team.getName(), challenge.id())) continue;

            if (!challenge.target().equalsIgnoreCase(blockName)) continue;

            progressManager.addProgress(team.getName(), challenge.id(), 1);

            BingoDataManager.getInstance().saveProgress();
        }
    }
}
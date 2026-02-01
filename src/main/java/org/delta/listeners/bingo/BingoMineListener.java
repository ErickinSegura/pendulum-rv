package org.delta.listeners.bingo;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;

import java.util.Map;

public class BingoMineListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // No contar si está en creativo
        if (player.getGameMode() == GameMode.CREATIVE) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        if (team == null) return;

        String blockName = event.getBlock().getType().name();

        checkMineChallenges(team, player, blockName);
    }

    private void checkMineChallenges(Team team, Player player, String blockName) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.MINE_BLOCK) continue;

            if (progressManager.isChallengeCompleted(team.getName(), challenge.getId())) continue;

            if (!challenge.getTarget().equalsIgnoreCase(blockName)) continue;

            progressManager.addProgress(team.getName(), challenge.getId(), 1);
            int currentProgress = progressManager.getProgress(team.getName(), challenge.getId());

            if (currentProgress >= challenge.getAmount()) {
                progressManager.completeChallenge(team.getName(), challenge.getId());
                notifyTeamCompletion(team, challenge);
            }
        }
    }

    private void notifyTeamCompletion(Team team, BingoChallenge challenge) {
        String message = BingoDataManager.getInstance()
                .getMessage("challenge-completed")
                .replace("{challenge}", ItemBuilder.format(challenge.getDisplayName()));

        for (String memberName : team.getEntries()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.color(message));
                member.playSound(member.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }
}
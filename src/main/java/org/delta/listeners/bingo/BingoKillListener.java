package org.delta.listeners.bingo;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
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

        checkKillChallenges(team, killer, mobName);
    }

    private void checkKillChallenges(Team team, Player player, String mobName) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            // Solo verificar retos de tipo KILL_MOB
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.KILL_MOB) continue;

            // Verificar si ya está completado
            if (progressManager.isChallengeCompleted(team.getName(), challenge.getId())) continue;

            // Verificar si es el mob correcto
            if (!challenge.getTarget().equalsIgnoreCase(mobName)) continue;

            // Agregar progreso (1 mob = 1 progreso)
            progressManager.addProgress(team.getName(), challenge.getId(), 1);
            int currentProgress = progressManager.getProgress(team.getName(), challenge.getId());

            // Verificar si se completó
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

        // Notificar a todos los miembros del equipo
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
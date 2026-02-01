package org.delta.listeners.bingo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;

import java.util.Map;

public class BingoCollectListener implements Listener {

    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        if (team == null) return;

        ItemStack item = event.getItem().getItemStack();
        String materialName = item.getType().name();

        checkCollectChallenges(team, player, materialName, item.getAmount());
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        if (team == null) return;

        ItemStack item = event.getCurrentItem();
        if (item == null) return;

        String materialName = item.getType().name();
        int amount = item.getAmount();

        if (event.isShiftClick()) {
            amount = getMaxCraftAmount(event, item);
        }

        checkCollectChallenges(team, player, materialName, amount);
    }

    private void checkCollectChallenges(Team team, Player player, String materialName, int amount) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.COLLECT_ITEM) continue;

            if (progressManager.isChallengeCompleted(team.getName(), challenge.getId())) continue;

            if (!challenge.getTarget().equalsIgnoreCase(materialName)) continue;

            progressManager.addProgress(team.getName(), challenge.getId(), amount);
            int currentProgress = progressManager.getProgress(team.getName(), challenge.getId());

            if (currentProgress >= challenge.getAmount()) {
                progressManager.completeChallenge(team.getName(), challenge.getId());
                notifyTeamCompletion(team, challenge);
            }
        }
    }

    private int getMaxCraftAmount(CraftItemEvent event, ItemStack result) {
        int maxCraft = result.getAmount();

        for (ItemStack ingredient : event.getInventory().getMatrix()) {
            if (ingredient != null && !ingredient.getType().isAir()) {
                maxCraft = Math.min(maxCraft, ingredient.getAmount() * result.getAmount());
            }
        }

        return maxCraft;
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
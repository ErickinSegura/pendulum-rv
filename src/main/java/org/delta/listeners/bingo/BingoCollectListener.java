package org.delta.listeners.bingo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
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

        trackCollectProgress(team, materialName, item.getAmount());
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

        trackCollectProgress(team, materialName, amount);
    }

    private void trackCollectProgress(Team team, String materialName, int amount) {
        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (BingoChallenge challenge : challenges.values()) {
            if (challenge.getChallengeType() != BingoChallenge.ChallengeType.COLLECT_ITEM) continue;

            if (progressManager.isChallengeCompleted(team.getName(), challenge.id())) continue;

            if (!challenge.target().equalsIgnoreCase(materialName)) continue;

            progressManager.addProgress(team.getName(), challenge.id(), amount);

            BingoDataManager.getInstance().saveProgress();
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
}
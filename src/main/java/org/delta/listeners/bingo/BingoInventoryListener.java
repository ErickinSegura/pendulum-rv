package org.delta.listeners.bingo;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoInventoryHolder;
import org.delta.managers.bingo.BingoProgressManager;

public class BingoInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BingoInventoryHolder holder)) return;

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());
        if (team == null || !team.getName().equals(holder.teamName())) return;

        int slot = event.getSlot();
        BingoChallenge challenge = getChallengeFromSlot(slot);

        if (challenge == null) return;

        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        if (progressManager.isChallengeCompleted(team.getName(), challenge.id())) {
            player.sendMessage(MessageUtils.color("&c¡Este reto ya está completado!"));
            return;
        }

        if (!hasRequirements(player, challenge)) {
            player.sendMessage(MessageUtils.color("&c¡No tienes los items/progreso necesarios para completar este reto!"));
            player.sendMessage(MessageUtils.color("&7Necesitas: &e" + challenge.amount() + "x " + challenge.target()));
            return;
        }

        if (completeChallengeManually(player, team, challenge)) {
            player.sendMessage(MessageUtils.color("&a¡Reto completado exitosamente!"));

            notifyTeamCompletion(team, challenge, player.getName());

            player.closeInventory();
        }
    }

    private BingoChallenge getChallengeFromSlot(int slot) {
        int gridSize = BingoDataManager.getInstance().getGridSize();
        int startSlot = 2;

        int row = slot / 9;
        int col = slot % 9;

        if (row >= gridSize || col < (startSlot % 9) || col >= (startSlot % 9) + gridSize) {
            return null;
        }

        int adjustedCol = col - (startSlot % 9);
        int challengeId = (row * gridSize) + adjustedCol + 1;

        return BingoDataManager.getInstance().getChallenge(String.valueOf(challengeId));
    }

    private boolean hasRequirements(Player player, BingoChallenge challenge) {
        switch (challenge.getChallengeType()) {
            case COLLECT_ITEM:
                return countItems(player, challenge.target()) >= challenge.amount();

            case KILL_MOB:
            case MINE_BLOCK:
                int progress = BingoProgressManager.getInstance().getProgress(
                        BingoProgressManager.getInstance().getPlayerTeam(player.getName()).getName(),
                        challenge.id()
                );
                return progress >= challenge.amount();

            default:
                return false;
        }
    }

    private int countItems(Player player, String materialName) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType().name().equalsIgnoreCase(materialName)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private boolean completeChallengeManually(Player player, Team team, BingoChallenge challenge) {
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        if (challenge.getChallengeType() == BingoChallenge.ChallengeType.COLLECT_ITEM) {
            if (!removeItems(player, challenge.target(), challenge.amount())) {
                return false;
            }
        }

        progressManager.completeChallenge(team.getName(), challenge.id());
        progressManager.setProgress(team.getName(), challenge.id(), challenge.amount());

        BingoDataManager.getInstance().saveProgress();

        checkBingoLines(team);

        return true;
    }

    private boolean removeItems(Player player, String materialName, int amount) {
        int remaining = amount;

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType().name().equalsIgnoreCase(materialName)) {
                int itemAmount = item.getAmount();

                if (itemAmount <= remaining) {
                    remaining -= itemAmount;
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(itemAmount - remaining);
                    remaining = 0;
                }

                if (remaining <= 0) break;
            }
        }

        return remaining == 0;
    }

    private void notifyTeamCompletion(Team team, BingoChallenge challenge, String completedBy) {
        String teamWithPlayer = team.getDisplayName() + " (" + completedBy + ")";

        String message = BingoDataManager.getInstance()
                .getMessage("challenge-completed")
                .replace("{challenge}", ItemBuilder.format(challenge.displayName()))
                .replace("{player}", teamWithPlayer);

        for (String memberName : team.getEntries()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.color(message));
                member.playSound(member.getLocation(),
                        org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }

    private void checkBingoLines(Team team) {
        int gridSize = BingoDataManager.getInstance().getGridSize();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        for (int row = 0; row < gridSize; row++) {
            boolean rowComplete = true;
            for (int col = 0; col < gridSize; col++) {
                int challengeId = (row * gridSize) + col + 1;
                if (!progressManager.isChallengeCompleted(team.getName(), challengeId)) {
                    rowComplete = false;
                    break;
                }
            }

            if (rowComplete && !progressManager.hasCompletedRow(team.getName(), row)) {
                progressManager.markRowCompleted(team.getName(), row);
                notifyBingo(team, "¡Fila " + (row + 1) + " completada!");
            }
        }

        for (int col = 0; col < gridSize; col++) {
            boolean colComplete = true;
            for (int row = 0; row < gridSize; row++) {
                int challengeId = (row * gridSize) + col + 1;
                if (!progressManager.isChallengeCompleted(team.getName(), challengeId)) {
                    colComplete = false;
                    break;
                }
            }

            if (colComplete && !progressManager.hasCompletedColumn(team.getName(), col)) {
                progressManager.markColumnCompleted(team.getName(), col);
                notifyBingo(team, "¡Columna " + (col + 1) + " completada!");
            }
        }

        boolean diagonal1 = true;
        boolean diagonal2 = true;

        for (int i = 0; i < gridSize; i++) {
            int challengeId1 = (i * gridSize) + i + 1;
            int challengeId2 = (i * gridSize) + (gridSize - 1 - i) + 1;

            if (!progressManager.isChallengeCompleted(team.getName(), challengeId1)) {
                diagonal1 = false;
            }
            if (!progressManager.isChallengeCompleted(team.getName(), challengeId2)) {
                diagonal2 = false;
            }
        }

        if (diagonal1 && !progressManager.hasCompletedDiagonal1(team.getName())) {
            progressManager.markDiagonal1Completed(team.getName());
            notifyBingo(team, "¡Diagonal principal completada!");
        }

        if (diagonal2 && !progressManager.hasCompletedDiagonal2(team.getName())) {
            progressManager.markDiagonal2Completed(team.getName());
            notifyBingo(team, "¡Diagonal secundaria completada!");
        }
    }

    private void notifyBingo(Team team, String lineType) {
        String message = BingoDataManager.getInstance()
                .getMessage("bingo-completed")
                .replace("{line}", lineType);

        for (String memberName : team.getEntries()) {
            Player member = org.bukkit.Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.color(message));
                member.playSound(member.getLocation(),
                        org.bukkit.Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof BingoInventoryHolder) {
            event.setCancelled(true);
        }
    }
}
package org.delta.listeners.bingo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
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
import org.delta.managers.bingo.BingoScoreManager;

public class BingoInventoryListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof BingoInventoryHolder holder)) return;

        event.setCancelled(true);

        if (holder.teamName().equals("GENERATING")) {
            return;
        }

        if (holder.teamName().equals("PREVIEW")) {
            if (event.getWhoClicked() instanceof Player player) {
                player.sendMessage(MessageUtils.color("&7Esta es solo una vista previa. Usa &d/pdl bingo &7para abrir tu tablero."));
            }
            return;
        }

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

            var logros = org.delta.pendulum.getInstance().getAchievementManager();
            int casillas = logros.addProgress(player, "bingo_casillas", 1);
            logros.unlock(player, org.delta.managers.achievements.Achievement.CASILLA_MARCADA);
            if (casillas >= 5) {
                logros.unlock(player, org.delta.managers.achievements.Achievement.MAESTRO_DEL_BINGO);
            }

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
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

        if (challenge.getChallengeType() == BingoChallenge.ChallengeType.COLLECT_ITEM) {
            if (!removeItems(player, challenge.target(), challenge.amount())) {
                return false;
            }
        }

        progressManager.completeChallenge(team.getName(), challenge.id());
        progressManager.setProgress(team.getName(), challenge.id(), challenge.amount());

        String challengeName = ItemBuilder.format(challenge.displayName());
        int points = scoreManager.registerChallengeCompletion(team.getName(), challenge.id(), challengeName);

        if (points > 0) {
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8&l≫ &a&l✔ RETO COMPLETADO &8&l≪"));
            player.sendMessage(MessageUtils.color("&7" + challengeName));
            player.sendMessage(MessageUtils.color("&e+" + points + " puntos &8| &7Total del equipo: &d" +
                    scoreManager.getTotalScore(team.getName()) + " pts"));
            player.sendMessage("");
        }

        BingoDataManager.getInstance().saveProgress();

        int lineasAntes = contarLineasBingo(team);
        checkBingoLines(team);
        checkFullBingo(team);
        if (contarLineasBingo(team) > lineasAntes) {
            org.delta.pendulum.getInstance().getAchievementManager()
                    .unlock(player, org.delta.managers.achievements.Achievement.PLENO_AL_BINGO);
        }

        return true;
    }

    private int contarLineasBingo(Team team) {
        BingoProgressManager pm = BingoProgressManager.getInstance();
        int diagonales = (pm.hasCompletedDiagonal1(team.getName()) ? 1 : 0)
                + (pm.hasCompletedDiagonal2(team.getName()) ? 1 : 0);
        return pm.getCompletedRows(team.getName()).size()
                + pm.getCompletedColumns(team.getName()).size()
                + diagonales;
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
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage(MessageUtils.color(message));
                member.playSound(member.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            }
        }
    }

    private void checkBingoLines(Team team) {
        int gridSize = BingoDataManager.getInstance().getGridSize();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

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

                String lineType = "row-" + row;
                String lineName = "Fila " + (row + 1);
                int points = scoreManager.registerLineCompletion(team.getName(), lineType, lineName);

                notifyBingo(team, lineName, points);
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

                String lineType = "col-" + col;
                String lineName = "Columna " + (col + 1);
                int points = scoreManager.registerLineCompletion(team.getName(), lineType, lineName);

                notifyBingo(team, lineName, points);
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

            String lineType = "diag1";
            String lineName = "Diagonal Principal";
            int points = scoreManager.registerLineCompletion(team.getName(), lineType, lineName);

            notifyBingo(team, lineName, points);
        }

        if (diagonal2 && !progressManager.hasCompletedDiagonal2(team.getName())) {
            progressManager.markDiagonal2Completed(team.getName());

            String lineType = "diag2";
            String lineName = "Diagonal Secundaria";
            int points = scoreManager.registerLineCompletion(team.getName(), lineType, lineName);

            notifyBingo(team, lineName, points);

        }
    }

    private void notifyBingo(Team team, String lineType, int points) {
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();

        for (String memberName : team.getEntries()) {
            Player member = Bukkit.getPlayer(memberName);
            if (member != null && member.isOnline()) {
                member.sendMessage("");
                member.sendMessage(MessageUtils.color("&8&l≫ &6&l¡BINGO! &8&l≪"));
                member.sendMessage(MessageUtils.color("&7¡Completaron la " + lineType + "!"));

                if (points > 0) {
                    member.sendMessage(MessageUtils.color("&e+" + points + " puntos &8| &7Total del equipo: &d" +
                            scoreManager.getTotalScore(team.getName()) + " pts"));
                }

                member.sendMessage("");
                member.playSound(member.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
            }
        }
    }

    private void checkFullBingo(Team team) {
        BingoProgressManager progressManager = BingoProgressManager.getInstance();
        BingoScoreManager scoreManager = BingoScoreManager.getInstance();
        BingoDataManager dataManager = BingoDataManager.getInstance();

        int gridSize = dataManager.getGridSize();
        int totalChallenges = gridSize * gridSize;

        int completedCount = progressManager.getCompletedChallenges(team.getName()).size();

        if (completedCount == totalChallenges) {
            int points = scoreManager.registerFullBingoCompletion(team.getName());

            if (points > 0) {
                Component announcement = MessageUtils.color(
                        "&8&l≫ &6&l¡BINGO COMPLETO! &8&l≪ &7¡El equipo &d" + team.getName() +
                                " &7ha completado todo el bingo! &e+" + points + " puntos"
                );

                Bukkit.broadcast(Component.empty());
                Bukkit.broadcast(announcement);
                Bukkit.broadcast(MessageUtils.color("&7Puntuación final del equipo: &d" +
                        scoreManager.getTotalScore(team.getName()) + " pts"));
                Bukkit.broadcast(Component.empty());

                for (String memberName : team.getEntries()) {
                    Player member = Bukkit.getPlayer(memberName);
                    if (member != null && member.isOnline()) {
                        member.playSound(member.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                        member.playSound(member.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 0.8f);
                        member.sendTitle(
                                MessageUtils.color("&6&l¡BINGO COMPLETO!").toString(),
                                MessageUtils.color("&e+" + points + " puntos").toString(),
                                10, 70, 20
                        );
                    }
                }

                dataManager.saveProgress();
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
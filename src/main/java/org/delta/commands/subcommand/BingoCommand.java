package org.delta.commands.subcommand;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoInventoryHolder;
import org.delta.managers.bingo.BingoProgressManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BingoCommand implements SubCommand {

    @Override
    public String getName() {
        return "bingo";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!BingoDataManager.getInstance().isEnabled()) {
            player.sendMessage(MessageUtils.color("&cEl sistema de bingo está desactivado."));
            return;
        }

        Team team = BingoProgressManager.getInstance().getPlayerTeam(player.getName());

        if (team == null) {
            player.sendMessage(MessageUtils.color("&cNo perteneces a ningún equipo."));
            return;
        }

        openBingoGUI(player, team);
    }

    private void openBingoGUI(Player player, Team team) {
        int gridSize = BingoDataManager.getInstance().getGridSize();

        // Calcular el tamaño del inventario
        // Para un grid de 4x4, necesitamos al menos 4 filas (36 slots)
        // Agregamos una fila extra para decoración/información
        int rows = gridSize + 1; // 4x4 grid + 1 fila de info = 5 filas
        if (rows > 6) rows = 6; // Máximo 6 filas en Minecraft

        int inventorySize = rows * 9;

        Component title = MessageUtils.color("&6&lBingo - " + team.getName());
        BingoInventoryHolder holder = new BingoInventoryHolder(team.getName());
        Inventory gui = Bukkit.createInventory(holder, inventorySize, title);

        Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();
        BingoProgressManager progressManager = BingoProgressManager.getInstance();

        int startSlot = 2;

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int challengeId = (row * gridSize) + col + 1;

                if (challengeId > challenges.size()) break;

                BingoChallenge challenge = challenges.get(String.valueOf(challengeId));
                if (challenge == null) continue;

                int slot = startSlot + (row * 9) + col;
                ItemStack item = createChallengeItem(challenge, team.getName(), progressManager);
                gui.setItem(slot, item);
            }
        }

        fillEmptySlots(gui, gridSize, startSlot);

        player.openInventory(gui);
    }

    private void fillEmptySlots(Inventory gui, int gridSize, int startSlot) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int i = 0; i < gui.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;

            boolean isInGrid = (row < gridSize) &&
                    (col >= (startSlot % 9)) &&
                    (col < (startSlot % 9) + gridSize);

            if (gui.getItem(i) == null && !isInGrid) {
                gui.setItem(i, glass);
            }
        }
    }


    private ItemStack createChallengeItem(BingoChallenge challenge, String teamName,
                                          BingoProgressManager progressManager) {
        Material material;
        try {
            material = Material.valueOf(challenge.getIcon());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }

        boolean completed = progressManager.isChallengeCompleted(teamName, challenge.getId());
        int progress = progressManager.getProgress(teamName, challenge.getId());

        String displayName = (completed ? "&a✔ " : "&7") + challenge.getDisplayName();

        List<String> lore = new ArrayList<>();
        lore.add(challenge.getDescription());
        lore.add("");
        lore.add("&7Progreso: &e" + progress + "&7/&e" + challenge.getAmount());

        if (completed) {
            lore.add("");
            lore.add("&a&l✔ COMPLETADO");
        }

        ItemBuilder builder = new ItemBuilder(material)
                .setDisplayName(ItemBuilder.format(displayName))
                .setLore(lore.stream()
                        .map(ItemBuilder::format)
                        .toList());

        if (completed) {
            builder.addEnchant(Enchantment.AQUA_AFFINITY, 1)
                    .addItemFlag(ItemFlag.HIDE_ENCHANTS);
        }

        return builder.build();
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage(MessageUtils.color("&eUso: &7/pendulum bingo"));
        player.sendMessage(MessageUtils.color("&7Abre la tabla de bingo de tu equipo"));
    }
}
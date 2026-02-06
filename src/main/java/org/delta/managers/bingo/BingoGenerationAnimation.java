package org.delta.managers.bingo;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.delta.pendulum;

import java.util.*;

public class BingoGenerationAnimation {
    private final pendulum plugin;
    private final Player player;
    private final int gridSize;
    private final int startSlot;
    private Inventory animationInventory;
    private final List<BingoChallenge> masterChallenges;

    public pendulum getPlugin() {
        return plugin;
    }

    public BingoGenerationAnimation(pendulum plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.gridSize = BingoDataManager.getInstance().getGridSize();
        this.startSlot = 2;

        Map<Integer, BingoChallenge> masterMap = BingoChallengesManager.getInstance().getMasterChallengeList();
        this.masterChallenges = new ArrayList<>(masterMap.values());
    }


    public void startAnimation(Runnable onComplete) {
        int rows = Math.min(gridSize, 6);
        int inventorySize = rows * 9;

        Component title = MessageUtils.color("&6&lGenerando Bingo...");
        BingoInventoryHolder holder = new BingoInventoryHolder("GENERATING");
        animationInventory = Bukkit.createInventory(holder, inventorySize, title);

        fillEmptySlots();

        player.openInventory(animationInventory);

        startShuffleAnimation(onComplete);
    }

    private void startShuffleAnimation(Runnable onComplete) {
        new BukkitRunnable() {
            int ticks = 0;
            final int totalTicks = 60;
            final int shufflesPerSecond = 5;
            final int ticksPerShuffle = 20 / shufflesPerSecond;

            @Override
            public void run() {
                if (player.getOpenInventory().getTopInventory() != animationInventory) {
                    this.cancel();
                    return;
                }

                if (ticks % ticksPerShuffle == 0) {
                    shuffleItems();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.3f, 1.0f + (ticks / (float) totalTicks));
                }

                ticks++;

                if (ticks >= totalTicks) {
                    this.cancel();

                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);

                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        showFinalTable();
                    }, 2L);
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void showFinalTable() {
        player.closeInventory();

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Map<String, BingoChallenge> challenges = BingoDataManager.getInstance().getChallenges();

            if (challenges.isEmpty()) {
                player.sendMessage(MessageUtils.color("&c✘ Error al mostrar la tabla generada"));
                return;
            }

            int rows = Math.min(gridSize, 6);
            int inventorySize = rows * 9;

            Component title = MessageUtils.color("&6&lNueva Tabla de Bingo");
            BingoInventoryHolder holder = new BingoInventoryHolder("PREVIEW");
            Inventory previewInventory = Bukkit.createInventory(holder, inventorySize, title);

            for (int row = 0; row < gridSize; row++) {
                for (int col = 0; col < gridSize; col++) {
                    int challengeId = (row * gridSize) + col + 1;

                    if (challengeId > challenges.size()) break;

                    BingoChallenge challenge = challenges.get(String.valueOf(challengeId));
                    if (challenge == null) continue;

                    int slot = startSlot + (row * 9) + col;
                    ItemStack item = createPreviewItem(challenge);
                    previewInventory.setItem(slot, item);
                }
            }

            fillEmptySlots(previewInventory);

            player.openInventory(previewInventory);

            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8&l≫ &6&lNUEVA TABLA GENERADA &8&l≪"));
            player.sendMessage(MessageUtils.color("&7Usa &d/pdl bingo &7para abrir tu tablero de equipo"));
            player.sendMessage("");
        }, 1L);
    }

    private ItemStack createPreviewItem(BingoChallenge challenge) {
        Material material;
        try {
            material = Material.valueOf(challenge.icon());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }

        List<String> lore = new ArrayList<>();
        lore.add(challenge.description());
        lore.add("");
        lore.add("&7Objetivo: &e" + challenge.amount() + "x " + challenge.target());

        return new ItemBuilder(material)
                .setDisplayName(ItemBuilder.format("&6" + challenge.displayName()))
                .setLore(lore.stream()
                        .map(ItemBuilder::format)
                        .toList())
                .build();
    }

    private void shuffleItems() {
        Random random = new Random();

        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize; col++) {
                int slot = startSlot + (row * 9) + col;

                BingoChallenge randomChallenge = masterChallenges.get(random.nextInt(masterChallenges.size()));

                ItemStack item = createShuffleItem(randomChallenge);
                animationInventory.setItem(slot, item);
            }
        }
    }

    private ItemStack createShuffleItem(BingoChallenge challenge) {
        Material material;
        try {
            material = Material.valueOf(challenge.icon());
        } catch (IllegalArgumentException e) {
            material = Material.PAPER;
        }

        return new ItemBuilder(material)
                .setDisplayName(ItemBuilder.format("&7???"))
                .setLore(Arrays.asList(
                        ItemBuilder.format("&8Mezclando..."),
                        ItemBuilder.format("&8Generando tabla...")
                ))
                .build();
    }

    private void fillEmptySlots() {
        fillEmptySlots(animationInventory);
    }

    private void fillEmptySlots(Inventory inventory) {
        ItemStack glass = new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE)
                .setDisplayName(" ")
                .build();

        for (int i = 0; i < inventory.getSize(); i++) {
            int row = i / 9;
            int col = i % 9;

            boolean isInGrid = (row >= (startSlot / 9)) && (row < (startSlot / 9) + gridSize) &&
                    (col >= (startSlot % 9)) &&
                    (col < (startSlot % 9) + gridSize);

            if (!isInGrid) {
                inventory.setItem(i, glass);
            }
        }
    }
}
package org.delta.libs.death;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class LifeManager {

    private final Plugin plugin;
    private static final int MAX_LIVES = 3;

    private final NamespacedKey livesKey;

    public LifeManager(Plugin plugin) {
        this.plugin = plugin;
        this.livesKey = new NamespacedKey(plugin, "player_lives");
        startActionBarUpdater();
    }

    private void startActionBarUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateHealthDisplay(player);
            }
        }, 0L, 20L);
    }


    public void initializePlayer(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        if (!data.has(livesKey, PersistentDataType.INTEGER)) {
            data.set(livesKey, PersistentDataType.INTEGER, MAX_LIVES);
        }
    }

    public int getLives(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.getOrDefault(livesKey, PersistentDataType.INTEGER, MAX_LIVES);
    }

    public void setLives(Player player, int lives) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(livesKey, PersistentDataType.INTEGER, Math.max(0, Math.min(lives, MAX_LIVES)));
        updateHealthDisplay(player);
    }

    public void removeLife(Player player) {
        int currentLives = getLives(player);

        if (currentLives > 0) {
            currentLives--;

            PersistentDataContainer data = player.getPersistentDataContainer();
            data.set(livesKey, PersistentDataType.INTEGER, currentLives);
        }
    }

    public void updateHealthDisplay(Player player) {
        int lives = getLives(player);
        Component actionBar = Component.empty();

        for (int i = 0; i < MAX_LIVES; i++) {
            if (i < lives) {
                actionBar = actionBar.append(Component.text("⏰ ", TextColor.color(0x55FF55)));
            } else {
                actionBar = actionBar.append(Component.text("⏰ ", TextColor.color(0x555555)));
            }
        }

        player.sendActionBar(actionBar);
    }


    public void resetLives(Player player) {
        setLives(player, MAX_LIVES);
    }

}

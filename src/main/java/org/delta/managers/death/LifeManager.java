package org.delta.managers.death;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.Icons;
import org.delta.libs.MessageUtils;

import java.util.ArrayDeque;
import java.util.Queue;

public class LifeManager {

    private final Plugin plugin;
    private static final int MAX_LIVES = 3;

    private final NamespacedKey livesKey;
    private boolean animating = false;
    private final Queue<ClockLossData> lossQueue = new ArrayDeque<>();

    public LifeManager(Plugin plugin) {
        this.plugin = plugin;
        this.livesKey = new NamespacedKey(plugin, "player_lives");
        startActionBarUpdater();
    }

    private void startActionBarUpdater() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (animating) return;
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
                actionBar = actionBar.append(Icons.ACTIVE_CLOCK);
            } else {
                actionBar = actionBar.append(Icons.INACTIVE_CLOCK);
            }
        }

        player.sendActionBar(actionBar);
    }


    public void playClockLossAnimation(Player dead) {
        lossQueue.add(new ClockLossData(dead.getName(), getLives(dead)));
        if (!animating) {
            playNextLoss();
        }
    }

    private void playNextLoss() {
        ClockLossData data = lossQueue.poll();
        if (data == null) {
            animating = false;
            for (Player p : Bukkit.getOnlinePlayers()) {
                updateHealthDisplay(p);
            }
            return;
        }

        animating = true;

        final String name = data.name();
        final int lives = data.lives();
        final int lostIndex = lives;
        final int frames = 6;

        new BukkitRunnable() {
            int frame = 0;

            @Override
            public void run() {
                if (frame >= frames) {
                    cancel();
                    playNextLoss();
                    return;
                }

                boolean lostOn = frame % 2 == 0;
                Component bar = buildLossBar(name, lives, lostIndex, lostOn);
                for (Player p : Bukkit.getOnlinePlayers()) {
                    p.sendActionBar(bar);
                }
                frame++;
            }
        }.runTaskTimer(plugin, 0L, 5L);
    }

    private Component buildLossBar(String name, int lives, int lostIndex, boolean lostOn) {
        Component bar = MessageUtils.color("&5&l" + name + " &7perdió un reloj  ");
        for (int i = 0; i < MAX_LIVES; i++) {
            if (i == lostIndex) {
                bar = bar.append(lostOn ? Icons.ACTIVE_CLOCK : Icons.INACTIVE_CLOCK);
            } else if (i < lives) {
                bar = bar.append(Icons.ACTIVE_CLOCK);
            } else {
                bar = bar.append(Icons.INACTIVE_CLOCK);
            }
        }
        return bar;
    }

    public void resetLives(Player player) {
        setLives(player, MAX_LIVES);
    }

    private record ClockLossData(String name, int lives) {}

}

package org.delta.managers.death;

import io.papermc.paper.scoreboard.numbers.NumberFormat;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.delta.database.repositories.PlayerRepository;
import org.delta.libs.Icons;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

import java.util.ArrayDeque;
import java.util.Queue;

public class LifeManager {

    private final Plugin plugin;
    private final int maxLives;

    private final NamespacedKey livesKey;
    private final Objective belowNameObjective;
    private boolean animating = false;
    private final Queue<ClockLossData> lossQueue = new ArrayDeque<>();

    public LifeManager(Plugin plugin) {
        this.plugin = plugin;
        this.maxLives = Math.max(1, PendulumSettings.getInstance().getVidas());
        this.livesKey = new NamespacedKey(plugin, "player_lives");
        this.belowNameObjective = setupBelowName();
        startActionBarUpdater();
    }

    private Objective setupBelowName() {
        Scoreboard board = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective obj = board.getObjective("pdl_relojes");
        if (obj == null) {
            obj = board.registerNewObjective("pdl_relojes", Criteria.DUMMY, Component.empty());
        }
        obj.setDisplaySlot(DisplaySlot.BELOW_NAME);
        return obj;
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
            data.set(livesKey, PersistentDataType.INTEGER, maxLives);
        }
    }

    public int getLives(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.getOrDefault(livesKey, PersistentDataType.INTEGER, maxLives);
    }

    public void setLives(Player player, int lives) {
        int clamped = Math.max(0, Math.min(lives, maxLives));
        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(livesKey, PersistentDataType.INTEGER, clamped);
        updateHealthDisplay(player);
        syncToDb(player, clamped);
    }

    public void removeLife(Player player) {
        int currentLives = getLives(player);

        if (currentLives > 0) {
            currentLives--;

            PersistentDataContainer data = player.getPersistentDataContainer();
            data.set(livesKey, PersistentDataType.INTEGER, currentLives);

            syncToDb(player, currentLives);
        }
    }

    private void syncToDb(Player player, int lives) {
        var db = pendulum.getInstance().getDatabaseManager();
        if (db == null || !db.isConnected()) return;

        var loc = player.getLocation();
        var data = new PlayerRepository.PlayerData(
                player.getUniqueId(),
                player.getName(),
                lives,
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getWorld() != null ? loc.getWorld().getName() : null
        );

        db.players().upsert(player.getUniqueId(), data)
                .exceptionally(err -> {
                    pendulum.getInstance().getLogger().warning(
                            "[LifeSync] Error al actualizar vidas de " + player.getName() + ": " + err.getMessage());
                    return null;
                });
    }

    public void updateHealthDisplay(Player player) {
        int lives = getLives(player);
        Component actionBar = Component.empty();

        for (int i = 0; i < maxLives; i++) {
            if (i < lives) {
                actionBar = actionBar.append(Icons.ACTIVE_CLOCK);
            } else {
                actionBar = actionBar.append(Icons.INACTIVE_CLOCK);
            }
        }

        player.sendActionBar(actionBar);
        updateNameDisplays(player, lives);
    }

    private void updateNameDisplays(Player player, int lives) {
        Component clocks = Component.empty();
        for (int i = 0; i < maxLives; i++) {
            clocks = clocks.append(i < lives ? Icons.ACTIVE_CLOCK : Icons.INACTIVE_CLOCK);
        }

        if (belowNameObjective != null) {
            var score = belowNameObjective.getScore(player.getName());
            score.setScore(lives);
            score.numberFormat(NumberFormat.fixed(clocks));
        }

        var rangoManager = pendulum.getInstance().getRangoManager();
        Component prefijo = rangoManager != null ? rangoManager.getPrefijo(player) : Component.empty();
        Component nombre = rangoManager != null
                ? rangoManager.getNombre(player)
                : MessageUtils.color("&f" + player.getName());

        Component tab = Component.empty()
                .append(prefijo)
                .append(nombre)
                .append(MessageUtils.color("&f "))
                .append(clocks);
        player.playerListName(tab);
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
        for (int i = 0; i < maxLives; i++) {
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
        setLives(player, maxLives);
    }

    private record ClockLossData(String name, int lives) {}

}

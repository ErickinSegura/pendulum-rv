package org.delta.managers.death;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.delta.libs.Icons;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

import java.time.Duration;

import static org.bukkit.Bukkit.getServer;
import static org.delta.managers.death.ChestEvents.placeDeathChest;

public class DeathEvents {
    pendulum plugin = pendulum.getInstance();
    private PlayerDeathMessages deathMessages;

    private static final int CLOCK_CYCLES = 1;
    private static final long TICKS_PER_FRAME = 1L;

    private static final boolean SYNC_DAY_NIGHT = true;
    private static final long DAY_NIGHT_SPEED = 2L;

    public DeathEvents() {
        this.deathMessages = new PlayerDeathMessages(plugin);
    }

    public void handlePlayerDeath(Player player, Location location, PlayerDeathEvent event) {
        displayDeathClockAnimation(player);
        PilarEvents.placeDeathPilar(player, location, event);
        placeDeathChest(player, location, event);
        broadcastDeathMessages(player);
    }

    private void displayDeathClockAnimation(Player player) {
        if (player == null || !player.isOnline()) return;

        World world = player.getWorld();
        long originalTime = world.getTime();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            animateClockForPlayer(onlinePlayer, player.getName(), world, originalTime);
        }
    }

    private void animateClockForPlayer(Player viewer, String deadPlayerName, World world, long originalTime) {
        final int framesPerCycle = 63;
        final int totalFrames = framesPerCycle * CLOCK_CYCLES;

        final long totalAnimationTicks = totalFrames * TICKS_PER_FRAME;
        final long timePerTick = SYNC_DAY_NIGHT ? (24000L * CLOCK_CYCLES) / totalAnimationTicks : 0;

        Component subtitle = MessageUtils.color("&dA &5&l" + deadPlayerName + "&r&d se le terminó el tiempo");

        for (int i = 0; i < totalFrames; i++) {
            final int frameIndex;
            if (i < 31) {
                frameIndex = 33 + i;
            } else {
                frameIndex = i - 31;
            }

            final long delay = i * TICKS_PER_FRAME;
            final int frameNumber = i;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (viewer.isOnline()) {
                    Component clockFrame = Icons.getClockFrame(frameIndex);

                    Title title = Title.title(
                            clockFrame,
                            subtitle,
                            Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofMillis(150),
                                    Duration.ZERO
                            )
                    );

                    viewer.showTitle(title);

                    if (SYNC_DAY_NIGHT) {
                        long newTime = (originalTime + (timePerTick * frameNumber * DAY_NIGHT_SPEED)) % 24000;
                        world.setTime(newTime);
                    }
                }
            }, delay);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (SYNC_DAY_NIGHT) {
                world.setTime(originalTime);
            }

            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (onlinePlayer.isOnline()) {
                    Title finalTitle = Title.title(
                            Icons.INACTIVE_CLOCK,
                            subtitle,
                            Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofMillis(800),
                                    Duration.ofMillis(600)
                            )
                    );
                    onlinePlayer.showTitle(finalTitle);
                }
            }
        }, totalAnimationTicks);
    }

    private void broadcastDeathMessages(Player player) {
        if (player != null) {
            String playerName = player.getName();
            player.sendMessage(MessageUtils.color("&cTe quedaste sin relojs"));
            getServer().broadcast(MessageUtils.color("&dA &5&l" + playerName + "&r&d se le ha acabado el tiempo..."));

            if (deathMessages.hasCustomMessage(playerName)) {
                String customMessage = deathMessages.getCustomDeathMessage(playerName);
                getServer().broadcast(MessageUtils.color("&7"+customMessage));
            } else {
                getServer().broadcast(MessageUtils.color("&7" + playerName + " hasta aquí llegó"));
            }
        }
    }
}
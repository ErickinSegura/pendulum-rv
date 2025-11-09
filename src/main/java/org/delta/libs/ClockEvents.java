package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import java.time.Duration;
import static org.bukkit.Bukkit.getServer;
import static org.delta.libs.ChestEvents.placeDeathChest;

public class ClockEvents {
    private static Plugin plugin;

    public static void setPlugin(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static void handlePlayerClockLoss(Player player, int currentLives, Location location, PlayerDeathEvent event) {
        broadcastClockMessages(player, currentLives);
        placeDeathChest(player, location, event);
        broadcastClockSound();
        temporaryBanPlayer(player);


    }

    private static void broadcastClockSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "minecraft:ambient.cave", 1, 1);
            player.playSound(player.getLocation(), "minecraft:block.glass.break", 1, 0.5f);
        }
    }

    private static void broadcastClockMessages(Player player, int currentLives) {
        if (player != null) {
            String playerName = player.getName();
            switch (currentLives) {
                case 2:
                    player.sendMessage(Component.text("Te quedan " + currentLives + " relojs", NamedTextColor.GOLD));
                    break;
                case 1:
                    player.sendMessage(Component.text("Te queda " + currentLives + " reloj", NamedTextColor.GOLD));
                    break;
                default:
                    break;
            }
            getServer().broadcast(MessageUtils.color("&5&l" + playerName + "&r&d perdió un reloj! Le quedan &l" + currentLives + "&r&d."));
        }
    }

    private static void temporaryBanPlayer(Player player) {
        if (player != null) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (player.isOnline()) {
                        BanList banList = getServer().getBanList(BanList.Type.PROFILE);
                        Duration banDuration = Duration.ofSeconds(5);
                        //Duration banDuration = Duration.ofHours(1);
                        String banSource = "Sistema de Relojes";

                        String timeRemaining = formatDuration(banDuration);
                        String banReason = "Perdiste un reloj. Vuelve cuando pase el tiempo para revivir.";

                        banList.addBan(player.getPlayerProfile(), banReason, banDuration, banSource);

                        Component kickMessage = Component.text("═══════════════════════════\n\n", NamedTextColor.DARK_PURPLE)
                                .append(Component.text("Perdiste un reloj\n\n", NamedTextColor.WHITE))
                                .append(Component.text("Tiempo para revivir: ", NamedTextColor.GRAY))
                                .append(Component.text(timeRemaining + "\n\n", NamedTextColor.LIGHT_PURPLE))
                                .append(Component.text("═══════════════════════════", NamedTextColor.DARK_PURPLE));

                        player.kick(kickMessage);
                    }
                }
            }.runTaskLater(plugin, 120L); // 1 segundo = 20 ticks
        }
    }

    private static String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();

        StringBuilder timeString = new StringBuilder();

        if (hours > 0) {
            timeString.append(hours).append(hours == 1 ? " hora" : " horas");
        }
        if (minutes > 0) {
            if (!timeString.isEmpty()) timeString.append(", ");
            timeString.append(minutes).append(minutes == 1 ? " minuto" : " minutos");
        }
        if (seconds > 0 && hours == 0) {
            if (!timeString.isEmpty()) timeString.append(" y ");
            timeString.append(seconds).append(seconds == 1 ? " segundo" : " segundos");
        }

        return timeString.toString();
    }
}

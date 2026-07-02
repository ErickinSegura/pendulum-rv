package org.delta.managers.death;

import com.destroystokyo.paper.profile.PlayerProfile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

import java.time.Duration;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import static org.bukkit.Bukkit.getServer;
import static org.delta.managers.death.ChestEvents.placeDeathChest;

public class ClockEvents {
    private static Plugin plugin;

    private static final long BAN_DELAY_TICKS = 120L;
    private static final Set<UUID> pendingBan = new HashSet<>();

    public static void setPlugin(Plugin pluginInstance) {
        plugin = pluginInstance;
    }

    public static boolean isPendingBan(Player player) {
        return player != null && pendingBan.contains(player.getUniqueId());
    }

    public static void handlePlayerClockLoss(Player player, int currentLives, Location location, PlayerDeathEvent event) {
        broadcastClockMessages(player, currentLives);
        PilarEvents.placeDeathPilar(player, location, event);
        placeDeathChest(player, location, event);
        broadcastClockSound();
        pendulum.getInstance().getLifeManager().playClockLossAnimation(player);
        temporaryBanPlayer(player);
    }

    public static void handleCombatLogClockLoss(Player player, int currentLives) {
        broadcastCombatLogMessages(player, currentLives);
        broadcastClockSound();
        pendulum.getInstance().getLifeManager().playClockLossAnimation(player);
    }

    public static void handleCombatLogElimination(Player player) {
        if (player == null) return;
        getServer().broadcast(MessageUtils.color("&5&l" + player.getName()
                + "&r&d hizo combat log y perdió su último reloj. Ha sido eliminado."));
        broadcastClockSound();
        permanentBanPlayer(player);
    }

    public static void permanentBanPlayer(Player player) {
        if (player == null) return;
        applyBan(player.getPlayerProfile(), true);
        if (player.isOnline()) {
            player.kick(buildKickMessage(true));
        }
    }

    public static void schedulePermanentBan(Player player) {
        scheduleBan(player, true);
    }

    private static void broadcastCombatLogMessages(Player player, int currentLives) {
        if (player == null) return;
        String playerName = player.getName();
        getServer().broadcast(MessageUtils.color("&5&l" + playerName
                + "&r&d hizo combat log y perdió un reloj! Le "
                + (currentLives == 1 ? "queda &l" : "quedan &l") + currentLives + "&r&d."));
    }

    private static void broadcastClockSound() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), "minecraft:entity.warden.sonic_boom", 1, 2f);
            player.playSound(player.getLocation(), "minecraft:block.bell.resonate", 1, 2f);
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
        scheduleBan(player, false);
    }

    private static void scheduleBan(Player player, boolean permanent) {
        if (player == null) return;

        final UUID uuid = player.getUniqueId();
        final PlayerProfile profile = player.getPlayerProfile();
        pendingBan.add(uuid);

        new BukkitRunnable() {
            @Override
            public void run() {
                applyBan(profile, permanent);
                pendingBan.remove(uuid);

                Player online = getServer().getPlayer(uuid);
                if (online != null && online.isOnline()) {
                    online.kick(buildKickMessage(permanent));
                }
            }
        }.runTaskLater(pendulum.getInstance(), BAN_DELAY_TICKS);
    }

    private static void applyBan(PlayerProfile profile, boolean permanent) {
        BanList banList = getServer().getBanList(BanList.Type.PROFILE);
        String banSource = "Sistema de Relojes";

        if (permanent) {
            banList.addBan(profile,
                    "Perdiste todos tus relojes. Has sido eliminado permanentemente.",
                    (Duration) null, banSource);
        } else {
            banList.addBan(profile,
                    "Perdiste un reloj. Vuelve cuando pase el tiempo para revivir.",
                    Duration.ofSeconds(5), banSource);
        }
    }

    private static Component buildKickMessage(boolean permanent) {
        if (permanent) {
            return Component.text("═══════════════════════════\n\n", NamedTextColor.DARK_PURPLE)
                    .append(Component.text("Has sido eliminado\n\n", NamedTextColor.WHITE))
                    .append(Component.text("Perdiste todos tus relojes\n\n", NamedTextColor.GRAY))
                    .append(Component.text("═══════════════════════════", NamedTextColor.DARK_PURPLE));
        }

        String timeRemaining = formatDuration(Duration.ofSeconds(5));
        return Component.text("═══════════════════════════\n\n", NamedTextColor.DARK_PURPLE)
                .append(Component.text("Perdiste un reloj\n\n", NamedTextColor.WHITE))
                .append(Component.text("Tiempo para revivir: ", NamedTextColor.GRAY))
                .append(Component.text(timeRemaining + "\n\n", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text("═══════════════════════════", NamedTextColor.DARK_PURPLE));
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

package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import static org.bukkit.Bukkit.getServer;
import static org.delta.libs.ChestEvents.placeDeathChest;

public class ClockEvents {
    public static void handlePlayerClockLoss(Player player, int currentLives, Location location, PlayerDeathEvent event) {
        broadcastClockMessages(player, currentLives);
        placeDeathChest(player, location, event);
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
}

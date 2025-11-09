package org.delta.libs.death;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

import static org.bukkit.Bukkit.getServer;
import static org.delta.libs.death.ChestEvents.placeDeathChest;

public class DeathEvents {
    pendulum plugin = pendulum.getInstance();


    public void handlePlayerDeath(Player player, Location location, PlayerDeathEvent event) {
        displayDeathTitle();
        placeDeathChest(player, location, event);
        broadcastDeathMessages(player, location);
    }

    private void displayDeathTitle() {
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "animation true 1 0 48");
        }, 20L);
    }

    private void broadcastDeathMessages(Player player, Location location) {
        if (player != null) {
            String playerName = player.getName();
            player.sendMessage(Component.text("Te quedaste sin relojs", NamedTextColor.RED));
            getServer().broadcast(MessageUtils.color("&dA &5&l" + playerName + "&r&d se le ha acabado el tiempo..."));
        }
    }
}

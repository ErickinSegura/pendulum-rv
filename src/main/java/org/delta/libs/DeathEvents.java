package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;
import static org.delta.libs.ChestEvents.placeDeathChest;

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

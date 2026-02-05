package org.delta.listeners.teamChest;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.delta.managers.teamChest.TeamChestManager;

public class TeamChestListener implements Listener {

    private final TeamChestManager manager;

    public TeamChestListener() {
        this.manager = TeamChestManager.getInstance();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        if (manager.isTeamChest(event.getInventory())) {
            manager.closeChest(player);

            manager.saveData();
        }
    }
}

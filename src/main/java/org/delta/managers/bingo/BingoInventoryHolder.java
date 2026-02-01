package org.delta.managers.bingo;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public record BingoInventoryHolder(String teamName) implements InventoryHolder {

    @Override
    @NotNull
    public Inventory getInventory() {
        return null;
    }
}
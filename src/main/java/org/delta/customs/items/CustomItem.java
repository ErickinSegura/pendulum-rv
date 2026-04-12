package org.delta.customs.items;

import org.bukkit.inventory.ItemStack;

public interface CustomItem {
    String getKey();
    ItemStack build();
}
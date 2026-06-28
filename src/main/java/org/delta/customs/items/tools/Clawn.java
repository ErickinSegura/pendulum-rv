package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Clawn implements CustomItem {
    @Override
    public String getKey() {
        return "clawn";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&6Clawn"))
                .setCustomModelData(1)
                .setMaxStackSize(1)
                .build();
    }
}

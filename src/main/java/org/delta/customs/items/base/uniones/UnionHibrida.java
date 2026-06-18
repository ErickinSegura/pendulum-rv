package org.delta.customs.items.base.uniones;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class UnionHibrida implements CustomItem {
    @Override public String getKey() { return "union_hibrida"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&dUnión Hibrida"))
                .setCustomModelData(10)
                .build();
    }
}

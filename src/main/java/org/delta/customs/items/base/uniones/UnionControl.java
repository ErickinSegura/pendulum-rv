package org.delta.customs.items.base.uniones;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class UnionControl implements CustomItem {
    @Override public String getKey() { return "union_control"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&5Unión de Control"))
                .setCustomModelData(9)
                .build();
    }
}

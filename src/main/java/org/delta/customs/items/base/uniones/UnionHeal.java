package org.delta.customs.items.base.uniones;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class UnionHeal implements CustomItem {
    @Override public String getKey() { return "union_heal"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&aUnión de Curación"))
                .setCustomModelData(7)
                .build();
    }
}
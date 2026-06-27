package org.delta.customs.items.modifier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class UnbreakeableModifier implements CustomItem {
    @Override
    public String getKey() {
        return "unbreakable_modifier";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.NETHER_BRICK)
                .setDisplayName(ItemBuilder.format("&6Irrompible Modifier"))
                .setCustomModelData(1)
                .build();
    }
}

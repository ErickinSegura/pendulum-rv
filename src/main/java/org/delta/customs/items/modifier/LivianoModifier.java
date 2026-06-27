package org.delta.customs.items.modifier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class LivianoModifier implements CustomItem {
    @Override
    public String getKey() {
        return "liviano_modifier";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.FEATHER)
                .setDisplayName(ItemBuilder.format("&fLiviano Modifier"))
                .setCustomModelData(1)
                .build();
    }
}

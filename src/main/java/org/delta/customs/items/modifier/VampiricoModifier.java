package org.delta.customs.items.modifier;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class VampiricoModifier implements CustomItem {
    @Override
    public String getKey() {
        return "vampirico_modifier";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.GHAST_TEAR)
                .setDisplayName(ItemBuilder.format("&cPLACEHOLDER_NOMBRE Modifier"))
                .setCustomModelData(1)
                .build();
    }
}

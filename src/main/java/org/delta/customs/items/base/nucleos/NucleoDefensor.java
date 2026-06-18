package org.delta.customs.items.base.nucleos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class NucleoDefensor implements CustomItem {
    @Override public String getKey() { return "nucleo_defensor"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&bNúcleo Defensor"))
                .setCustomModelData(12)
                .build();
    }
}

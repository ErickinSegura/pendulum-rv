package org.delta.customs.items.base.nucleos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class NucleoHeal implements CustomItem {
    @Override public String getKey() { return "nucleo_heal"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&aNúcleo Sanador"))
                .setCustomModelData(13)
                .build();
    }
}

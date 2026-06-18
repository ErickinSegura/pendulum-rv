package org.delta.customs.items.base.fragmentos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class FragmentoHeal implements CustomItem {
    @Override public String getKey() { return "fragmento_heal"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&aFragmento de Curación"))
                .setCustomModelData(6)
                .build();
    }
}

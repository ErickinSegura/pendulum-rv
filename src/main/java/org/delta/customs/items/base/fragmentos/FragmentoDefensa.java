package org.delta.customs.items.base.fragmentos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FragmentoDefensa implements CustomItem {
    @Override public String getKey() { return "fragmento_defensa"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&bFragmento de Defensa"))
                .setCustomModelData(4)
                .build();
    }
}
package org.delta.customs.items.base.fragmentos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FragmentoAtaque implements CustomItem {
    @Override public String getKey() { return "fragmento_ataque"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&cFragmento de Ataque"))
                .setCustomModelData(2)
                .build();
    }
}

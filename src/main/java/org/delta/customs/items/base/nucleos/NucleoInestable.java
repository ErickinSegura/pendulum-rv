package org.delta.customs.items.base.nucleos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class NucleoInestable implements CustomItem {
    @Override public String getKey() { return "nucleo_inestable"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.NETHER_STAR)
                .setDisplayName("&d&lNúcleo Inestable")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &dHíbrido&7."), ItemBuilder.format("&8Clase: &dHíbrido")))
                .build();
    }
}

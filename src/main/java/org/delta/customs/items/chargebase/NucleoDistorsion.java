package org.delta.customs.items.chargebase;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class NucleoDistorsion implements CustomItem {
    @Override public String getKey() { return "nucleo_distorsion"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.ENDER_PEARL)
                .setDisplayName("&e&lNúcleo de Distorsión")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &eControlador&7."), ItemBuilder.format("&8Clase: &eControlador")))
                .build();
    }
}

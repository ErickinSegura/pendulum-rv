package org.delta.customs.items.base.nucleos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class NucleoProteccion implements CustomItem {
    @Override public String getKey() { return "nucleo_proteccion"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.IRON_INGOT)
                .setDisplayName("&b&lNúcleo de Protección")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &bDefensor&7."), ItemBuilder.format("&8Clase: &bDefensor")))
                .build();
    }
}
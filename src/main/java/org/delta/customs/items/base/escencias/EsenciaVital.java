package org.delta.customs.items.base.escencias;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class EsenciaVital implements CustomItem {
    @Override public String getKey() { return "esencia_vital"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.GLISTERING_MELON_SLICE)
                .setDisplayName("&a&lEsencia Vital")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &aHealer&7."), ItemBuilder.format("&8Clase: &aHealer")))
                .build();
    }
}

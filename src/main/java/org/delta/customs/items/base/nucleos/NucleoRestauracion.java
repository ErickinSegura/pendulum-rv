package org.delta.customs.items.base.nucleos;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class NucleoRestauracion implements CustomItem {
    @Override public String getKey() { return "nucleo_restauracion"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.EXPERIENCE_BOTTLE)
                .setDisplayName("&a&lNúcleo de Restauración")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &aHealer&7."), ItemBuilder.format("&8Clase: &aHealer")))
                .build();
    }
}
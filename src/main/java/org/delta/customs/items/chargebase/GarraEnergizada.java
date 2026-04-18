package org.delta.customs.items.chargebase;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class GarraEnergizada implements CustomItem {
    @Override public String getKey() { return "garra_energizada"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.PHANTOM_MEMBRANE)
                .setDisplayName("&c&lGarra Energizada")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &cAtacante&7."), ItemBuilder.format("&8Clase: &cAtacante")))
                .build();
    }
}

package org.delta.customs.items.chargebase;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FragmentoEscudo implements CustomItem {
    @Override public String getKey() { return "fragmento_escudo"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.SHIELD)
                .setDisplayName("&b&lFragmento de Escudo")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &bDefensor&7."), ItemBuilder.format("&8Clase: &bDefensor")))
                .build();
    }
}
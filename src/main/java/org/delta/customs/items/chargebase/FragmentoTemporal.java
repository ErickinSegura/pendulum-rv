package org.delta.customs.items.chargebase;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FragmentoTemporal implements CustomItem {
    @Override public String getKey() { return "fragmento_temporal"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.CLOCK)
                .setDisplayName("&e&lFragmento Temporal")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &eControlador&7."), ItemBuilder.format("&8Clase: &eControlador")))
                .build();
    }
}

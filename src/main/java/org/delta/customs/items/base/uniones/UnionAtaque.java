package org.delta.customs.items.base.uniones;
import org.bukkit.Material;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;
import org.bukkit.inventory.ItemStack;

public class UnionAtaque implements CustomItem {
    @Override public String getKey() { return "union_ataque"; }
    @Override public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&cUnión de Ataque"))
                .setCustomModelData(3)
                .build();
    }
}
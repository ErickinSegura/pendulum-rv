package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class VaritaBarreraMejorada implements CustomItem {
    @Override
    public String getKey() {
        return "varita_barrera_mejorada";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.END_ROD)
                .setDisplayName(ItemBuilder.format("&bVarita de Barrera Mejorada"))
                .addEnchant(Enchantment.UNBREAKING, 2)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(2)
                .build();
    }
}

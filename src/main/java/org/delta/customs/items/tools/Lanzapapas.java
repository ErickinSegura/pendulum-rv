package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class Lanzapapas implements CustomItem {
    @Override
    public String getKey() {
        return "lanzapapas";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.CROSSBOW)
                .setDisplayName(ItemBuilder.format("&6Lanzapapas"))
                .addEnchant(Enchantment.INFINITY, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .addItemFlag(ItemFlag.HIDE_ADDITIONAL_TOOLTIP)
                .setCustomModelData(1)
                .build();
    }
}
package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.delta.customs.items.CustomItem;

public class Lanzapapas implements CustomItem {
    @Override
    public String getKey() {
        return "lanzapapas";
    }

    @Override
    public org.bukkit.inventory.ItemStack build() {
        return new org.delta.libs.builders.ItemBuilder(Material.CROSSBOW)
                .setDisplayName(org.delta.libs.builders.ItemBuilder.format("&6Lanzapapas"))
                .addEnchant(Enchantment.INFINITY, 1)
                .addItemFlag(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(1)
                .build();
    }

}

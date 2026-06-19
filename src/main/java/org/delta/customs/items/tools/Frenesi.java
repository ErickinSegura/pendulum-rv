package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Frenesi implements CustomItem {
    @Override
    public String getKey() {
        return "frenesi";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.BLAZE_POWDER)
                .setDisplayName(ItemBuilder.format("&cFrenesí"))
                .addEnchant(Enchantment.UNBREAKING, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(1)
                .build();
    }
}

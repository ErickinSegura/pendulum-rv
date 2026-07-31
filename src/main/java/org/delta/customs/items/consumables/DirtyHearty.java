package org.delta.customs.items.consumables;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class DirtyHearty implements CustomItem {

    @Override
    public String getKey() {
        return "dirty_hearty";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.ROTTEN_FLESH)
                .setDisplayName(ItemBuilder.format("&c&lDirty Hearty"))
                .setCustomModelData(1)
                .addEnchant(Enchantment.INFINITY, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .build();
    }
}

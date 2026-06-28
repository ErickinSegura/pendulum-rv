package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Lazo implements CustomItem {
    @Override
    public String getKey() {
        return "lazo";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.FISHING_ROD)
                .setDisplayName(ItemBuilder.format("&aLazo"))
                .addEnchant(Enchantment.UNBREAKING, 3)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setUnbrekeable(true)
                .setCustomModelData(1)
                .setMaxStackSize(1)
                .build();
    }
}

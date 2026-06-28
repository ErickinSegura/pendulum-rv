package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class VaritaBarrera implements CustomItem {
    @Override
    public String getKey() {
        return "varita_barrera";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.END_ROD)
                .setDisplayName(ItemBuilder.format("&3Varita de Barrera"))
                .addEnchant(Enchantment.UNBREAKING, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(1)
                .setMaxStackSize(1)
                .build();
    }
}

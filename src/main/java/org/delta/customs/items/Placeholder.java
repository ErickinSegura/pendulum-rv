package org.delta.customs.items;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Placeholder implements CustomItem {
    @Override
    public String getKey() {
        return "placeholder";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.STICK)
                .setDisplayName(ItemBuilder.format("Placeholder"))
                .setLore(List.of(
                        ItemBuilder.format("Item placeholder y debug de Pendulum."),
                        ItemBuilder.format("En caso de encontrar este item en servidor final, reportar a Administración")
                ))
                .addEnchant(Enchantment.MENDING, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(1)
                .build();
    }
}
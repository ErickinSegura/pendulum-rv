package org.delta.customs.items.chargebase.crafteos.lanzapapas;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

public class PapaExplosiva implements CustomItem {
    @Override
    public String getKey() {
        return "papa_explosiva";
    }

    @Override
    public ItemStack build() {
        return new ItemBuilder(Material.POTATO)
                .setDisplayName(ItemBuilder.format("&cPapa Explosiva"))
                .addEnchant(Enchantment.MENDING, 1)
                .addItemFlag(ItemFlag.HIDE_ENCHANTS)
                .setCustomModelData(1)
                .build();
    }
}

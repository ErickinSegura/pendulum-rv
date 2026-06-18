package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Clawn implements CustomItem {
    @Override
    public String getKey() {
        return "clawn";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.POPPED_CHORUS_FRUIT)
                .setDisplayName(ItemBuilder.format("&6Clawn"))
                .setLore(List.of(
                        ItemBuilder.format("&7Aumenta el rango de acción en &a+4&7,"),
                        ItemBuilder.format("&7pero reduce tu daño en &c-5&7."),
                        ItemBuilder.format("&8No funciona con mazo ni lanza.")
                ))
                .setCustomModelData(1)
                .build();
    }
}

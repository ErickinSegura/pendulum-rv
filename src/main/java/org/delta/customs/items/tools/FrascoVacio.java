package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class FrascoVacio implements CustomItem {
    @Override
    public String getKey() {
        return "frasco_vacio";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.SHULKER_SHELL)
                .setDisplayName(ItemBuilder.format("&5Frasco del Vacío"))
                .setLore(List.of(
                        ItemBuilder.format("&7Click izquierdo a un mob pasivo o"),
                        ItemBuilder.format("&7neutral para guardarlo."),
                        ItemBuilder.format("&7Click derecho al suelo para liberarlo.")
                ))
                .setCustomModelData(1)
                .setMaxStackSize(1)
                .build();
    }
}

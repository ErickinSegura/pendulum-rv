package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.libs.builders.CustomRecipeBuilder;

public class ManzanaDoradaEncantadaCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        return CustomRecipeBuilder.of("manzana_dorada_encantada")
                .result(new ItemStack(Material.ENCHANTED_GOLDEN_APPLE))
                .ingredient(0, Material.GOLD_BLOCK, 2)
                .ingredient(1, Material.GOLD_BLOCK, 2)
                .ingredient(2, Material.GOLD_BLOCK, 2)
                .ingredient(3, Material.GOLD_BLOCK, 2)
                .ingredient(4, Material.APPLE, 1)
                .ingredient(5, Material.GOLD_BLOCK, 2)
                .ingredient(6, Material.GOLD_BLOCK, 2)
                .ingredient(7, Material.GOLD_BLOCK, 2)
                .ingredient(8, Material.GOLD_BLOCK, 2)
                .build();
    }
}

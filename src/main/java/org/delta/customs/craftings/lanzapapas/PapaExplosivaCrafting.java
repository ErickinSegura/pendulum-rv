package org.delta.customs.craftings.lanzapapas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.PapaExplosiva;
import org.delta.libs.builders.CustomRecipeBuilder;

public class PapaExplosivaCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack papaExplosiva = new PapaExplosiva().build();

        return CustomRecipeBuilder.of("lanzapapas")
                .result(papaExplosiva)
                .ingredient(0, Material.GUNPOWDER, 1)
                .ingredient(1, Material.MAGMA_CREAM, 1)
                .ingredient(2, Material.GUNPOWDER, 1)
                .ingredient(3, Material.BLAZE_POWDER, 1)
                .ingredient(4, Material.POTATO, 1)
                .ingredient(5, Material.BLAZE_POWDER, 1)
                .ingredient(6, Material.GUNPOWDER, 1)
                .ingredient(7, Material.MAGMA_CREAM, 1)
                .ingredient(8, Material.GUNPOWDER, 1)
                .build();
    }
}

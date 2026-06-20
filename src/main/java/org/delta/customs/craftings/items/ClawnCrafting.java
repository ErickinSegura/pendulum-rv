package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoControl;
import org.delta.customs.items.tools.Clawn;
import org.delta.libs.builders.CustomRecipeBuilder;

public class ClawnCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoControl().build();

        return CustomRecipeBuilder.of("clawn")
                .result(new Clawn().build())
                .ingredient(0, Material.COPPER_BLOCK, 1)
                .ingredient(1, Material.COPPER_INGOT, 1)
                .ingredient(3, Material.COPPER_INGOT, 1)
                .ingredient(4, nucleo)
                .ingredient(6, Material.COPPER_BLOCK, 1)
                .ingredient(7, Material.COPPER_INGOT, 1)
                .build();
    }
}

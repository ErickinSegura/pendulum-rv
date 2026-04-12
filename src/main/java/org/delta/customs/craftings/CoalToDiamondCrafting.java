package org.delta.customs.craftings;

import org.bukkit.Material;
import org.delta.libs.builders.CustomRecipeBuilder;

public class CoalToDiamondCrafting implements CustomCrafting {

    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        return CustomRecipeBuilder.of("coal_to_diamond")
                .result(Material.DIAMOND)
                .ingredient(4, Material.COAL, 5)
                .build();
    }
}
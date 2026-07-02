package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.consumables.DirtyHearty;
import org.delta.libs.builders.CustomRecipeBuilder;

public class DirtyHeartyCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        return CustomRecipeBuilder.of("dirty_hearty")
                .result(new DirtyHearty().build())
                .ingredient(0, Material.GOLD_BLOCK, 1)
                .ingredient(1, Material.GOLD_BLOCK, 1)
                .ingredient(2, Material.GOLD_BLOCK, 1)
                .ingredient(3, Material.GOLD_BLOCK, 1)
                .ingredient(4, Material.PLAYER_HEAD, 1)
                .ingredient(5, Material.GOLD_BLOCK, 1)
                .ingredient(6, Material.GOLD_BLOCK, 1)
                .ingredient(7, Material.GOLD_BLOCK, 1)
                .ingredient(8, Material.GOLD_BLOCK, 1)
                .build();
    }
}

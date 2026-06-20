package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoAtacante;
import org.delta.customs.items.tools.Frenesi;
import org.delta.libs.builders.CustomRecipeBuilder;

public class FrenesiCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoAtacante().build();

        return CustomRecipeBuilder.of("frenesi")
                .result(new Frenesi().build())
                .ingredient(0, Material.MAGMA_CREAM, 1)
                .ingredient(1, Material.BLAZE_ROD, 1)
                .ingredient(2, Material.MAGMA_CREAM, 1)
                .ingredient(3, Material.BLAZE_POWDER, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.BLAZE_POWDER, 1)
                .ingredient(6, Material.MAGMA_CREAM, 1)
                .ingredient(7, Material.GUNPOWDER, 1)
                .ingredient(8, Material.MAGMA_CREAM, 1)
                .build();
    }
}

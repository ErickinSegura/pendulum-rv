package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoHealHibrido;
import org.delta.customs.items.tools.VaritaCurativa;
import org.delta.libs.builders.CustomRecipeBuilder;

public class VaritaCurativaCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoHealHibrido().build();

        return CustomRecipeBuilder.of("varita_curativa")
                .result(new VaritaCurativa().build())
                .ingredient(0, Material.GLOWSTONE_DUST, 1)
                .ingredient(1, Material.GLISTERING_MELON_SLICE, 1)
                .ingredient(2, Material.GLOWSTONE_DUST, 1)
                .ingredient(3, Material.GHAST_TEAR, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.GHAST_TEAR, 1)
                .ingredient(6, Material.GLISTERING_MELON_SLICE, 1)
                .ingredient(7, Material.BLAZE_ROD, 1)
                .ingredient(8, Material.GLISTERING_MELON_SLICE, 1)
                .build();
    }
}

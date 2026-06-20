package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoAtacanteHibrido;
import org.delta.customs.items.tools.Lanzapapas;
import org.delta.libs.builders.CustomRecipeBuilder;

public class LanzapapasCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoAtacanteHibrido().build();

        return CustomRecipeBuilder.of("lanzapapas")
                .result(new Lanzapapas().build())
                .ingredient(0, Material.IRON_INGOT, 1)
                .ingredient(1, Material.CROSSBOW, 1)
                .ingredient(2, Material.IRON_INGOT, 1)
                .ingredient(3, Material.STRING, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.STRING, 1)
                .ingredient(6, Material.GUNPOWDER, 1)
                .ingredient(7, Material.POTATO, 1)
                .ingredient(8, Material.GUNPOWDER, 1)
                .build();
    }
}

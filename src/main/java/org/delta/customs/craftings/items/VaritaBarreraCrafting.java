package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoDefensor;
import org.delta.customs.items.tools.VaritaBarrera;
import org.delta.libs.builders.CustomRecipeBuilder;

public class VaritaBarreraCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoDefensor().build();

        return CustomRecipeBuilder.of("varita_barrera")
                .result(new VaritaBarrera().build())
                .ingredient(0, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(1, Material.END_ROD, 1)
                .ingredient(2, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(3, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(6, Material.IRON_INGOT, 1)
                .ingredient(7, Material.SHIELD, 1)
                .ingredient(8, Material.IRON_INGOT, 1)
                .build();
    }
}

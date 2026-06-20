package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoDefensorHibrido;
import org.delta.customs.items.tools.VaritaBarrera;
import org.delta.customs.items.tools.VaritaBarreraMejorada;
import org.delta.libs.builders.CustomRecipeBuilder;

public class VaritaBarreraMejoradaCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoDefensorHibrido().build();

        return CustomRecipeBuilder.of("varita_barrera_mejorada")
                .result(new VaritaBarreraMejorada().build())
                .ingredient(0, Material.DIAMOND, 1)
                .ingredient(2, Material.DIAMOND, 1)
                .ingredient(3, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.CYAN_STAINED_GLASS, 1)
                .ingredient(6, Material.DIAMOND, 1)
                .ingredient(8, Material.DIAMOND, 1)
                .build();
    }
}

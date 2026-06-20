package org.delta.customs.craftings.items.nucleos;

import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.fragmentos.FragmentoDefensa;
import org.delta.customs.items.base.nucleos.NucleoDefensor;
import org.delta.customs.items.base.uniones.UnionDefensa;
import org.delta.libs.builders.CustomRecipeBuilder;

public class NucleoDefensorCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack union = new UnionDefensa().build();
        ItemStack fragmento = new FragmentoDefensa().build();

        return CustomRecipeBuilder.of("nucleo_defensor")
                .result(new NucleoDefensor().build())
                .ingredient(1, fragmento)
                .ingredient(3, fragmento)
                .ingredient(4, union)
                .ingredient(5, fragmento)
                .ingredient(7, fragmento)
                .build();
    }
}

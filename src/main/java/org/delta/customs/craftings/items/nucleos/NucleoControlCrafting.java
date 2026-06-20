package org.delta.customs.craftings.items.nucleos;

import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.fragmentos.FragmentoControl;
import org.delta.customs.items.base.nucleos.NucleoControl;
import org.delta.customs.items.base.uniones.UnionControl;
import org.delta.libs.builders.CustomRecipeBuilder;

public class NucleoControlCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack union = new UnionControl().build();
        ItemStack fragmento = new FragmentoControl().build();

        return CustomRecipeBuilder.of("nucleo_control")
                .result(new NucleoControl().build())
                .ingredient(1, fragmento)
                .ingredient(3, fragmento)
                .ingredient(4, union)
                .ingredient(5, fragmento)
                .ingredient(7, fragmento)
                .build();
    }
}

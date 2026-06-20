package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoControlHibrido;
import org.delta.customs.items.tools.Lazo;
import org.delta.libs.builders.CustomRecipeBuilder;

public class LazoCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoControlHibrido().build();

        return CustomRecipeBuilder.of("lazo")
                .result(new Lazo().build())
                .ingredient(0, Material.STRING, 1)
                .ingredient(1, Material.TRIPWIRE_HOOK, 1)
                .ingredient(2, Material.STRING, 1)
                .ingredient(3, Material.SLIME_BALL, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.SLIME_BALL, 1)
                .ingredient(6, Material.STRING, 1)
                .ingredient(7, Material.FISHING_ROD, 1)
                .ingredient(8, Material.STRING, 1)
                .build();
    }
}

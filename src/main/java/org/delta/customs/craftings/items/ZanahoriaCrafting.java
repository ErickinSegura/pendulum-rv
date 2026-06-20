package org.delta.customs.craftings.items;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.nucleos.NucleoHeal;
import org.delta.customs.items.consumables.ZanahoriaEncantada;
import org.delta.libs.builders.CustomRecipeBuilder;

public class ZanahoriaCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack nucleo = new NucleoHeal().build();

        return CustomRecipeBuilder.of("zanahoria_encantada")
                .result(new ZanahoriaEncantada().build())
                .ingredient(0, Material.GOLD_INGOT, 1)
                .ingredient(1, Material.GOLDEN_CARROT, 1)
                .ingredient(2, Material.GOLD_INGOT, 1)
                .ingredient(3, Material.GOLDEN_CARROT, 1)
                .ingredient(4, nucleo)
                .ingredient(5, Material.GOLDEN_CARROT, 1)
                .ingredient(6, Material.GOLD_INGOT, 1)
                .ingredient(7, Material.GLISTERING_MELON_SLICE, 1)
                .ingredient(8, Material.GOLD_INGOT, 1)
                .build();
    }
}

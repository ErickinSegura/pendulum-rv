package org.delta.customs.craftings.lanzapapas;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.chargebase.crafteos.lanzapapas.PapaExplosiva;
import org.delta.libs.builders.CustomRecipeBuilder;

public class LanzapapasCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack papaExplosiva = new PapaExplosiva().build();

        return CustomRecipeBuilder.of("lanzapapas")
                .result(papaExplosiva)
                .ingredient(2, papaExplosiva)
                .ingredient(3, papaExplosiva)
                .build();
    }
}

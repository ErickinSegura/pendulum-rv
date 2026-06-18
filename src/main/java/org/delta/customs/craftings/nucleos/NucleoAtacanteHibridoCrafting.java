package org.delta.customs.craftings.nucleos;

import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.fragmentos.FragmentoAtaque;
import org.delta.customs.items.base.nucleos.NucleoAtacanteHibrido;
import org.delta.customs.items.base.uniones.UnionHibrida;
import org.delta.libs.builders.CustomRecipeBuilder;

public class NucleoAtacanteHibridoCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack union = new UnionHibrida().build();
        ItemStack fragmento = new FragmentoAtaque().build();

        return CustomRecipeBuilder.of("nucleo_atacante_hibrido")
                .result(new NucleoAtacanteHibrido().build())
                .ingredient(1, fragmento)
                .ingredient(3, fragmento)
                .ingredient(4, union)
                .ingredient(5, fragmento)
                .ingredient(7, fragmento)
                .build();
    }
}

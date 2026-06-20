package org.delta.customs.craftings.items.nucleos;

import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.fragmentos.FragmentoHeal;
import org.delta.customs.items.base.nucleos.NucleoHealHibrido;
import org.delta.customs.items.base.uniones.UnionHibrida;
import org.delta.libs.builders.CustomRecipeBuilder;

public class NucleoHealHibridoCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack union = new UnionHibrida().build();
        ItemStack fragmento = new FragmentoHeal().build();

        return CustomRecipeBuilder.of("nucleo_heal_hibrido")
                .result(new NucleoHealHibrido().build())
                .minDay(10)
                .ingredient(1, fragmento)
                .ingredient(3, fragmento)
                .ingredient(4, union)
                .ingredient(5, fragmento)
                .ingredient(7, fragmento)
                .build();
    }
}

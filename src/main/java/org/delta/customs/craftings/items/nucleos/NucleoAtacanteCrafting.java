package org.delta.customs.craftings.items.nucleos;

import org.bukkit.inventory.ItemStack;
import org.delta.customs.craftings.CustomCrafting;
import org.delta.customs.items.base.fragmentos.FragmentoAtaque;
import org.delta.customs.items.base.nucleos.NucleoAtacante;
import org.delta.customs.items.base.uniones.UnionAtaque;
import org.delta.libs.builders.CustomRecipeBuilder;

public class NucleoAtacanteCrafting implements CustomCrafting {
    @Override
    public CustomRecipeBuilder.CustomRecipe build() {
        ItemStack union = new UnionAtaque().build();
        ItemStack fragmento = new FragmentoAtaque().build();

        return CustomRecipeBuilder.of("nucleo_atacante")
                .result(new NucleoAtacante().build())
                .ingredient(1, fragmento)
                .ingredient(3, fragmento)
                .ingredient(4, union)
                .ingredient(5, fragmento)
                .ingredient(7, fragmento)
                .build();
    }
}

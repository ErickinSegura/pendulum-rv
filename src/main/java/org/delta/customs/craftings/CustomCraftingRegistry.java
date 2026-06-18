package org.delta.customs.craftings;

import org.delta.customs.craftings.lanzapapas.PapaExplosivaCrafting;
import org.delta.customs.craftings.nucleos.*;
import org.delta.listeners.player.CustomCraftingListener;

import java.util.List;

public class CustomCraftingRegistry {

    private static final List<CustomCrafting> RECIPES = List.of(
            new PapaExplosivaCrafting(),
            // Núcleos por clase
            new NucleoAtacanteCrafting(),
            new NucleoDefensorCrafting(),
            new NucleoHealCrafting(),
            new NucleoControlCrafting(),
            // Núcleos con unión híbrida
            new NucleoAtacanteHibridoCrafting(),
            new NucleoDefensorHibridoCrafting(),
            new NucleoHealHibridoCrafting(),
            new NucleoControlHibridoCrafting()
    );

    public static void register(CustomCraftingListener listener) {
        RECIPES.forEach(crafting -> listener.registerRecipe(crafting.build()));
    }
}

package org.delta.customs.craftings;

import org.delta.customs.craftings.items.*;
import org.delta.customs.craftings.items.PapaExplosivaCrafting;
import org.delta.customs.craftings.items.nucleos.*;
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
            new NucleoControlHibridoCrafting(),
            // Items custom (cada uno requiere su núcleo)
            new FrenesiCrafting(),
            new LanzapapasCrafting(),
            new ZanahoriaCrafting(),
            new VaritaCurativaCrafting(),
            new ClawnCrafting(),
            new LazoCrafting(),
            new VaritaBarreraCrafting(),
            new VaritaBarreraMejoradaCrafting(),
            new DirtyHeartyCrafting(),
            new ManzanaDoradaEncantadaCrafting()
    );

    public static void register(CustomCraftingListener listener) {
        RECIPES.forEach(crafting -> listener.registerRecipe(crafting.build()));
    }
}

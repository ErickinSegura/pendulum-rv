package org.delta.customs.items;

import org.delta.customs.items.base.fragmentos.FragmentoHeal;
import org.delta.customs.items.base.fragmentos.FragmentoDefensa;
import org.delta.customs.items.base.fragmentos.FragmentoControl;
import org.delta.customs.items.base.fragmentos.FragmentoAtaque;
import org.delta.customs.items.base.nucleos.*;
import org.delta.customs.items.base.uniones.*;
import org.delta.customs.items.modifier.UnbreakeableModifier;
import org.delta.customs.items.modifier.LivianoModifier;
import org.delta.customs.items.modifier.TempleModifier;
import org.delta.customs.items.tools.Lanzapapas;
import org.delta.customs.items.base.PapaExplosiva;
import org.delta.customs.items.tools.Clawn;
import org.delta.customs.items.tools.Lazo;
import org.delta.customs.items.tools.VaritaCurativa;
import org.delta.customs.items.tools.VaritaBarrera;
import org.delta.customs.items.tools.VaritaBarreraMejorada;
import org.delta.customs.items.tools.Frenesi;
import org.delta.customs.items.tools.AnclaVinculo;
import org.delta.customs.items.tools.FrascoVacio;
import org.delta.customs.items.consumables.ZanahoriaEncantada;
import org.delta.customs.items.consumables.DirtyHearty;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ItemRegistry {

    private static final Map<String, CustomItem> ITEMS = new HashMap<>();

    static {
        register(new Clawn());
        // ChargeBase drops
        register(new UnionAtaque());
        register(new FragmentoAtaque());
        register(new UnionDefensa());
        register(new FragmentoDefensa());
        register(new FragmentoHeal());
        register(new UnionHeal());
        register(new FragmentoControl());
        register(new UnionControl());
        register(new UnionHibrida());

        // Núcleos por clase
        register(new NucleoAtacante());
        register(new NucleoDefensor());
        register(new NucleoHeal());
        register(new NucleoControl());

        // Núcleos con unión híbrida
        register(new NucleoAtacanteHibrido());
        register(new NucleoDefensorHibrido());
        register(new NucleoHealHibrido());
        register(new NucleoControlHibrido());

        // Modifiers
        register(new UnbreakeableModifier());
        register(new LivianoModifier());
        register(new TempleModifier());

        register(new Placeholder());

        register(new PapaExplosiva());
        register(new Lanzapapas());
        register(new Lazo());
        register(new VaritaCurativa());
        register(new VaritaBarrera());
        register(new VaritaBarreraMejorada());
        register(new Frenesi());
        register(new AnclaVinculo());
        register(new FrascoVacio());
        register(new ZanahoriaEncantada());
        register(new DirtyHearty());
    }

    private static void register(CustomItem item) {
        ITEMS.put(item.getKey(), item);
    }

    public static Optional<CustomItem> get(String key) {
        return Optional.ofNullable(ITEMS.get(key.toLowerCase()));
    }

    public static Set<String> getKeys() {
        return ITEMS.keySet();
    }
}
package org.delta.customs.items;

import org.delta.customs.items.chargebase.*;
import org.delta.customs.items.chargebase.crafteos.lanzapapas.Lanzapapas;
import org.delta.customs.items.chargebase.crafteos.lanzapapas.PapaExplosiva;
import org.delta.customs.items.tools.Clawn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ItemRegistry {

    private static final Map<String, CustomItem> ITEMS = new HashMap<>();

    static {
        register(new Clawn());
        // ChargeBase drops
        register(new NucleoImpulso());
        register(new GarraEnergizada());
        register(new NucleoProteccion());
        register(new FragmentoEscudo());
        register(new EsenciaVital());
        register(new NucleoRestauracion());
        register(new FragmentoTemporal());
        register(new NucleoDistorsion());
        register(new NucleoInestable());
        register(new Placeholder());

        register(new PapaExplosiva());
        register(new Lanzapapas());
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
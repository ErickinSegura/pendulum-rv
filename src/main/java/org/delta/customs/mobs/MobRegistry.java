package org.delta.customs.mobs;

import org.bukkit.Location;
import org.delta.customs.mobs.chargebase.atacante.AtacanteAvanzado;
import org.delta.customs.mobs.chargebase.controlador.ControladorAvanzado;
import org.delta.customs.mobs.chargebase.defensor.DefensorAvanzado;
import org.delta.customs.mobs.chargebase.healer.HealerAvanzado;
import org.delta.customs.mobs.chargebase.hibrido.HibridoAvanzado;
import org.delta.customs.mobs.zombie_test.ZombieTest;
import org.delta.customs.mobs.chargebase.defensor.DefensorBasico;
import org.delta.customs.mobs.chargebase.healer.HealerBasico;
import org.delta.customs.mobs.chargebase.atacante.AtacanteBasico;
import org.delta.customs.mobs.chargebase.controlador.ControladorBasico;
import org.delta.customs.mobs.chargebase.hibrido.HibridoBasico;
import org.delta.pendulum;

import java.util.*;
import java.util.function.BiFunction;

public class MobRegistry {

    private static final Map<String, BiFunction<pendulum, Location, CustomMob>> FACTORIES = new HashMap<>();

    static {
        // ChargeBase
        register("atacante_basico", AtacanteBasico::new);
        register("atacante_avanzado", AtacanteAvanzado::new);
        register("defensor_basico", DefensorBasico::new);
        register("defensor_avanzado", DefensorAvanzado::new);
        register("healer_basico", HealerBasico::new);
        register("healer_avanzado", HealerAvanzado::new);
        register("controlador_basico", ControladorBasico::new);
        register("controlador_avanzado", ControladorAvanzado::new);
        register("hibrido_basico", HibridoBasico::new);
        register("hibrido_avanzado", HibridoAvanzado::new);
    }

    private static void register(String key, BiFunction<pendulum, Location, CustomMob> factory) {
        FACTORIES.put(key.toLowerCase(), factory);
    }

    public static Optional<CustomMob> get(String key, pendulum plugin, Location location) {
        BiFunction<pendulum, Location, CustomMob> factory = FACTORIES.get(key.toLowerCase());
        if (factory == null) return Optional.empty();
        return Optional.of(factory.apply(plugin, location));
    }

    public static Set<String> getKeys() {
        return FACTORIES.keySet();
    }
}
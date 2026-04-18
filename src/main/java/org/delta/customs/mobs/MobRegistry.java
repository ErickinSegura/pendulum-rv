package org.delta.customs.mobs;

import org.bukkit.Location;
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
        register("zombie_vengador", ZombieTest::new);
        // ChargeBase
        register("atacante_basico", AtacanteBasico::new);
        register("defensor_basico", DefensorBasico::new);
        register("healer_basico", HealerBasico::new);
        register("controlador_basico", ControladorBasico::new);
        register("hibrido_basico", HibridoBasico::new);
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
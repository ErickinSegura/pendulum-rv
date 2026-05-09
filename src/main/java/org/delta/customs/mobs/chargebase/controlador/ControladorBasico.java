package org.delta.customs.mobs.chargebase.controlador;


import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class ControladorBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public ControladorBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.CONTROLADOR; }

    @Override
    public String getKey() { return "controlador_basico"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.ILLUSIONER)
                .setCustomName("&5&lControlador Básico")
                .setCustomNameVisible(true)
                .setMaxHealth(60)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        return entity;
    }

}
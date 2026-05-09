package org.delta.customs.mobs.chargebase.controlador;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class ControladorAvanzado implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public ControladorAvanzado(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.CONTROLADOR; }

    @Override
    public String getKey() { return "controlador_avanzado"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.BREEZE)
                .setCustomName("&5&lControl Avanzado")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.35)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        return entity;
    }

    @Override
    public double getKnockbackStrength() { return 5.0; }

}

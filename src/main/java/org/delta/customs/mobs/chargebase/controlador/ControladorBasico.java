package org.delta.customs.mobs.chargebase.controlador;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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

    public MobClass getMobClass() { return MobClass.CONTROLADOR; }

    @Override public String getKey() { return "controlador_basico"; }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.SHULKER)
                .setCustomName("&e&lControlador")
                .setCustomNameVisible(true)
                .setMaxHealth(35)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.25)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, Integer.MAX_VALUE, 0, false, false))
                .build(location);
    }
}
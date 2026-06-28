package org.delta.customs.mobs.boss;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class CustodioVacio implements CustomMob {

    public static final String TAG = "custodio_vacio";

    private final pendulum plugin;
    private final Location location;

    public CustodioVacio(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() {
        return null;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.ENDERMAN)
                .setCustomName("&5&lCustodio del Vacío")
                .setCustomNameVisible(true)
                .setMaxHealth(350)
                .setHealth(350)
                .setRemovable(false)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.32)
                .setAttribute(Attribute.ATTACK_DAMAGE, 6.0)
                .setAttribute(Attribute.KNOCKBACK_RESISTANCE, 0.7)
                .setAttribute(Attribute.FOLLOW_RANGE, 45.0)
                .setAttribute(Attribute.SCALE, 1.6)
                .setBossBar(plugin, "&5&lCustodio del Vacío", BarColor.PURPLE, BarStyle.SEGMENTED_10)
                .setBossBarRange(60.0)
                .build(location);

        entity.addScoreboardTag(getKey());
        return entity;
    }
}

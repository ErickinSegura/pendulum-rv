package org.delta.customs.mobs.chargebase.defensor;

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

public class DefensorBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public DefensorBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    public MobClass getMobClass() { return MobClass.DEFENSOR; }

    @Override public String getKey() { return "defensor_basico"; }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.IRON_GOLEM)
                .setCustomName("&b&lDefensor")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.2)
                .setAttribute(Attribute.ATTACK_DAMAGE, 8.0)
                .setAttribute(Attribute.KNOCKBACK_RESISTANCE, 0.8)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false))
                .build(location);
    }
}
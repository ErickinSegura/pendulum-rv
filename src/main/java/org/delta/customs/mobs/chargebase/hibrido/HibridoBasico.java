package org.delta.customs.mobs.chargebase.hibrido;

import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class HibridoBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HibridoBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.HIBRIDO; }

    @Override
    public String getKey() { return "hibrido_basico"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.CREEPER)
                .setCustomName("&d&lHibrido Básico")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.42)
                .setAttribute(Attribute.SCALE, 0.7)
                .setRemovable(false)
                .build(location);

        Creeper creeper = (Creeper) entity;
        creeper.setPowered(true);
        creeper.setMaxFuseTicks(60);
        creeper.setExplosionRadius(3);

        entity.addScoreboardTag(getKey());
        return entity;
    }

}
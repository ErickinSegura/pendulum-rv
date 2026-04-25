package org.delta.customs.mobs.chargebase.defensor;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class DefensorBasico implements CustomMob {

    private final pendulum plugin;
    private final Location location;

    public DefensorBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.DEFENSOR; }

    @Override
    public String getKey() { return "defensor_basico"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.SNIFFER)
                .setCustomName("&b&lDefensor")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED, 0.25)
                .setAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE, 0.8)
                .setAttribute(Attribute.SCALE, 1.3)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(
                        PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false))
                .build(location);

        applyNMSBehavior(entity);
        return entity;
    }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.clearBrain(nms);
        NMSEntityUtils.setAttackDamage(nms, 10.0);
        NMSEntityUtils.setFollowRange(nms, 20.0);
        NMSEntityUtils.setAttackKnockback(nms, 20.0);
        NMSEntityUtils.applyMeleeGoals(nms);
    }
}
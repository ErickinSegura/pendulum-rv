package org.delta.customs.mobs.chargebase.defensor;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class DefensorAvanzado implements CustomMob {

    private final pendulum plugin;
    private final Location location;

    public DefensorAvanzado(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.DEFENSOR; }

    @Override
    public String getKey() {
        return "defensor_avanzado";
    }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.IRON_GOLEM)
                .setCustomName("&b&lMultimedios")
                .setCustomNameVisible(true)
                .setMaxHealth(150)
                .setAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED, 0.25)
                .setAttribute(org.bukkit.attribute.Attribute.KNOCKBACK_RESISTANCE, 0.8)
                .setAttribute(Attribute.SCALE, 2)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(
                        PotionEffectType.RESISTANCE, Integer.MAX_VALUE, 0, false, false))
                .build(location);

        entity.addScoreboardTag(getKey());
        applyNMSBehavior(entity);
        return entity;
    }

    @Override
    public double getKnockbackStrength() { return 10; }

    @Override
    public double getKnockbackVertical() { return 1.2; }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.clearBrain(nms);

        AttributeMap map = nms.getAttributes();
        if (!map.hasAttribute(Attributes.FOLLOW_RANGE)) {
            map.registerAttribute(Attributes.FOLLOW_RANGE);
        }

        NMSEntityUtils.setFollowRange(nms, 20.0);
        NMSEntityUtils.applyMeleeGoals(nms);
    }
}

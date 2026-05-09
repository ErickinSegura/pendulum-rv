package org.delta.customs.mobs.chargebase.hibrido;

import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class HibridoAvanzado implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HibridoAvanzado(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.HIBRIDO; }

    @Override
    public String getKey() { return "hibrido_avanzado"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.WARDEN)
                .setCustomName("&d&lHibrido Avanzado")
                .setCustomNameVisible(true)
                .setMaxHealth(120)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.32)
                .setAttribute(Attribute.ATTACK_DAMAGE, 10.0)
                .setAttribute(Attribute.SCALE, 1.3)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        applyNMSBehavior(entity);
        return entity;
    }

    @Override
    public double getKnockbackStrength() { return 8.0; }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.setFollowRange(nms, 40.0);
        NMSEntityUtils.setAttackDamage(nms, 18.0);
    }
}
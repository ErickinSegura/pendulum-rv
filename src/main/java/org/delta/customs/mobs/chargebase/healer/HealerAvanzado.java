package org.delta.customs.mobs.chargebase.healer;

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

public class HealerAvanzado implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HealerAvanzado(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.HEALER; }

    @Override
    public String getKey() { return "healer_avanzado"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.ALLAY)
                .setCustomName("&a&lAntonio")
                .setCustomNameVisible(true)
                .setMaxHealth(140)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.38)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        applyNMSBehavior(entity);
        return entity;
    }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.clearBrain(nms);
        NMSEntityUtils.setFollowRange(nms, 30.0);
        NMSEntityUtils.applyFleeGoals(nms, 14.0f);
    }
}

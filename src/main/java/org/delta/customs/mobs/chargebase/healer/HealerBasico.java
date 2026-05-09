package org.delta.customs.mobs.chargebase.healer;

import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.MobBuilder;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class HealerBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HealerBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.HEALER; }

    @Override
    public String getKey() { return "healer_basico"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.WITCH)
                .setCustomName("&a&lHealer Básico")
                .setCustomNameVisible(true)
                .setMaxHealth(40)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        applyNMSBehavior(entity);
        return entity;
    }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.clearBrain(nms);
        NMSEntityUtils.setFollowRange(nms, 24.0);
        NMSEntityUtils.applyFleeGoals(nms, 10.0f);
    }
}
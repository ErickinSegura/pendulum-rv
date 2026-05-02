package org.delta.customs.mobs;

import org.bukkit.entity.LivingEntity;
import org.delta.customs.mobs.chargebase.MobClass;

public interface CustomMob {
    MobClass getMobClass();

    String getKey();
    LivingEntity build();

    default double getKnockbackStrength() { return 0; }
    default double getKnockbackVertical() { return 0; }
}
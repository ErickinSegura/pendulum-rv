package org.delta.customs.mobs;

import org.bukkit.entity.LivingEntity;

public interface CustomMob {
    String getKey();
    LivingEntity build();
}
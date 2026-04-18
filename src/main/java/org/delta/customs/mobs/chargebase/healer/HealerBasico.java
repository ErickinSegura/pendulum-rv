// Healer
package org.delta.customs.mobs.chargebase.healer;

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

public class HealerBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HealerBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    public MobClass getMobClass() { return MobClass.HEALER; }

    @Override public String getKey() { return "healer_basico"; }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.WITCH)
                .setCustomName("&a&lHealer")
                .setCustomNameVisible(true)
                .setMaxHealth(40)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.28)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 1, false, false))
                .build(location);
    }
}
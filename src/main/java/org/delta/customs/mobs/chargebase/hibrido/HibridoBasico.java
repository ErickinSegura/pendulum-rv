// Híbrido
package org.delta.customs.mobs.chargebase.hibrido;

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

public class HibridoBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public HibridoBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    public MobClass getMobClass() { return MobClass.HIBRIDO; }

    @Override public String getKey() { return "hibrido_basico"; }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.WARDEN)
                .setCustomName("&d&lHíbrido")
                .setCustomNameVisible(true)
                .setMaxHealth(120)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.3)
                .setAttribute(Attribute.ATTACK_DAMAGE, 12.0)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, false))
                .addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, Integer.MAX_VALUE, 0, false, false))
                .build(location);
    }
}
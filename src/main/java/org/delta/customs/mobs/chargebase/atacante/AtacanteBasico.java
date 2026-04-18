// Atacante
package org.delta.customs.mobs.chargebase.atacante;

import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.bukkit.entity.EntityType;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class AtacanteBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public AtacanteBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    public MobClass getMobClass() { return MobClass.ATACANTE; }

    @Override public String getKey() { return "atacante_basico"; }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.ZOMBIE)
                .setCustomName("&c&lAtacante")
                .setCustomNameVisible(true)
                .setMaxHealth(30)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.35)
                .setAttribute(Attribute.ATTACK_DAMAGE, 5.0)
                .setRemovable(false)
                .addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 0, false, false))
                .build(location);
    }
}
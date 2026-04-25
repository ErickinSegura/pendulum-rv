package org.delta.customs.mobs.zombie_test;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.ItemBuilder;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class ZombieTest implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public ZombieTest(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() {
        return null;
    }

    @Override
    public String getKey() {
        return "zombie_vengador";
    }

    @Override
    public LivingEntity build() {
        return new MobBuilder(EntityType.ZOMBIE)
                .setCustomName("&c⚠ Zombie Vengador")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setHealth(80)
                .setGlowing(true)
                .setRemovable(false)
                .setHelmet(new ItemBuilder(Material.IRON_HELMET).build())
                .setMainHand(new ItemBuilder(Material.IRON_SWORD).build())
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1))
                .setBossBar(plugin, "&c⚠ Zombie Vengador &7| &c❤ Vida", BarColor.RED, BarStyle.SEGMENTED_10)
                .setBossBarRange(40.0)
                .addAttributeFlat(Attribute.ATTACK_KNOCKBACK, "zomb_atk_kb", 5)
                .addAttributeFlat(Attribute.SCALE, "zomb_scale", 1.5)
                .build(location);
    }
}

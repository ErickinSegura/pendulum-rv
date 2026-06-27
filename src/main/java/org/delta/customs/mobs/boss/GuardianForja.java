package org.delta.customs.mobs.boss;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.ItemBuilder;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class GuardianForja implements CustomMob {

    public static final String TAG = "guardian_forja";

    private final pendulum plugin;
    private final Location location;

    public GuardianForja(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() {
        return null;
    }

    @Override
    public String getKey() {
        return TAG;
    }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.WITHER_SKELETON)
                .setCustomName("&5&lGuardián de la Forja")
                .setCustomNameVisible(true)
                .setMaxHealth(300)
                .setHealth(300)
                .setRemovable(false)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.28)
                .setAttribute(Attribute.ATTACK_DAMAGE, 4.0)
                .setAttribute(Attribute.KNOCKBACK_RESISTANCE, 0.8)
                .setAttribute(Attribute.FOLLOW_RANGE, 35.0)
                .setAttribute(Attribute.SCALE, 1.8)
                .setMainHand(new ItemBuilder(Material.NETHERITE_AXE)
                        .addAttributeFlat(Attribute.ATTACK_DAMAGE, "guardian_axe_dmg", 2.0, EquipmentSlot.HAND)
                        .addItemFlag(ItemFlag.HIDE_ATTRIBUTES).build())
                .setHelmet(new ItemBuilder(Material.NETHERITE_HELMET)
                        .setArmorTrim(TrimMaterial.NETHERITE, TrimPattern.SENTRY).build())
                .setBossBar(plugin, "&5&l⚒ Guardián de la Forja", BarColor.PURPLE, BarStyle.SEGMENTED_12)
                .setBossBarRange(50.0)
                .build(location);

        entity.addScoreboardTag(getKey());

        EntityEquipment eq = entity.getEquipment();
        if (eq != null) {
            eq.setItemInMainHandDropChance(0f);
            eq.setHelmetDropChance(0f);
        }
        return entity;
    }
}

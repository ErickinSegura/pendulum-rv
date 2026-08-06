package org.delta.customs.mobs.chargebase.atacante;

import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.libs.builders.ItemBuilder;
import org.delta.libs.builders.MobBuilder;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class AtacanteBasico implements CustomMob {
    private final pendulum plugin;
    private final Location location;

    public AtacanteBasico(pendulum plugin, Location location) {
        this.plugin = plugin;
        this.location = location;
    }

    @Override
    public MobClass getMobClass() { return MobClass.ATACANTE; }

    @Override
    public String getKey() { return "atacante_basico"; }

    @Override
    public LivingEntity build() {
        LivingEntity entity = new MobBuilder(EntityType.ZOMBIE)
                .setCustomName("&c&lLa Cebolla")
                .setCustomNameVisible(true)
                .setMaxHealth(60)
                .setAttribute(Attribute.MOVEMENT_SPEED, 0.32)
                .setAttribute(Attribute.ATTACK_DAMAGE, 8.0)
                .setAttribute(Attribute.SCALE, 1.1)
                .setRemovable(false)
                .build(location);

        entity.addScoreboardTag(getKey());
        applyEquipment(entity);
        applyNMSBehavior(entity);
        return entity;
    }

    private void applyEquipment(LivingEntity entity) {
        EntityEquipment eq = entity.getEquipment();

        eq.setHelmet(new ItemStack(Material.DRAGON_HEAD));
        eq.setHelmetDropChance(0f);

        eq.setChestplate(new ItemBuilder(Material.IRON_CHESTPLATE)
                .setArmorTrim(TrimMaterial.COPPER, TrimPattern.WILD).build());
        eq.setLeggings(new ItemBuilder(Material.IRON_LEGGINGS)
                .setArmorTrim(TrimMaterial.COPPER, TrimPattern.WILD).build());
        eq.setBoots(new ItemBuilder(Material.IRON_BOOTS)
                .setArmorTrim(TrimMaterial.COPPER, TrimPattern.WILD).build());

        eq.setChestplateDropChance(0f);
        eq.setLeggingsDropChance(0f);
        eq.setBootsDropChance(0f);
    }

    private void applyNMSBehavior(LivingEntity bukkit) {
        PathfinderMob nms = NMSEntityUtils.toNMS(bukkit);
        NMSEntityUtils.clearBrain(nms);
        NMSEntityUtils.setFollowRange(nms, 24.0);
        NMSEntityUtils.setAttackDamage(nms, 8.0);
        NMSEntityUtils.applyMeleeGoals(nms);
    }
}
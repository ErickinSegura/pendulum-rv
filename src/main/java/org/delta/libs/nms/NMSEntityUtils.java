package org.delta.libs.nms;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

public class NMSEntityUtils {


    public static PathfinderMob toNMS(LivingEntity bukkit) {
        return (PathfinderMob) ((CraftLivingEntity) bukkit).getHandle();
    }


    public static void registerAttribute(PathfinderMob nms,
                                         Holder<Attribute> holder,
                                         double baseValue) {
        AttributeMap map = nms.getAttributes();
        if (map.getInstance(holder) == null) {
            map.registerAttribute(holder);
        }
        map.getInstance(holder).setBaseValue(baseValue);
    }

    public static void setAttackDamage(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.ATTACK_DAMAGE, value);
    }

    public static void setFollowRange(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.FOLLOW_RANGE, value);
    }

    public static void setKnockbackResistance(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.KNOCKBACK_RESISTANCE, value);
    }

    public static void setAttackKnockback(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.ATTACK_KNOCKBACK, value);
    }

    public static void setMovementSpeed(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.MOVEMENT_SPEED, value);
    }

    public static void setArmor(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.ARMOR, value);
    }

    public static void setArmorToughness(PathfinderMob nms, double value) {
        registerAttribute(nms, Attributes.ARMOR_TOUGHNESS, value);
    }


    public static void clearGoals(PathfinderMob nms) {
        nms.goalSelector.getAvailableGoals().clear();
        nms.targetSelector.getAvailableGoals().clear();
    }

    public static void clearBrain(PathfinderMob nms) {
        net.minecraft.world.entity.ai.Brain<?> brain = nms.getBrain();
        try {
            java.lang.reflect.Field behaviorsField =
                    brain.getClass().getDeclaredField("availableBehaviorsByPriority");
            behaviorsField.setAccessible(true);
            ((java.util.Map<?, ?>) behaviorsField.get(brain)).clear();

            java.lang.reflect.Field memoriesField =
                    brain.getClass().getDeclaredField("memories");
            memoriesField.setAccessible(true);
            ((java.util.Map<?, ?>) memoriesField.get(brain)).clear();

            java.lang.reflect.Field sensorsField =
                    brain.getClass().getDeclaredField("sensors");
            sensorsField.setAccessible(true);
            ((java.util.Map<?, ?>) sensorsField.get(brain)).clear();

            java.lang.reflect.Field activeActivitiesField =
                    brain.getClass().getDeclaredField("activeActivities");
            activeActivitiesField.setAccessible(true);
            ((java.util.Set<?>) activeActivitiesField.get(brain)).clear();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void applyMeleeGoals(PathfinderMob nms) {
        clearGoals(nms);
        nms.goalSelector.addGoal(1, new FloatGoal(nms));
        nms.goalSelector.addGoal(2, new MeleeAttackGoal(nms, 1.3D, true));
        nms.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(nms, 0.8D));
        nms.goalSelector.addGoal(4, new LookAtPlayerGoal(nms, Player.class, 8.0F));
        nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
        nms.targetSelector.addGoal(2, new HurtByTargetGoal(nms));
    }

    public static void applyTankGoals(PathfinderMob nms) {
        clearGoals(nms);
        nms.goalSelector.addGoal(1, new FloatGoal(nms));
        nms.goalSelector.addGoal(2, new MeleeAttackGoal(nms, 1.0D, false));
        nms.goalSelector.addGoal(3, new LookAtPlayerGoal(nms, Player.class, 12.0F));
        nms.goalSelector.addGoal(4, new RandomLookAroundGoal(nms));
        nms.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(nms, Player.class, true));
        nms.targetSelector.addGoal(2, new HurtByTargetGoal(nms));
    }

    public static void applyFleeGoals(PathfinderMob nms, float detectionRadius) {
        clearGoals(nms);
        nms.goalSelector.addGoal(1, new FloatGoal(nms));
        nms.goalSelector.addGoal(2, new AvoidEntityGoal<>(nms, Player.class, detectionRadius, 1.2D, 1.5D));
        nms.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(nms, 1.0D));
        nms.goalSelector.addGoal(4, new LookAtPlayerGoal(nms, Player.class, 8.0F));
    }


    public static void forceTarget(PathfinderMob nms, LivingEntity target) {
        net.minecraft.world.entity.LivingEntity nmsTarget =
                (net.minecraft.world.entity.LivingEntity) ((CraftLivingEntity) target).getHandle();
        nms.setTarget(nmsTarget);
    }

    public static void knockback(PathfinderMob nms, double strength, double deltaX, double deltaZ) {
        nms.knockback(strength, deltaX, deltaZ);
    }

    public static void setInvulnerableTicks(PathfinderMob nms, int ticks) {
        nms.invulnerableTime = ticks;
    }


}
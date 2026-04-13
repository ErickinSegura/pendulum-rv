package org.delta.libs.builders;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.NamespacedKey;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class MobBuilder {

    private final EntityType entityType;
    private String customName;
    private boolean customNameVisible = false;
    private boolean silent = false;
    private boolean glowing = false;
    private boolean invulnerable = false;
    private boolean ai = true;
    private boolean removable = true;
    private double maxHealth = -1;
    private double health = -1;
    private int fireTicksDuration = 0;
    private ItemStack helmet, chestplate, leggings, boots, mainHand, offHand;
    private final List<PotionEffect> potionEffects = new ArrayList<>();
    private final Map<Attribute, Double> baseAttributes = new HashMap<>();
    private final List<AttributeModifierEntry> attributeModifiers = new ArrayList<>();
    private record AttributeModifierEntry(Attribute attribute, String name, double amount, Operation operation) {}

    private boolean hasBossBar = false;
    private String bossBarTitle;
    private BarColor bossBarColor = BarColor.RED;
    private BarStyle bossBarStyle = BarStyle.SOLID;
    private double bossBarRange = 50.0;
    private Plugin plugin;

    public MobBuilder(EntityType entityType) {
        if (!LivingEntity.class.isAssignableFrom(entityType.getEntityClass())) {
            throw new IllegalArgumentException("EntityType must be a LivingEntity.");
        }
        this.entityType = entityType;
    }

    public MobBuilder setCustomName(String name) {
        this.customName = ItemBuilder.format(name);
        return this;
    }

    public MobBuilder setCustomNameVisible(boolean visible) {
        this.customNameVisible = visible;
        return this;
    }

    public MobBuilder setSilent(boolean silent) {
        this.silent = silent;
        return this;
    }

    public MobBuilder setGlowing(boolean glowing) {
        this.glowing = glowing;
        return this;
    }

    public MobBuilder setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        return this;
    }

    public MobBuilder setAI(boolean ai) {
        this.ai = ai;
        return this;
    }

    public MobBuilder setRemovable(boolean removable) {
        this.removable = removable;
        return this;
    }

    public MobBuilder setMaxHealth(double maxHealth) {
        this.maxHealth = maxHealth;
        return this;
    }

    public MobBuilder setHealth(double health) {
        this.health = health;
        return this;
    }

    public MobBuilder setOnFire(int ticks) {
        this.fireTicksDuration = ticks;
        return this;
    }

    public MobBuilder addPotionEffect(PotionEffect effect) {
        this.potionEffects.add(effect);
        return this;
    }

    public MobBuilder setHelmet(ItemStack helmet) {
        this.helmet = helmet;
        return this;
    }

    public MobBuilder setChestplate(ItemStack chestplate) {
        this.chestplate = chestplate;
        return this;
    }

    public MobBuilder setLeggings(ItemStack leggings) {
        this.leggings = leggings;
        return this;
    }

    public MobBuilder setBoots(ItemStack boots) {
        this.boots = boots;
        return this;
    }

    public MobBuilder setMainHand(ItemStack item) {
        this.mainHand = item;
        return this;
    }

    public MobBuilder setOffHand(ItemStack item) {
        this.offHand = item;
        return this;
    }

    public MobBuilder setAttribute(Attribute attribute, double baseValue) {
        this.baseAttributes.put(attribute, baseValue);
        return this;
    }

    public MobBuilder addAttributeModifier(Attribute attribute, String name, double amount, Operation operation) {
        this.attributeModifiers.add(new AttributeModifierEntry(attribute, name, amount, operation));
        return this;
    }

    public MobBuilder addAttributeFlat(Attribute attribute, String name, double amount) {
        return addAttributeModifier(attribute, name, amount, Operation.ADD_NUMBER);
    }

    public MobBuilder addAttributePercent(Attribute attribute, String name, double percent) {
        return addAttributeModifier(attribute, name, percent, Operation.ADD_SCALAR);
    }

    public MobBuilder addAttributeMultiplier(Attribute attribute, String name, double multiplier) {
        return addAttributeModifier(attribute, name, multiplier, Operation.MULTIPLY_SCALAR_1);
    }

    public MobBuilder setBossBar(Plugin plugin, String title, BarColor color, BarStyle style) {
        this.hasBossBar = true;
        this.plugin = plugin;
        this.bossBarTitle = ItemBuilder.format(title);
        this.bossBarColor = color;
        this.bossBarStyle = style;
        return this;
    }

    public MobBuilder setBossBar(Plugin plugin, String title) {
        return setBossBar(plugin, title, BarColor.RED, BarStyle.SOLID);
    }

    public MobBuilder setBossBarRange(double range) {
        this.bossBarRange = range;
        return this;
    }

    public LivingEntity build(Location location) {
        LivingEntity entity = (LivingEntity) location.getWorld().spawnEntity(location, entityType);

        if (customName != null) {
            entity.setCustomName(customName);
            entity.setCustomNameVisible(customNameVisible);
        }

        entity.setSilent(silent);
        entity.setGlowing(glowing);
        entity.setInvulnerable(invulnerable);
        entity.setAI(ai);

        if (!removable && entity instanceof Mob mob) {
            mob.setRemoveWhenFarAway(false);
        }

        baseAttributes.forEach((attr, value) -> {
            var instance = entity.getAttribute(attr);
            if (instance != null) instance.setBaseValue(value);
        });

        attributeModifiers.forEach(entry -> {
            var instance = entity.getAttribute(entry.attribute());
            if (instance == null) return;
            NamespacedKey key = new NamespacedKey("mobbuilder", entry.name().toLowerCase().replace(" ", "_"));
            instance.addModifier(new AttributeModifier(key, entry.amount(), entry.operation()));
        });

        if (maxHealth > 0) {
            entity.getAttribute(Attribute.MAX_HEALTH).setBaseValue(maxHealth);
        }

        double finalHealth = health > 0
                ? Math.min(health, entity.getAttribute(Attribute.MAX_HEALTH).getValue())
                : entity.getAttribute(Attribute.MAX_HEALTH).getValue();
        entity.setHealth(finalHealth);

        if (fireTicksDuration > 0) entity.setFireTicks(fireTicksDuration);

        potionEffects.forEach(entity::addPotionEffect);

        EntityEquipment equipment = entity.getEquipment();
        if (equipment != null) {
            if (helmet != null) equipment.setHelmet(helmet);
            if (chestplate != null) equipment.setChestplate(chestplate);
            if (leggings != null) equipment.setLeggings(leggings);
            if (boots != null) equipment.setBoots(boots);
            if (mainHand != null) equipment.setItemInMainHand(mainHand);
            if (offHand != null) equipment.setItemInOffHand(offHand);
        }

        if (hasBossBar) {
            BossBar bossBar = Bukkit.createBossBar(bossBarTitle, bossBarColor, bossBarStyle);
            bossBar.setProgress(1.0);

            double range = bossBarRange;

            int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
                Bukkit.getOnlinePlayers().forEach(player -> {
                    if (player.getWorld().equals(entity.getWorld())
                            && player.getLocation().distance(entity.getLocation()) <= range) {
                        bossBar.addPlayer(player);
                    } else {
                        bossBar.removePlayer(player);
                    }
                });
            }, 0L, 10L).getTaskId();

            Bukkit.getPluginManager().registerEvents(new Listener() {

                @EventHandler
                public void onDamage(EntityDamageEvent event) {
                    if (!event.getEntity().getUniqueId().equals(entity.getUniqueId())) return;

                    double currentHealth = Math.max(0, entity.getHealth() - event.getFinalDamage());
                    double maxHp = entity.getAttribute(Attribute.MAX_HEALTH).getValue();
                    bossBar.setProgress(Math.max(0, currentHealth / maxHp));
                }

                @EventHandler
                public void onDeath(EntityDeathEvent event) {
                    if (!event.getEntity().getUniqueId().equals(entity.getUniqueId())) return;

                    bossBar.setProgress(0);
                    bossBar.removeAll();
                    Bukkit.getScheduler().cancelTask(taskId);
                    HandlerList.unregisterAll(this);
                }

            }, plugin);
        }

        return entity;
    }
}
package org.delta.libs.builders;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Map;

public class ItemBuilder {
    protected ItemStack is;
    protected ItemMeta im;

    public ItemBuilder() {}

    public ItemBuilder(ItemStack itemStack) {
        this.is = new ItemStack(itemStack);
    }

    public ItemBuilder(Material material) {
        this.is = new ItemStack(material);
    }

    public ItemBuilder(Material material, int amount) {
        this.is = new ItemStack(material, amount);
    }

    public ItemBuilder setDurability(int durability) {
        this.is.setDurability((short) durability);
        return this;
    }

    public ItemBuilder setUnbrekeable(boolean b) {
        this.im = this.is.getItemMeta();
        this.im.setUnbreakable(b);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setCustomModelData(int model) {
        this.im = this.is.getItemMeta();
        this.im.setCustomModelData(model);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setCustomModelData(int model, boolean b) {
        if (!b) return this;
        this.im = this.is.getItemMeta();
        this.im.setCustomModelData(model);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setDisplayName(String name) {
        this.im = this.is.getItemMeta();
        this.im.setDisplayName(name);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addEnchant(Enchantment enchantment, int level) {
        this.im = this.is.getItemMeta();
        this.im.addEnchant(enchantment, level, true);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addEnchants(Map<Enchantment, Integer> enchantments) {
        this.im = this.is.getItemMeta();
        if (!enchantments.isEmpty())
            for (Enchantment ench : enchantments.keySet())
                this.im.addEnchant(ench, enchantments.get(ench).intValue(), true);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addItemFlag(ItemFlag itemflag) {
        this.im = this.is.getItemMeta();
        this.im.addItemFlags(itemflag);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder setLore(List<String> lore) {
        this.im = this.is.getItemMeta();
        this.im.setLore(lore);
        this.is.setItemMeta(this.im);
        return this;
    }

    private EquipmentSlotGroup toSlotGroup(EquipmentSlot slot) {
        if (slot == null) return EquipmentSlotGroup.ANY;
        return switch (slot) {
            case HEAD     -> EquipmentSlotGroup.HEAD;
            case CHEST    -> EquipmentSlotGroup.CHEST;
            case LEGS     -> EquipmentSlotGroup.LEGS;
            case FEET     -> EquipmentSlotGroup.FEET;
            case HAND     -> EquipmentSlotGroup.MAINHAND;
            case OFF_HAND -> EquipmentSlotGroup.OFFHAND;
            default       -> EquipmentSlotGroup.ANY;
        };
    }


    public ItemBuilder addAttributeModifier(Attribute attribute,
                                            String name,
                                            double amount,
                                            Operation operation,
                                            EquipmentSlot slot) {
        this.im = this.is.getItemMeta();
        NamespacedKey key = new NamespacedKey("itembuilder", name.toLowerCase().replace(" ", "_"));
        AttributeModifier modifier = new AttributeModifier(key, amount, operation, toSlotGroup(slot));
        this.im.addAttributeModifier(attribute, modifier);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder addAttributeFlat(Attribute attribute, String name, double amount, EquipmentSlot slot) {
        return addAttributeModifier(attribute, name, amount, Operation.ADD_NUMBER, slot);
    }

    public ItemBuilder addAttributeFlat(Attribute attribute, String name, double amount) {
        return addAttributeModifier(attribute, name, amount, Operation.ADD_NUMBER, null);
    }

    public ItemBuilder addAttributePercent(Attribute attribute, String name, double percent, EquipmentSlot slot) {
        return addAttributeModifier(attribute, name, percent, Operation.ADD_SCALAR, slot);
    }

    public ItemBuilder addAttributePercent(Attribute attribute, String name, double percent) {
        return addAttributeModifier(attribute, name, percent, Operation.ADD_SCALAR, null);
    }

    public ItemBuilder addAttributeMultiplier(Attribute attribute, String name, double multiplier, EquipmentSlot slot) {
        return addAttributeModifier(attribute, name, multiplier, Operation.MULTIPLY_SCALAR_1, slot);
    }

    public ItemBuilder removeAttributeModifiers(Attribute attribute) {
        this.im = this.is.getItemMeta();
        this.im.removeAttributeModifier(attribute);
        this.is.setItemMeta(this.im);
        return this;
    }

    public ItemBuilder hideAttributes() {
        this.im = this.is.getItemMeta();
        this.im.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        this.is.setItemMeta(this.im);
        return this;
    }


    public static String format(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public ItemStack build() {
        return this.is;
    }
}
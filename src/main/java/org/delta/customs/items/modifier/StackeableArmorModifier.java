package org.delta.customs.items.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.attribute.AttributeModifier.Operation;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class StackeableArmorModifier implements Modifier {

    protected abstract NamespacedKey key();

    protected abstract Attribute attribute();

    protected abstract Operation operation();

    protected abstract double bonusPerStack();

    protected abstract String loreLine();

    protected int maxStacks() {
        return Integer.MAX_VALUE;
    }

    public int getStacks(ItemStack target) {
        if (target == null) return 0;
        ItemMeta meta = target.getItemMeta();
        if (meta == null) return 0;
        Integer count = meta.getPersistentDataContainer().get(key(), PersistentDataType.INTEGER);
        return count == null ? 0 : count;
    }

    @Override
    public boolean canApply(ItemStack target) {
        if (!Modifier.isArmor(target)) return false;
        if (Modifier.hasIncompatible(this, target)) return false;
        return getStacks(target) < maxStacks();
    }

    @Override
    public boolean isApplied(ItemStack target) {
        return getStacks(target) > 0;
    }

    @Override
    public void apply(ItemStack target) {
        ItemMeta meta = target.getItemMeta();
        if (meta == null) return;

        int newStacks = getStacks(target) + 1;
        NamespacedKey attributeKey = attributeKey(target);

        Collection<AttributeModifier> existing = meta.getAttributeModifiers(attribute());
        if (existing != null) {
            for (AttributeModifier current : new ArrayList<>(existing)) {
                if (current.getKey().equals(attributeKey)) {
                    meta.removeAttributeModifier(attribute(), current);
                }
            }
        }

        AttributeModifier modifier = new AttributeModifier(
                attributeKey,
                bonusPerStack() * newStacks,
                operation(),
                EquipmentSlotGroup.ARMOR
        );
        meta.addAttributeModifier(attribute(), modifier);
        meta.getPersistentDataContainer().set(key(), PersistentDataType.INTEGER, newStacks);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.removeIf(line -> line.equals(loreLine()));
        lore.add(loreLine());
        meta.setLore(lore);

        target.setItemMeta(meta);
    }

    private NamespacedKey attributeKey(ItemStack target) {
        return new NamespacedKey("delta", key().getKey() + "_" + armorSuffix(target));
    }

    private String armorSuffix(ItemStack target) {
        String name = target.getType().name();
        if (name.endsWith("_HELMET")) return "helmet";
        if (name.endsWith("_CHESTPLATE")) return "chestplate";
        if (name.endsWith("_LEGGINGS")) return "leggings";
        if (name.endsWith("_BOOTS")) return "boots";
        return "armor";
    }
}

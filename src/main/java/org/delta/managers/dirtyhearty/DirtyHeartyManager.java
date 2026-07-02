package org.delta.managers.dirtyhearty;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;

public final class DirtyHeartyManager {

    public static final double HEART_VALUE = 2.0;
    public static final int MAX_HEARTS = 4;

    private static final NamespacedKey COUNT_KEY = new NamespacedKey("delta", "dirty_hearty_count");
    private static final NamespacedKey MODIFIER_KEY = new NamespacedKey("delta", "dirty_hearty_bonus");

    private DirtyHeartyManager() {}

    public static int getCount(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.getOrDefault(COUNT_KEY, PersistentDataType.INTEGER, 0);
    }

    private static void setCount(Player player, int count) {
        int clamped = Math.max(0, Math.min(MAX_HEARTS, count));
        player.getPersistentDataContainer().set(COUNT_KEY, PersistentDataType.INTEGER, clamped);
    }

    public static boolean addHeart(Player player) {
        int count = getCount(player);
        if (count >= MAX_HEARTS) return false;
        setCount(player, count + 1);
        applyModifier(player);
        return true;
    }

    public static void reset(Player player) {
        setCount(player, 0);
        applyModifier(player);

        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr != null && player.getHealth() > attr.getValue()) {
            player.setHealth(attr.getValue());
        }
    }

    public static void applyModifier(Player player) {
        AttributeInstance attr = player.getAttribute(Attribute.MAX_HEALTH);
        if (attr == null) return;

        for (AttributeModifier modifier : new ArrayList<>(attr.getModifiers())) {
            if (modifier.getKey().equals(MODIFIER_KEY)) {
                attr.removeModifier(modifier);
            }
        }

        int count = getCount(player);
        if (count > 0) {
            attr.addModifier(new AttributeModifier(
                    MODIFIER_KEY,
                    count * HEART_VALUE,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.ANY
            ));
        }
    }
}

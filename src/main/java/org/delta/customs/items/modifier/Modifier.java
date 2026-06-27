package org.delta.customs.items.modifier;

import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.Set;

public interface Modifier {

    String getKey();

    String getDisplayName();

    boolean canApply(ItemStack target);

    boolean isApplied(ItemStack target);

    void apply(ItemStack target);

    default Set<String> incompatibleWith() {
        return Collections.emptySet();
    }

    static boolean hasIncompatible(Modifier modifier, ItemStack target) {
        for (String key : modifier.incompatibleWith()) {
            if (ModifierRegistry.get(key).map(other -> other.isApplied(target)).orElse(false)) {
                return true;
            }
        }
        return false;
    }

    static boolean isArmor(ItemStack item) {
        if (item == null) return false;
        String name = item.getType().name();
        return name.endsWith("_HELMET")
                || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS")
                || name.endsWith("_BOOTS");
    }
}

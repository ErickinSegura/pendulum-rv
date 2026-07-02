package org.delta.customs.items.modifier;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.libs.builders.ItemBuilder;

import java.util.ArrayList;
import java.util.List;

public class ModifierIrrompible implements Modifier {

    private static final NamespacedKey APPLIED_KEY = new NamespacedKey("delta", "modifier_irrompible");
    private static final String LORE = ItemBuilder.format("&6\uD83D\uDEE1 Irrompible");

    @Override
    public String getKey() {
        return "unbreakable_modifier";
    }

    @Override
    public String getDisplayName() {
        return "Irrompible";
    }

    @Override
    public boolean canApply(ItemStack target) {
        if (target == null) return false;
        if (target.getType().getMaxDurability() <= 0) return false;
        return !isApplied(target);
    }

    @Override
    public boolean isApplied(ItemStack target) {
        if (target == null) return false;
        ItemMeta meta = target.getItemMeta();
        if (meta == null) return false;
        if (meta.isUnbreakable()) return true;
        return meta.getPersistentDataContainer().has(APPLIED_KEY, PersistentDataType.BYTE);
    }

    @Override
    public void apply(ItemStack target) {
        ItemMeta meta = target.getItemMeta();
        if (meta == null) return;

        meta.setUnbreakable(true);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.getPersistentDataContainer().set(APPLIED_KEY, PersistentDataType.BYTE, (byte) 1);

        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add(LORE);
        meta.setLore(lore);

        target.setItemMeta(meta);
    }
}

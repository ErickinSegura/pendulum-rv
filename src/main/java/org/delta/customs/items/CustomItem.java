package org.delta.customs.items;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public interface CustomItem {
    NamespacedKey ITEM_KEY = new NamespacedKey("delta", "custom_item_key");

    String getKey();
    ItemStack buildItem();

    default ItemStack build() {
        ItemStack item = buildItem();
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(
                    ITEM_KEY,
                    PersistentDataType.STRING,
                    getKey()
            );
            item.setItemMeta(meta);
        }
        return item;
    }
}
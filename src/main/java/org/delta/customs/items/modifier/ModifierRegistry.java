package org.delta.customs.items.modifier;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ModifierRegistry {

    private static final Map<String, Modifier> MODIFIERS = new HashMap<>();

    static {
        register(new ModifierIrrompible());
        register(new ModifierLiviano());
        register(new ModifierVampirico());
    }

    private static void register(Modifier modifier) {
        MODIFIERS.put(modifier.getKey().toLowerCase(), modifier);
    }

    public static Collection<Modifier> values() {
        return Collections.unmodifiableCollection(MODIFIERS.values());
    }

    public static Optional<Modifier> get(String key) {
        if (key == null) return Optional.empty();
        return Optional.ofNullable(MODIFIERS.get(key.toLowerCase()));
    }

    public static Optional<Modifier> fromItem(ItemStack item) {
        if (item == null) return Optional.empty();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return Optional.empty();
        String key = meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING);
        return get(key);
    }
}

package org.delta.customs.items;

import org.delta.customs.items.tools.Clawn;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class ItemRegistry {

    private static final Map<String, CustomItem> ITEMS = new HashMap<>();

    static {
        register(new Clawn());
    }

    private static void register(CustomItem item) {
        ITEMS.put(item.getKey(), item);
    }

    public static Optional<CustomItem> get(String key) {
        return Optional.ofNullable(ITEMS.get(key.toLowerCase()));
    }

    public static Set<String> getKeys() {
        return ITEMS.keySet();
    }
}
package org.delta.commands.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.ItemRegistry;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GiveMenuHolder implements InventoryHolder {

    private record Category(String token, String display, Material pane) {}

    private static final List<Category> CATEGORIES = List.of(
            new Category(".base.nucleos.", "&6&lNúcleos", Material.ORANGE_STAINED_GLASS_PANE),
            new Category(".base.fragmentos.", "&a&lFragmentos", Material.LIME_STAINED_GLASS_PANE),
            new Category(".base.uniones.", "&b&lUniones", Material.CYAN_STAINED_GLASS_PANE),
            new Category(".modifier.", "&d&lModificadores", Material.MAGENTA_STAINED_GLASS_PANE),
            new Category(".tools.", "&9&lHerramientas", Material.LIGHT_BLUE_STAINED_GLASS_PANE),
            new Category(".consumables.", "&e&lConsumibles", Material.YELLOW_STAINED_GLASS_PANE)
    );

    private static final Category OTROS =
            new Category("", "&7&lOtros", Material.GRAY_STAINED_GLASS_PANE);

    private final Inventory inventory;

    public GiveMenuHolder() {
        Map<Category, List<CustomItem>> grouped = group();

        int slots = 0;
        for (List<CustomItem> items : grouped.values()) {
            slots += 1 + items.size();
        }
        int size = Math.min(54, Math.max(9, (int) Math.ceil(slots / 9.0) * 9));

        this.inventory = Bukkit.createInventory(this, size,
                MessageUtils.color("&6&lItems del Plugin"));

        int slot = 0;
        for (Map.Entry<Category, List<CustomItem>> entry : grouped.entrySet()) {
            if (slot >= size) break;
            inventory.setItem(slot++, header(entry.getKey()));
            for (CustomItem item : entry.getValue()) {
                if (slot >= size) break;
                inventory.setItem(slot++, item.build());
            }
        }
    }

    private Map<Category, List<CustomItem>> group() {
        Map<Category, List<CustomItem>> grouped = new LinkedHashMap<>();
        for (Category category : CATEGORIES) grouped.put(category, new ArrayList<>());
        grouped.put(OTROS, new ArrayList<>());

        List<String> keys = new ArrayList<>(ItemRegistry.getKeys());
        keys.sort(String::compareToIgnoreCase);

        for (String key : keys) {
            CustomItem item = ItemRegistry.get(key).orElse(null);
            if (item == null) continue;
            grouped.get(categoryOf(item)).add(item);
        }

        grouped.values().removeIf(List::isEmpty);
        return grouped;
    }

    private Category categoryOf(CustomItem item) {
        String name = item.getClass().getName();
        for (Category category : CATEGORIES) {
            if (name.contains(category.token())) return category;
        }
        return OTROS;
    }

    private ItemStack header(Category category) {
        return new ItemBuilder(category.pane())
                .setDisplayName(ItemBuilder.format(category.display()))
                .build();
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}

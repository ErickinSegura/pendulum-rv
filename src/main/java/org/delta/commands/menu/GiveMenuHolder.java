package org.delta.commands.menu;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.ItemRegistry;
import org.delta.libs.MessageUtils;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Interfaz tipo cofre que muestra todos los items del plugin.
 * Al hacer click en uno, el {@code GiveMenuListener} entrega un stack al jugador.
 */
public class GiveMenuHolder implements InventoryHolder {

    private final Inventory inventory;

    public GiveMenuHolder() {
        List<String> keys = new ArrayList<>(ItemRegistry.getKeys());
        keys.sort(String::compareToIgnoreCase);

        int rows = (int) Math.ceil(keys.size() / 9.0);
        int size = Math.min(54, Math.max(9, rows * 9));

        this.inventory = Bukkit.createInventory(this, size,
                MessageUtils.color("&6&lItems del Plugin"));

        for (String key : keys) {
            CustomItem item = ItemRegistry.get(key).orElse(null);
            if (item == null) continue;
            inventory.addItem(item.build());
        }
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}

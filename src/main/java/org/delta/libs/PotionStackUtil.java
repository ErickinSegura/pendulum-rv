package org.delta.libs;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 * Centraliza el apilado de pociones. En 1.21 el tamaño de stack es un componente
 * de datos, por lo que dos pociones solo se apilan entre sí si ambas tienen el
 * mismo {@code max_stack_size}. Para que el apilado sea consistente hay que aplicar
 * este componente a TODA poción que pase por el servidor (ver {@code PotionListener}).
 */
public final class PotionStackUtil {

    public static final int MAX_STACK_SIZE = 16;

    private PotionStackUtil() {}

    public static boolean isPotion(Material material) {
        return material == Material.POTION
                || material == Material.SPLASH_POTION
                || material == Material.LINGERING_POTION;
    }

    /**
     * Aplica el tamaño de stack a una poción individual.
     *
     * @return {@code true} si el ítem cambió (era poción y aún no tenía el componente).
     */
    public static boolean normalize(ItemStack item) {
        if (item == null || !isPotion(item.getType())) return false;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        if (meta.hasMaxStackSize() && meta.getMaxStackSize() == MAX_STACK_SIZE) return false;

        meta.setMaxStackSize(MAX_STACK_SIZE);
        item.setItemMeta(meta);
        return true;
    }

    /**
     * Normaliza todas las pociones de un inventario. Reescribe cada slot que cambie
     * para garantizar que la mutación persista (algunos inventarios devuelven copias).
     */
    public static void normalize(Inventory inventory) {
        if (inventory == null) return;

        ItemStack[] contents = inventory.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (normalize(item)) {
                inventory.setItem(i, item);
            }
        }
    }
}

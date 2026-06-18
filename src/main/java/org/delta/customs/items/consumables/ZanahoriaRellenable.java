package org.delta.customs.items.consumables;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

/**
 * Zanahoria dorada que nunca se gasta al comerla: en su lugar consume una de las
 * "cargas" guardadas en su PersistentDataContainer. Se rellena con zanahorias
 * normales (hasta {@link #MAX_CHARGES}). Sin cargas, no se puede comer.
 */
public class ZanahoriaRellenable implements CustomItem {

    public static final int MAX_CHARGES = 256;
    public static final NamespacedKey CHARGES_KEY = new NamespacedKey("delta", "carrot_charges");

    @Override
    public String getKey() {
        return "zanahoria_rellenable";
    }

    @Override
    public ItemStack buildItem() {
        ItemStack item = new ItemBuilder(Material.GOLDEN_CARROT)
                .setDisplayName(ItemBuilder.format("&6Zanahoria Rellenable"))
                .build();
        setCharges(item, MAX_CHARGES);
        return item;
    }

    public static int getCharges(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer charges = meta.getPersistentDataContainer().get(CHARGES_KEY, PersistentDataType.INTEGER);
        return charges == null ? 0 : charges;
    }

    public static void setCharges(ItemStack item, int charges) {
        if (item == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        int clamped = Math.max(0, Math.min(MAX_CHARGES, charges));
        meta.getPersistentDataContainer().set(CHARGES_KEY, PersistentDataType.INTEGER, clamped);
        meta.setLore(buildLore(clamped));
        item.setItemMeta(meta);
    }

    private static List<String> buildLore(int charges) {
        return List.of(
                ItemBuilder.format("&7Una zanahoria dorada que nunca se gasta."),
                ItemBuilder.format("&7Cómela mientras le queden cargas."),
                ItemBuilder.format(""),
                ItemBuilder.format("&6Cargas: &e" + charges + "&7/&e" + MAX_CHARGES),
                ItemBuilder.format("&8Combínala con zanahorias en la mesa"),
                ItemBuilder.format("&8de crafteo para rellenarla.")
        );
    }
}

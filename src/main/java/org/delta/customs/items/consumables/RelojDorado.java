package org.delta.customs.items.consumables;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class RelojDorado implements CustomItem {

    @Override
    public String getKey() {
        return "reloj_dorado";
    }

    @Override
    public ItemStack buildItem() {
        ItemStack item = new ItemBuilder(Material.CLOCK)
                .setDisplayName(ItemBuilder.format("&6&lReloj Dorado del Péndulo"))
                .setLore(List.of(
                        ItemBuilder.format("&7Click derecho para otorgar &e1 hora"),
                        ItemBuilder.format("&7extra de día en el servidor."),
                        ItemBuilder.format("&c&oUn solo uso."),
                        ItemBuilder.format("&8Easter Egg · Tempus")
                ))
                .setMaxStackSize(1)
                .build();

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setEnchantmentGlintOverride(true);
            item.setItemMeta(meta);
        }
        return item;
    }
}

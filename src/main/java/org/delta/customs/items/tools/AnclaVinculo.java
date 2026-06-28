package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class AnclaVinculo implements CustomItem {
    @Override
    public String getKey() {
        return "ancla_vinculo";
    }

    @Override
    public ItemStack buildItem() {
        return new ItemBuilder(Material.RECOVERY_COMPASS)
                .setDisplayName(ItemBuilder.format("&5&lAncla de Vínculo"))
                .setLore(List.of(
                        ItemBuilder.format("&7Click derecho: viaja junto a"),
                        ItemBuilder.format("&7un aliado de tu equipo."),
                        ItemBuilder.format("&8Enfriamiento prolongado")
                ))
                .setCustomModelData(1)
                .setMaxStackSize(1)
                .build();
    }
}

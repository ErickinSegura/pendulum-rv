package org.delta.customs.items.tools;

import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;

import java.util.List;

public class Clawn implements CustomItem {
    @Override
    public String getKey() {
        return "clawn";
    }

    @Override
    public ItemStack build() {
        return new ItemBuilder(Material.RESIN_CLUMP)
                .setDisplayName(ItemBuilder.format("&6Clawn"))
                .setLore(List.of(
                        ItemBuilder.format("Consigue más rango de acción con esta garra de resina.")
                ))
                .addAttributeFlat(Attribute.BLOCK_INTERACTION_RANGE, "clawn_block_int_range", 5.0)
                .addAttributeFlat(Attribute.ENTITY_INTERACTION_RANGE, "clawn_entity_int_range", 5.0)
                .addAttributeFlat(Attribute.ATTACK_DAMAGE, "clawn_dmg", -5.0)
                .setCustomModelData(1)
                .build();
    }
}

package org.delta.customs.items.chargebase;
import org.bukkit.Material;
import org.delta.customs.items.CustomItem;
import org.delta.libs.builders.ItemBuilder;
import org.bukkit.inventory.ItemStack;
import java.util.List;

public class NucleoImpulso implements CustomItem {
    @Override public String getKey() { return "nucleo_impulso"; }
    @Override public ItemStack build() {
        return new ItemBuilder(Material.BLAZE_ROD)
                .setDisplayName("&c&lNúcleo de Impulso")
                .setLore(List.of(ItemBuilder.format("&7Drop de mob &cAtacante&7."), ItemBuilder.format("&8Clase: &cAtacante")))
                .build();
    }
}
package org.delta.commands.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.libs.MessageUtils;
import org.delta.libs.builders.ItemBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TeamTeleportHolder implements InventoryHolder {

    public static final NamespacedKey TARGET_KEY = new NamespacedKey("delta", "teleport_target");

    private final Inventory inventory;

    public TeamTeleportHolder(List<Player> mates) {
        int size = Math.min(54, Math.max(9, (int) Math.ceil(mates.size() / 9.0) * 9));
        this.inventory = Bukkit.createInventory(this, size,
                MessageUtils.color("&5&lViajar a un aliado"));

        for (Player mate : mates) {
            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta meta = (SkullMeta) head.getItemMeta();
            if (meta != null) {
                meta.setOwningPlayer(mate);
                meta.setDisplayName(ItemBuilder.format("&a" + mate.getName()));
                meta.getPersistentDataContainer().set(TARGET_KEY, PersistentDataType.STRING, mate.getName());
                head.setItemMeta(meta);
            }
            inventory.addItem(head);
        }
    }

    @Override
    @NotNull
    public Inventory getInventory() {
        return inventory;
    }
}

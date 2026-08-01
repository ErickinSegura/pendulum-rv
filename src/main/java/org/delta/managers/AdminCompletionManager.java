package org.delta.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AdminCompletionManager {

    private final NamespacedKey key;

    public AdminCompletionManager(Plugin plugin) {
        this.key = new NamespacedKey(plugin, "admin_completions_visibles");
    }

    public boolean isVisible(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        return data.getOrDefault(key, PersistentDataType.BOOLEAN, false);
    }

    public void setVisible(Player player, boolean visible) {
        player.getPersistentDataContainer().set(key, PersistentDataType.BOOLEAN, visible);
    }

    public boolean toggle(Player player) {
        boolean nuevo = !isVisible(player);
        setVisible(player, nuevo);
        return nuevo;
    }
}

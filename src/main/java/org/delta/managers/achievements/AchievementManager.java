package org.delta.managers.achievements;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

public class AchievementManager {

    private final Plugin plugin;
    private final AchievementDatapack datapack;

    public AchievementManager(Plugin plugin) {
        this.plugin = plugin;
        this.datapack = new AchievementDatapack(plugin);
        this.datapack.install();
    }

    public void unlock(Player player, Achievement achievement) {
        datapack.award(player, achievement);
    }

    public int addProgress(Player player, String counterId, int amount) {
        NamespacedKey key = new NamespacedKey(plugin, "progress_" + counterId);
        PersistentDataContainer data = player.getPersistentDataContainer();
        int updated = data.getOrDefault(key, PersistentDataType.INTEGER, 0) + amount;
        data.set(key, PersistentDataType.INTEGER, updated);
        return updated;
    }

    public int addToSet(Player player, String setId, String value) {
        NamespacedKey key = new NamespacedKey(plugin, "set_" + setId);
        PersistentDataContainer data = player.getPersistentDataContainer();
        String raw = data.getOrDefault(key, PersistentDataType.STRING, "");
        java.util.LinkedHashSet<String> set = new java.util.LinkedHashSet<>();
        if (!raw.isEmpty()) {
            for (String v : raw.split(",")) {
                if (!v.isBlank()) set.add(v);
            }
        }
        set.add(value);
        data.set(key, PersistentDataType.STRING, String.join(",", set));
        return set.size();
    }
}

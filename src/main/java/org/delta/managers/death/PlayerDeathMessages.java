package org.delta.managers.death;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delta.pendulum;

import java.io.File;
import java.io.IOException;


public class PlayerDeathMessages {
    private final pendulum plugin;
    private File configFile;
    private FileConfiguration config;

    public PlayerDeathMessages(pendulum plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "players.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                config = YamlConfiguration.loadConfiguration(configFile);

                config.set("players.iPancrema", "Placeholder");

                saveConfig();
                plugin.getLogger().info("Archivo players.yml creado con éxito");
            } catch (IOException e) {
                plugin.getLogger().severe("Error al crear players.yml: " + e.getMessage());
            }
        } else {
            config = YamlConfiguration.loadConfiguration(configFile);
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar players.yml: " + e.getMessage());
        }
    }


    public String getCustomDeathMessage(String playerName) {
        if (config.contains("players." + playerName)) {
            return config.getString("players." + playerName);
        }
        return null;
    }

    public boolean hasCustomMessage(String playerName) {
        return config.contains("players." + playerName);
    }
}
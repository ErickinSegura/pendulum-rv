package org.delta.managers.teamChest;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TeamChestManager {

    private static final char SEP = '\u0000';

    private static TeamChestManager instance;
    private final File dataFile;
    private final File configFile;
    private FileConfiguration dataConfig;
    private FileConfiguration config;
    private final Map<String, Inventory> teamChests;
    private final Map<String, Integer> teamChestSizes;
    private final Set<String> openChests;
    private final Map<String, String> playerInChest;

    private TeamChestManager(File dataFolder) {
        this.dataFile = new File(dataFolder, "teamchests.yml");
        this.configFile = new File(dataFolder, "teamchests_config.yml");
        this.teamChests = new HashMap<>();
        this.teamChestSizes = new HashMap<>();
        this.openChests = new HashSet<>();
        this.playerInChest = new HashMap<>();
        loadConfig();
        loadData();
    }

    public static void initialize(File dataFolder) {
        if (instance == null) {
            instance = new TeamChestManager(dataFolder);
        }
    }

    public static TeamChestManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("TeamChestManager no ha sido inicializado");
        }
        return instance;
    }

    private static YamlConfiguration newYaml() {
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.options().pathSeparator(SEP);
        return cfg;
    }

    private static YamlConfiguration loadYaml(File file) {
        YamlConfiguration cfg = newYaml();
        if (file.exists()) {
            try {
                cfg.load(file);
            } catch (Exception e) {
                backupCorrupt(file);
            }
        }
        return cfg;
    }

    private static void backupCorrupt(File file) {
        File backup = new File(file.getParentFile(), file.getName() + ".corrupt-" + System.currentTimeMillis());
        try {
            java.nio.file.Files.copy(file.toPath(), backup.toPath());
            Bukkit.getLogger().severe("[TeamChest] No se pudo parsear " + file.getName()
                    + ", respaldo creado: " + backup.getName());
        } catch (IOException ex) {
            Bukkit.getLogger().severe("[TeamChest] No se pudo parsear ni respaldar " + file.getName());
        }
    }

    private static String path(String... parts) {
        return String.join(String.valueOf(SEP), parts);
    }

    private void loadConfig() {
        config = loadYaml(configFile);
        if (!config.contains("default_rows")) {
            try {
                configFile.getParentFile().mkdirs();
                config.set("default_rows", 3);
                config.save(configFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (config.getConfigurationSection("teams") != null) {
            for (String team : config.getConfigurationSection("teams").getKeys(false)) {
                int rows = config.getInt(path("teams", team, "rows"), 3);
                teamChestSizes.put(team.toLowerCase(), rows);
            }
        }
    }

    private void saveConfig() {
        try {
            for (Map.Entry<String, Integer> entry : teamChestSizes.entrySet()) {
                config.set(path("teams", entry.getKey(), "rows"), entry.getValue());
            }
            config.save(configFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean setTeamChestSize(String teamName, int rows) {
        if (rows < 1 || rows > 6) {
            return false;
        }

        String normalizedTeam = teamName.toLowerCase();
        int oldRows = teamChestSizes.getOrDefault(normalizedTeam, 3);

        if (oldRows == rows) {
            return true;
        }

        teamChestSizes.put(normalizedTeam, rows);

        if (teamChests.containsKey(normalizedTeam)) {
            migrateChestSize(normalizedTeam, oldRows, rows);
        }

        saveConfig();
        return true;
    }

    private void migrateChestSize(String teamName, int oldRows, int newRows) {
        Inventory oldInv = teamChests.get(teamName);
        int newSize = newRows * 9;
        Inventory newInv = Bukkit.createInventory(null, newSize,
                "§6Cofre del Equipo §e" + capitalize(teamName));

        int maxSlots = Math.min(oldInv.getSize(), newInv.getSize());
        for (int i = 0; i < maxSlots; i++) {
            ItemStack item = oldInv.getItem(i);
            if (item != null) {
                newInv.setItem(i, item.clone());
            }
        }

        teamChests.put(teamName, newInv);
        saveData();
    }

    public int getTeamChestRows(String teamName) {
        return teamChestSizes.getOrDefault(teamName.toLowerCase(),
                config.getInt("default_rows", 3));
    }

    public boolean isChestOpen(String teamName) {
        return openChests.contains(teamName.toLowerCase());
    }

    public String getPlayerInChest(String teamName) {
        for (Map.Entry<String, String> entry : playerInChest.entrySet()) {
            if (entry.getValue().equalsIgnoreCase(teamName)) {
                return entry.getKey();
            }
        }
        return null;
    }

    public boolean openChest(Player player, String teamName) {
        String normalizedTeam = teamName.toLowerCase();

        if (isChestOpen(normalizedTeam)) {
            return false;
        }

        openChests.add(normalizedTeam);
        playerInChest.put(player.getName(), normalizedTeam);
        return true;
    }

    public void closeChest(Player player) {
        String teamName = playerInChest.remove(player.getName());
        if (teamName != null) {
            openChests.remove(teamName);
        }
    }

    public Inventory getTeamChest(String teamName) {
        String normalizedTeam = teamName.toLowerCase();

        if (!teamChests.containsKey(normalizedTeam)) {
            int rows = getTeamChestRows(normalizedTeam);
            int size = rows * 9;
            Inventory inv = Bukkit.createInventory(null, size,
                    "§6Cofre del Equipo §e" + capitalize(teamName));
            teamChests.put(normalizedTeam, inv);
        }

        return teamChests.get(normalizedTeam);
    }

    private String serializeInventory(Inventory inventory) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            dataOutput.writeInt(inventory.getSize());

            for (int i = 0; i < inventory.getSize(); i++) {
                ItemStack item = inventory.getItem(i);
                dataOutput.writeObject(item);
            }

            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private Inventory deserializeInventory(String data, String teamName, int size) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);

            int savedSize = dataInput.readInt();
            Inventory inventory = Bukkit.createInventory(null, size,
                    "§6Cofre del Equipo §e" + capitalize(teamName));

            int maxSlots = Math.min(savedSize, size);
            for (int i = 0; i < maxSlots; i++) {
                ItemStack item = (ItemStack) dataInput.readObject();
                if (item != null) {
                    inventory.setItem(i, item);
                }
            }

            dataInput.close();
            return inventory;
        } catch (Exception e) {
            e.printStackTrace();
            return Bukkit.createInventory(null, size,
                    "§6Cofre del Equipo §e" + capitalize(teamName));
        }
    }

    public void saveData() {
        try {
            dataFile.getParentFile().mkdirs();

            dataConfig = newYaml();

            for (Map.Entry<String, Inventory> entry : teamChests.entrySet()) {
                String teamName = entry.getKey();
                Inventory inv = entry.getValue();

                String serialized = serializeInventory(inv);
                dataConfig.set(path("teams", teamName, "inventory"), serialized);
                dataConfig.set(path("teams", teamName, "size"), inv.getSize());
            }

            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadData() {
        if (!dataFile.exists()) {
            return;
        }

        dataConfig = loadYaml(dataFile);

        if (dataConfig.getConfigurationSection("teams") == null) {
            return;
        }

        for (String teamName : dataConfig.getConfigurationSection("teams").getKeys(false)) {
            String inventoryData = dataConfig.getString(path("teams", teamName, "inventory"));
            int rows = getTeamChestRows(teamName);
            int size = rows * 9;

            if (inventoryData != null && !inventoryData.isEmpty()) {
                Inventory inv = deserializeInventory(inventoryData, teamName, size);
                teamChests.put(teamName.toLowerCase(), inv);
            }
        }
    }

    public boolean isTeamChest(Inventory inventory) {
        return teamChests.containsValue(inventory);
    }

    public String getTeamFromInventory(Inventory inventory) {
        for (Map.Entry<String, Inventory> entry : teamChests.entrySet()) {
            if (entry.getValue().equals(inventory)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
package org.delta.managers.bingo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delta.pendulum;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BingoChallengesManager {
    private static BingoChallengesManager instance;
    private final pendulum plugin;
    private File challengesFile;
    private FileConfiguration challengesConfig;

    private final Map<Integer, BingoChallenge> masterChallengeList;

    private BingoChallengesManager(pendulum plugin) {
        this.plugin = plugin;
        this.masterChallengeList = new HashMap<>();
        loadChallengesFile();
        loadMasterChallengeList();
    }

    public static BingoChallengesManager getInstance(pendulum plugin) {
        if (instance == null) {
            instance = new BingoChallengesManager(plugin);
        }
        return instance;
    }

    public static BingoChallengesManager getInstance() {
        return instance;
    }

    private void loadChallengesFile() {
        challengesFile = new File(plugin.getDataFolder(), "bingo-challenges.yml");

        if (!challengesFile.exists()) {
            try {
                plugin.saveResource("bingo-challenges.yml", false);
                plugin.getLogger().info("Archivo bingo-challenges.yml creado desde recursos");
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("No se encontró bingo-challenges.yml en recursos, creando archivo vacío");
                createEmptyChallengesFile();
            }
        }

        challengesConfig = YamlConfiguration.loadConfiguration(challengesFile);
    }

    private void createEmptyChallengesFile() {
        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            challengesFile.createNewFile();
            FileConfiguration config = YamlConfiguration.loadConfiguration(challengesFile);

            config.set("challenges.1.type", "COLLECT_ITEM");
            config.set("challenges.1.material", "DIAMOND");
            config.set("challenges.1.amount", 5);
            config.set("challenges.1.display-name", "&bDiamantes Brillantes");
            config.set("challenges.1.description", "&7Consigue 5 diamantes");
            config.set("challenges.1.icon", "DIAMOND");

            config.save(challengesFile);
            plugin.getLogger().info("Archivo bingo-challenges.yml creado con retos de ejemplo");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al crear bingo-challenges.yml: " + e.getMessage());
        }
    }

    private void loadMasterChallengeList() {
        masterChallengeList.clear();

        if (!challengesConfig.contains("challenges")) {
            plugin.getLogger().warning("No se encontró la sección 'challenges' en bingo-challenges.yml");
            return;
        }

        var challengesSection = challengesConfig.getConfigurationSection("challenges");
        if (challengesSection == null) {
            plugin.getLogger().warning("La sección 'challenges' está vacía");
            return;
        }

        for (String key : challengesSection.getKeys(false)) {
            try {
                int id = Integer.parseInt(key);
                String path = "challenges." + key;

                String type = challengesConfig.getString(path + ".type");
                String materialOrMob = challengesConfig.getString(path + ".material",
                        challengesConfig.getString(path + ".mob"));
                int amount = challengesConfig.getInt(path + ".amount");
                String displayName = challengesConfig.getString(path + ".display-name");
                String description = challengesConfig.getString(path + ".description");
                String icon = challengesConfig.getString(path + ".icon");

                BingoChallenge challenge = new BingoChallenge(
                        id,
                        type,
                        materialOrMob,
                        amount,
                        displayName,
                        description,
                        icon
                );

                masterChallengeList.put(id, challenge);
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("ID de reto inválido: " + key);
            }
        }

        plugin.getLogger().info("Cargados " + masterChallengeList.size() + " retos en la lista maestra");
    }

    public void reloadChallenges() {
        challengesConfig = YamlConfiguration.loadConfiguration(challengesFile);
        loadMasterChallengeList();
        plugin.getLogger().info("Lista de retos recargada");
    }

    public Map<String, BingoChallenge> generateNewBingoTable(int gridSize) {
        int totalSlots = gridSize * gridSize;

        if (masterChallengeList.size() < totalSlots) {
            plugin.getLogger().warning("No hay suficientes retos en la lista maestra (" +
                    masterChallengeList.size() + ") para llenar una tabla de " +
                    totalSlots + " espacios");
            return null;
        }

        List<Integer> availableIds = new ArrayList<>(masterChallengeList.keySet());
        Collections.shuffle(availableIds);

        Map<String, BingoChallenge> newTable = new HashMap<>();

        for (int i = 0; i < totalSlots; i++) {
            int originalId = availableIds.get(i);
            BingoChallenge originalChallenge = masterChallengeList.get(originalId);

            BingoChallenge newChallenge = new BingoChallenge(
                    i + 1,
                    originalChallenge.type(),
                    originalChallenge.target(),
                    originalChallenge.amount(),
                    originalChallenge.displayName(),
                    originalChallenge.description(),
                    originalChallenge.icon()
            );

            newTable.put(String.valueOf(i + 1), newChallenge);
        }

        plugin.getLogger().info("Generada nueva tabla de bingo con " + totalSlots + " retos aleatorios");
        return newTable;
    }

    public void saveBingoTableToConfig(Map<String, BingoChallenge> table, FileConfiguration bingoConfig, File bingoFile) {
        try {
            if (bingoConfig.contains("bingo.challenges")) {
                bingoConfig.set("bingo.challenges", null);
            }

            for (Map.Entry<String, BingoChallenge> entry : table.entrySet()) {
                String key = entry.getKey();
                BingoChallenge challenge = entry.getValue();
                String path = "bingo.challenges." + key;

                bingoConfig.set(path + ".type", challenge.type());

                if (challenge.type().equals("KILL_MOB")) {
                    bingoConfig.set(path + ".mob", challenge.target());
                } else {
                    bingoConfig.set(path + ".material", challenge.target());
                }

                bingoConfig.set(path + ".amount", challenge.amount());
                bingoConfig.set(path + ".display-name", challenge.displayName());
                bingoConfig.set(path + ".description", challenge.description());
                bingoConfig.set(path + ".icon", challenge.icon());
            }

            bingoConfig.save(bingoFile);
            plugin.getLogger().info("Tabla de bingo guardada en bingo.yml");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar tabla de bingo: " + e.getMessage());
        }
    }

    public int getTotalChallengesAvailable() {
        return masterChallengeList.size();
    }

    public Map<Integer, BingoChallenge> getMasterChallengeList() {
        return new HashMap<>(masterChallengeList);
    }

    public BingoChallenge getMasterChallenge(int id) {
        return masterChallengeList.get(id);
    }
}
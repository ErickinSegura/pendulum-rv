package org.delta.managers.bingo;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delta.pendulum;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BingoDataManager {
    private static BingoDataManager instance;
    private final pendulum plugin;
    private File bingoFile;
    private FileConfiguration bingoConfig;
    private File progressFile;
    private FileConfiguration progressConfig;

    private final Map<String, BingoChallenge> challenges;

    private BingoDataManager(pendulum plugin) {
        this.plugin = plugin;
        this.challenges = new HashMap<>();

        BingoChallengesManager.getInstance(plugin);

        loadConfiguration();
        loadChallenges();
        loadProgress();
    }

    public static BingoDataManager getInstance(pendulum plugin) {
        if (instance == null) {
            instance = new BingoDataManager(plugin);
            BingoScoreManager.getInstance(plugin);
        }
        return instance;
    }

    public static BingoDataManager getInstance() {
        return instance;
    }

    private void loadConfiguration() {
        bingoFile = new File(plugin.getDataFolder(), "bingo.yml");

        if (!bingoFile.exists()) {
            createDefaultConfig();
        }

        bingoConfig = YamlConfiguration.loadConfiguration(bingoFile);

        progressFile = new File(plugin.getDataFolder(), "bingo-progress.yml");
        if (!progressFile.exists()) {
            try {
                progressFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error al crear bingo-progress.yml: " + e.getMessage());
            }
        }
        progressConfig = YamlConfiguration.loadConfiguration(progressFile);
    }

    private void createDefaultConfig() {
        try {
            plugin.saveResource("bingo.yml", false);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().info("Creando archivo bingo.yml por defecto...");

            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }

            try {
                bingoFile.createNewFile();
                FileConfiguration config = YamlConfiguration.loadConfiguration(bingoFile);

                config.set("bingo.enabled", true);
                config.set("bingo.grid-size", 5);

                generateInitialTable(config);

                config.set("bingo.messages.challenge-completed", "&a¡{player} ha completado el reto: {challenge}!");
                config.set("bingo.messages.bingo-completed", "&6&l¡BINGO! &e¡Tu equipo ha completado {line}!");
                config.set("bingo.messages.team-progress", "&7Progreso del equipo: &a{completed}&7/&c{total}");

                config.save(bingoFile);
                plugin.getLogger().info("Archivo bingo.yml creado exitosamente!");
            } catch (IOException ex) {
                plugin.getLogger().severe("Error al crear bingo.yml: " + ex.getMessage());
            }
        }
    }

    /**
     * Genera una tabla inicial de bingo usando el sistema de retos maestros
     */
    private void generateInitialTable(FileConfiguration config) {
        int gridSize = config.getInt("bingo.grid-size", 5);
        BingoChallengesManager challengesManager = BingoChallengesManager.getInstance();

        Map<String, BingoChallenge> newTable = challengesManager.generateNewBingoTable(gridSize);

        if (newTable != null) {
            for (Map.Entry<String, BingoChallenge> entry : newTable.entrySet()) {
                String key = entry.getKey();
                BingoChallenge challenge = entry.getValue();
                String path = "bingo.challenges." + key;

                config.set(path + ".type", challenge.type());

                if (challenge.type().equals("KILL_MOB")) {
                    config.set(path + ".mob", challenge.target());
                } else {
                    config.set(path + ".material", challenge.target());
                }

                config.set(path + ".amount", challenge.amount());
                config.set(path + ".display-name", challenge.displayName());
                config.set(path + ".description", challenge.description());
                config.set(path + ".icon", challenge.icon());
            }
            plugin.getLogger().info("Tabla inicial de bingo generada con " + newTable.size() + " retos");
        } else {
            plugin.getLogger().warning("No se pudo generar tabla inicial, usando retos por defecto");
            createDefaultChallenges(config);
        }
    }

    /**
     * Crea retos por defecto como fallback (solo se usa si falla el sistema de retos maestros)
     */
    private void createDefaultChallenges(FileConfiguration config) {
        String[][] defaultChallenges = {
                {"COLLECT_ITEM", "DIAMOND", "5", "&bDiamantes Brillantes", "&7Consigue 5 diamantes", "DIAMOND"},
                {"COLLECT_ITEM", "EMERALD", "3", "&aEsmeraldas Valiosas", "&7Consigue 3 esmeraldas", "EMERALD"},
                {"KILL_MOB", "ZOMBIE", "20", "&cMatador de Zombies", "&7Mata 20 zombies", "ROTTEN_FLESH"},
                {"KILL_MOB", "SKELETON", "15", "&fCazador de Esqueletos", "&7Mata 15 esqueletos", "BONE"},
                {"MINE_BLOCK", "STONE", "500", "&7Minero de Piedra", "&7Mina 500 bloques de piedra", "STONE"},
                {"MINE_BLOCK", "COAL_ORE", "64", "&8Carbón Abundante", "&7Mina 64 minerales de carbón", "COAL_ORE"},
                {"COLLECT_ITEM", "GOLDEN_APPLE", "1", "&6Manzana Dorada", "&7Consigue 1 manzana dorada", "GOLDEN_APPLE"},
                {"KILL_MOB", "CREEPER", "10", "&aExplosivo", "&7Mata 10 creepers", "GUNPOWDER"},
                {"MINE_BLOCK", "IRON_ORE", "32", "&fHierro Forjado", "&7Mina 32 minerales de hierro", "IRON_ORE"},
                {"COLLECT_ITEM", "ENDER_PEARL", "8", "&5Perlas del End", "&7Consigue 8 perlas de ender", "ENDER_PEARL"},
                {"KILL_MOB", "SPIDER", "25", "&8Exterminador de Arañas", "&7Mata 25 arañas", "SPIDER_EYE"},
                {"MINE_BLOCK", "GOLD_ORE", "16", "&6Oro Reluciente", "&7Mina 16 minerales de oro", "GOLD_ORE"},
                {"COLLECT_ITEM", "BLAZE_ROD", "5", "&eVaras Flamígeras", "&7Consigue 5 varas de blaze", "BLAZE_ROD"},
                {"KILL_MOB", "ENDERMAN", "5", "&5Cazador del End", "&7Mata 5 enderman", "ENDER_PEARL"},
                {"MINE_BLOCK", "DIAMOND_ORE", "8", "&bMinero de Diamantes", "&7Mina 8 minerales de diamante", "DIAMOND_ORE"},
                {"COLLECT_ITEM", "GHAST_TEAR", "3", "&fLágrimas Espectrales", "&7Consigue 3 lágrimas de ghast", "GHAST_TEAR"},
                {"COLLECT_ITEM", "DIAMOND", "5", "&bDiamantes Brillantes", "&7Consigue 5 diamantes", "DIAMOND"},
                {"COLLECT_ITEM", "EMERALD", "3", "&aEsmeraldas Valiosas", "&7Consigue 3 esmeraldas", "EMERALD"},
                {"KILL_MOB", "ZOMBIE", "20", "&cMatador de Zombies", "&7Mata 20 zombies", "ROTTEN_FLESH"},
                {"KILL_MOB", "SKELETON", "15", "&fCazador de Esqueletos", "&7Mata 15 esqueletos", "BONE"},
                {"MINE_BLOCK", "STONE", "500", "&7Minero de Piedra", "&7Mina 500 bloques de piedra", "STONE"},
                {"MINE_BLOCK", "COAL_ORE", "64", "&8Carbón Abundante", "&7Mina 64 minerales de carbón", "COAL_ORE"},
                {"COLLECT_ITEM", "GOLDEN_APPLE", "1", "&6Manzana Dorada", "&7Consigue 1 manzana dorada", "GOLDEN_APPLE"},
                {"KILL_MOB", "CREEPER", "10", "&aExplosivo", "&7Mata 10 creepers", "GUNPOWDER"},
                {"MINE_BLOCK", "IRON_ORE", "32", "&fHierro Forjado", "&7Mina 32 minerales de hierro", "IRON_ORE"}
        };

        for (int i = 0; i < defaultChallenges.length; i++) {
            String[] challenge = defaultChallenges[i];
            String path = "bingo.challenges." + (i + 1);

            config.set(path + ".type", challenge[0]);

            if (challenge[0].equals("KILL_MOB")) {
                config.set(path + ".mob", challenge[1]);
            } else {
                config.set(path + ".material", challenge[1]);
            }

            config.set(path + ".amount", Integer.parseInt(challenge[2]));
            config.set(path + ".display-name", challenge[3]);
            config.set(path + ".description", challenge[4]);
            config.set(path + ".icon", challenge[5]);
        }
    }

    private void loadChallenges() {
        challenges.clear();

        if (!bingoConfig.contains("bingo.challenges")) {
            plugin.getLogger().warning("No se encontraron retos en bingo.yml");
            return;
        }

        for (String key : Objects.requireNonNull(bingoConfig.getConfigurationSection("bingo.challenges")).getKeys(false)) {
            String path = "bingo.challenges." + key;

            BingoChallenge challenge = new BingoChallenge(
                    Integer.parseInt(key),
                    bingoConfig.getString(path + ".type"),
                    bingoConfig.getString(path + ".material", bingoConfig.getString(path + ".mob")),
                    bingoConfig.getInt(path + ".amount"),
                    bingoConfig.getString(path + ".display-name"),
                    bingoConfig.getString(path + ".description"),
                    bingoConfig.getString(path + ".icon")
            );

            challenges.put(key, challenge);
        }

        plugin.getLogger().info("Cargados " + challenges.size() + " retos de bingo");
    }

    /**
     * Genera una nueva tabla de bingo aleatoria
     * @return true si se generó exitosamente, false si hubo un error
     */
    public boolean generateNewBingoTable() {
        try {
            int gridSize = getGridSize();
            BingoChallengesManager challengesManager = BingoChallengesManager.getInstance();

            Map<String, BingoChallenge> newTable = challengesManager.generateNewBingoTable(gridSize);

            if (newTable == null) {
                plugin.getLogger().severe("No se pudo generar nueva tabla de bingo");
                return false;
            }

            challengesManager.saveBingoTableToConfig(newTable, bingoConfig, bingoFile);

            loadChallenges();

            plugin.getLogger().info("Nueva tabla de bingo generada y cargada exitosamente");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error al generar nueva tabla de bingo: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void saveProgress() {
        try {
            BingoProgressManager progressManager = BingoProgressManager.getInstance();
            Map<String, Object> data = progressManager.getProgressData();

            for (String key : progressConfig.getKeys(false)) {
                progressConfig.set(key, null);
            }

            for (Map.Entry<String, Object> entry : data.entrySet()) {
                progressConfig.set(entry.getKey(), entry.getValue());
            }

            progressConfig.save(progressFile);

            BingoScoreManager.getInstance().saveScoreData();

            plugin.getLogger().info("Progreso de bingo guardado exitosamente");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar progreso de bingo: " + e.getMessage());
        }
    }

    private Map<String, Object> convertConfigSectionToMap(ConfigurationSection section) {
        Map<String, Object> result = new HashMap<>();

        for (String key : section.getKeys(false)) {
            Object value = section.get(key);

            if (value instanceof ConfigurationSection) {
                result.put(key, convertConfigSectionToMap((ConfigurationSection) value));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }

    public void loadProgress() {
        try {
            if (!progressFile.exists()) {
                plugin.getLogger().info("No hay progreso previo de bingo para cargar");
                return;
            }

            progressConfig = YamlConfiguration.loadConfiguration(progressFile);

            Set<String> keys = progressConfig.getKeys(false);
            if (keys.isEmpty()) {
                plugin.getLogger().info("El archivo bingo-progress.yml está vacío");
                return;
            }

            Map<String, Object> data = new HashMap<>();

            for (String key : keys) {
                Object value = progressConfig.get(key);

                if (value instanceof ConfigurationSection) {
                    data.put(key, convertConfigSectionToMap((ConfigurationSection) value));
                } else {
                    data.put(key, value);
                }
            }

            BingoProgressManager.getInstance().loadProgress(data);
            plugin.getLogger().info("Progreso de bingo cargado exitosamente");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al cargar progreso de bingo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void resetProgress() {
        BingoProgressManager.getInstance().resetAllProgress();
        BingoScoreManager.getInstance().resetAllScores();

        try {
            for (String key : progressConfig.getKeys(false)) {
                progressConfig.set(key, null);
            }
            progressConfig.save(progressFile);

            plugin.getLogger().info("Progreso de bingo reseteado");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al resetear progreso: " + e.getMessage());
        }
    }

    public Map<String, BingoChallenge> getChallenges() {
        return new HashMap<>(challenges);
    }

    public BingoChallenge getChallenge(String id) {
        return challenges.get(id);
    }

    public int getGridSize() {
        return bingoConfig.getInt("bingo.grid-size", 5);
    }

    public boolean isEnabled() {
        return bingoConfig.getBoolean("bingo.enabled", true);
    }

    public String getMessage(String key) {
        return bingoConfig.getString("bingo.messages." + key, "&cMensaje no configurado");
    }
}
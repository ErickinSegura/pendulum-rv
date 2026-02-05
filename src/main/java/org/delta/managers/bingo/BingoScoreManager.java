package org.delta.managers.bingo;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.delta.pendulum;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class BingoScoreManager {
    private static BingoScoreManager instance;
    private final pendulum plugin;
    private File scoreFile;
    private FileConfiguration scoreConfig;

    private static final int CHALLENGE_BASE_POINTS = 100;
    private static final int CHALLENGE_MIN_POINTS = 50;
    private static final int LINE_BASE_POINTS = 500;
    private static final int LINE_MIN_POINTS = 250;
    private static final int FULL_BINGO_BASE_POINTS = 1000;
    private static final int FULL_BINGO_MIN_POINTS = 500;

    private final Map<Integer, List<String>> challengeCompletions;
    private final Map<String, List<String>> lineCompletions;
    private final List<String> fullBingoCompletions;

    private final Map<String, List<ScoreEntry>> teamScoreHistory;

    private BingoScoreManager(pendulum plugin) {
        this.plugin = plugin;
        this.challengeCompletions = new HashMap<>();
        this.lineCompletions = new HashMap<>();
        this.fullBingoCompletions = new ArrayList<>();
        this.teamScoreHistory = new HashMap<>();
        loadConfiguration();
        loadScoreData();
    }

    public static BingoScoreManager getInstance(pendulum plugin) {
        if (instance == null) {
            instance = new BingoScoreManager(plugin);
        }
        return instance;
    }

    public static BingoScoreManager getInstance() {
        return instance;
    }

    private void loadConfiguration() {
        scoreFile = new File(plugin.getDataFolder(), "bingo-scores.yml");
        if (!scoreFile.exists()) {
            try {
                scoreFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Error al crear bingo-scores.yml: " + e.getMessage());
            }
        }
        scoreConfig = YamlConfiguration.loadConfiguration(scoreFile);
    }

    public int registerChallengeCompletion(String teamName, int challengeId, String challengeName) {
        challengeCompletions.putIfAbsent(challengeId, new ArrayList<>());
        List<String> completions = challengeCompletions.get(challengeId);

        if (completions.contains(teamName)) {
            return 0;
        }

        completions.add(teamName);
        int position = completions.size();
        int points = calculateChallengePoints(position);

        addScoreEntry(teamName, "CHALLENGE", challengeName, points, position);
        saveScoreData();

        return points;
    }

    public int registerLineCompletion(String teamName, String lineType, String lineName) {
        lineCompletions.putIfAbsent(lineType, new ArrayList<>());
        List<String> completions = lineCompletions.get(lineType);

        if (completions.contains(teamName)) {
            return 0;
        }

        completions.add(teamName);
        int position = completions.size();
        int points = calculateLinePoints(position);

        addScoreEntry(teamName, "LINE", lineName, points, position);
        saveScoreData();

        return points;
    }

    public int registerFullBingoCompletion(String teamName) {
        if (fullBingoCompletions.contains(teamName)) {
            return 0;
        }

        fullBingoCompletions.add(teamName);
        int position = fullBingoCompletions.size();
        int points = calculateFullBingoPoints(position);

        addScoreEntry(teamName, "FULL_BINGO", "Bingo Completo", points, position);
        saveScoreData();

        return points;
    }

    private int calculateChallengePoints(int position) {
        int points = CHALLENGE_BASE_POINTS - ((position - 1) * 10);
        return Math.max(points, CHALLENGE_MIN_POINTS);
    }

    private int calculateLinePoints(int position) {
        int points = LINE_BASE_POINTS - ((position - 1) * 50);
        return Math.max(points, LINE_MIN_POINTS);
    }

    private int calculateFullBingoPoints(int position) {
        int points = FULL_BINGO_BASE_POINTS - ((position - 1) * 100);
        return Math.max(points, FULL_BINGO_MIN_POINTS);
    }

    private void addScoreEntry(String teamName, String type, String description, int points, int position) {
        teamScoreHistory.putIfAbsent(teamName, new ArrayList<>());
        ScoreEntry entry = new ScoreEntry(
                type,
                description,
                points,
                position,
                System.currentTimeMillis()
        );
        teamScoreHistory.get(teamName).add(entry);
    }

    public int getTotalScore(String teamName) {
        List<ScoreEntry> entries = teamScoreHistory.get(teamName);
        if (entries == null) return 0;

        return entries.stream()
                .mapToInt(ScoreEntry::points)
                .sum();
    }

    public List<ScoreEntry> getScoreHistory(String teamName) {
        return new ArrayList<>(teamScoreHistory.getOrDefault(teamName, new ArrayList<>()));
    }

    public Map<String, Integer> getLeaderboard() {
        Map<String, Integer> leaderboard = new HashMap<>();

        for (Map.Entry<String, List<ScoreEntry>> entry : teamScoreHistory.entrySet()) {
            int total = entry.getValue().stream()
                    .mapToInt(ScoreEntry::points)
                    .sum();
            leaderboard.put(entry.getKey(), total);
        }

        return leaderboard;
    }

    public List<Map.Entry<String, Integer>> getSortedLeaderboard() {
        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(getLeaderboard().entrySet());
        sortedList.sort((a, b) -> b.getValue().compareTo(a.getValue()));
        return sortedList;
    }

    public void resetAllScores() {
        challengeCompletions.clear();
        lineCompletions.clear();
        fullBingoCompletions.clear();
        teamScoreHistory.clear();
        saveScoreData();
    }

    public void resetTeamScore(String teamName) {
        for (List<String> completions : challengeCompletions.values()) {
            completions.remove(teamName);
        }
        for (List<String> completions : lineCompletions.values()) {
            completions.remove(teamName);
        }
        fullBingoCompletions.remove(teamName);

        teamScoreHistory.remove(teamName);
        saveScoreData();
    }

    public void saveScoreData() {
        try {
            for (String key : scoreConfig.getKeys(false)) {
                scoreConfig.set(key, null);
            }

            Map<String, List<String>> challengeCompletionsData = new HashMap<>();
            for (Map.Entry<Integer, List<String>> entry : challengeCompletions.entrySet()) {
                challengeCompletionsData.put(String.valueOf(entry.getKey()), entry.getValue());
            }
            scoreConfig.set("challenge-completions", challengeCompletionsData);

            scoreConfig.set("line-completions", lineCompletions);

            scoreConfig.set("full-bingo-completions", fullBingoCompletions);

            Map<String, List<Map<String, Object>>> historyData = new HashMap<>();
            for (Map.Entry<String, List<ScoreEntry>> entry : teamScoreHistory.entrySet()) {
                List<Map<String, Object>> entries = new ArrayList<>();
                for (ScoreEntry scoreEntry : entry.getValue()) {
                    Map<String, Object> entryMap = new HashMap<>();
                    entryMap.put("type", scoreEntry.type());
                    entryMap.put("description", scoreEntry.description());
                    entryMap.put("points", scoreEntry.points());
                    entryMap.put("position", scoreEntry.position());
                    entryMap.put("timestamp", scoreEntry.timestamp());
                    entries.add(entryMap);
                }
                historyData.put(entry.getKey(), entries);
            }
            scoreConfig.set("score-history", historyData);

            scoreConfig.save(scoreFile);
            plugin.getLogger().info("Puntuaciones de bingo guardadas exitosamente");
        } catch (IOException e) {
            plugin.getLogger().severe("Error al guardar puntuaciones: " + e.getMessage());
        }
    }

    private void loadScoreData() {
        try {
            if (!scoreFile.exists()) {
                plugin.getLogger().info("No hay puntuaciones previas de bingo para cargar");
                return;
            }

            scoreConfig = YamlConfiguration.loadConfiguration(scoreFile);

            if (scoreConfig.contains("challenge-completions")) {
                var section = scoreConfig.getConfigurationSection("challenge-completions");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        try {
                            int challengeId = Integer.parseInt(key);
                            List<String> teams = section.getStringList(key);
                            challengeCompletions.put(challengeId, new ArrayList<>(teams));
                        } catch (NumberFormatException e) {
                            plugin.getLogger().warning("Error al parsear ID de reto: " + key);
                        }
                    }
                }
            }

            if (scoreConfig.contains("line-completions")) {
                var section = scoreConfig.getConfigurationSection("line-completions");
                if (section != null) {
                    for (String key : section.getKeys(false)) {
                        List<String> teams = section.getStringList(key);
                        lineCompletions.put(key, new ArrayList<>(teams));
                    }
                }
            }

            if (scoreConfig.contains("full-bingo-completions")) {
                List<String> data = scoreConfig.getStringList("full-bingo-completions");
                fullBingoCompletions.addAll(data);
            }

            if (scoreConfig.contains("score-history")) {
                var section = scoreConfig.getConfigurationSection("score-history");
                if (section != null) {
                    for (String teamName : section.getKeys(false)) {
                        List<ScoreEntry> entries = new ArrayList<>();
                        List<?> entryList = section.getList(teamName);

                        if (entryList != null) {
                            for (Object obj : entryList) {
                                if (obj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> entryMap = (Map<String, Object>) obj;

                                    ScoreEntry scoreEntry = new ScoreEntry(
                                            (String) entryMap.get("type"),
                                            (String) entryMap.get("description"),
                                            ((Number) entryMap.get("points")).intValue(),
                                            ((Number) entryMap.get("position")).intValue(),
                                            ((Number) entryMap.get("timestamp")).longValue()
                                    );
                                    entries.add(scoreEntry);
                                }
                            }
                        }
                        teamScoreHistory.put(teamName, entries);
                    }
                }
            }

            plugin.getLogger().info("Puntuaciones de bingo cargadas exitosamente");
        } catch (Exception e) {
            plugin.getLogger().severe("Error al cargar puntuaciones: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public record ScoreEntry(
            String type,
            String description,
            int points,
            int position,
            long timestamp
    ) {
        public String getFormattedType() {
            return switch (type) {
                case "CHALLENGE" -> "Reto";
                case "LINE" -> "Línea";
                case "FULL_BINGO" -> "Bingo Completo";
                default -> type;
            };
        }
    }
}
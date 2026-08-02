package org.delta.managers.bingo;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;
import org.delta.database.BingoSyncManager;

import java.util.*;

public class BingoProgressManager {
    private static BingoProgressManager instance;

    private final Map<String, Map<Integer, Integer>> teamProgress;
    private final Map<String, Set<Integer>> completedChallenges;
    private final Map<String, Set<Integer>> completedRows;
    private final Map<String, Set<Integer>> completedColumns;
    private final Map<String, Boolean> completedDiagonal1;
    private final Map<String, Boolean> completedDiagonal2;

    private BingoProgressManager() {
        this.teamProgress = new HashMap<>();
        this.completedChallenges = new HashMap<>();
        this.completedRows = new HashMap<>();
        this.completedColumns = new HashMap<>();
        this.completedDiagonal1 = new HashMap<>();
        this.completedDiagonal2 = new HashMap<>();
    }

    public static BingoProgressManager getInstance() {
        if (instance == null) {
            instance = new BingoProgressManager();
        }
        return instance;
    }


    public void addProgress(String teamName, int challengeId, int amount) {
        teamProgress.computeIfAbsent(teamName, k -> new HashMap<>());
        int current = teamProgress.get(teamName).getOrDefault(challengeId, 0);
        int newValue = current + amount;
        teamProgress.get(teamName).put(challengeId, newValue);

        syncProgress(teamName, challengeId, newValue);
    }

    public void setProgress(String teamName, int challengeId, int amount) {
        teamProgress.computeIfAbsent(teamName, k -> new HashMap<>());
        teamProgress.get(teamName).put(challengeId, amount);

        syncProgress(teamName, challengeId, amount);
    }

    public int getProgress(String teamName, int challengeId) {
        return teamProgress.getOrDefault(teamName, new HashMap<>()).getOrDefault(challengeId, 0);
    }

    public Map<Integer, Integer> getAllProgress(String teamName) {
        return new HashMap<>(teamProgress.getOrDefault(teamName, new HashMap<>()));
    }

    public Set<String> getAllTeamNames() {
        Set<String> teams = new HashSet<>();
        teams.addAll(teamProgress.keySet());
        teams.addAll(completedChallenges.keySet());
        return teams;
    }


    public void completeChallenge(String teamName, int challengeId) {
        completedChallenges.computeIfAbsent(teamName, k -> new HashSet<>());
        completedChallenges.get(teamName).add(challengeId);

        BingoChallenge challenge = BingoDataManager.getInstance().getChallenge(String.valueOf(challengeId));
        int finalProgress = challenge != null ? challenge.amount() : getProgress(teamName, challengeId);
        syncProgress(teamName, challengeId, finalProgress, true);
    }

    public boolean isChallengeCompleted(String teamName, int challengeId) {
        return completedChallenges.getOrDefault(teamName, new HashSet<>()).contains(challengeId);
    }

    public Set<Integer> getCompletedChallenges(String teamName) {
        return new HashSet<>(completedChallenges.getOrDefault(teamName, new HashSet<>()));
    }


    public void markRowCompleted(String teamName, int row) {
        completedRows.computeIfAbsent(teamName, k -> new HashSet<>());
        completedRows.get(teamName).add(row);
    }

    public boolean hasCompletedRow(String teamName, int row) {
        return completedRows.getOrDefault(teamName, new HashSet<>()).contains(row);
    }

    public void markColumnCompleted(String teamName, int col) {
        completedColumns.computeIfAbsent(teamName, k -> new HashSet<>());
        completedColumns.get(teamName).add(col);
    }

    public boolean hasCompletedColumn(String teamName, int col) {
        return completedColumns.getOrDefault(teamName, new HashSet<>()).contains(col);
    }

    public void markDiagonal1Completed(String teamName) {
        completedDiagonal1.put(teamName, true);
    }

    public boolean hasCompletedDiagonal1(String teamName) {
        return completedDiagonal1.getOrDefault(teamName, false);
    }

    public void markDiagonal2Completed(String teamName) {
        completedDiagonal2.put(teamName, true);
    }

    public boolean hasCompletedDiagonal2(String teamName) {
        return completedDiagonal2.getOrDefault(teamName, false);
    }

    public Set<Integer> getCompletedRows(String teamName) {
        return new HashSet<>(completedRows.getOrDefault(teamName, new HashSet<>()));
    }

    public Set<Integer> getCompletedColumns(String teamName) {
        return new HashSet<>(completedColumns.getOrDefault(teamName, new HashSet<>()));
    }


    public void resetTeamProgress(String teamName) {
        teamProgress.remove(teamName);
        completedChallenges.remove(teamName);
        completedRows.remove(teamName);
        completedColumns.remove(teamName);
        completedDiagonal1.remove(teamName);
        completedDiagonal2.remove(teamName);
    }

    public void resetAllProgress() {
        teamProgress.clear();
        completedChallenges.clear();
        completedRows.clear();
        completedColumns.clear();
        completedDiagonal1.clear();
        completedDiagonal2.clear();
    }



    private void syncProgress(String teamName, int challengeId, int progress) {
        boolean completed = isChallengeCompleted(teamName, challengeId);
        syncProgress(teamName, challengeId, progress, completed);
    }


    private void syncProgress(String teamName, int challengeId, int progress, boolean completed) {
        BingoSyncManager sync = BingoSyncManager.getInstance();
        if (sync == null) return;

        long teamId = sync.resolveTeamId(teamName);
        if (teamId == -1L) return;

        sync.syncProgress(teamId, challengeId, progress, completed);
    }

    public void loadProgress(Map<String, Object> data) {
        if (data.containsKey("progress")) {
            Object progressObj = data.get("progress");
            if (progressObj instanceof Map<?, ?> progressData) {
                for (Map.Entry<?, ?> entry : progressData.entrySet()) {
                    String teamName = entry.getKey().toString();
                    Object teamProgressObj = entry.getValue();

                    if (teamProgressObj instanceof Map<?, ?> teamProgressMap) {
                        Map<Integer, Integer> converted = new HashMap<>();
                        for (Map.Entry<?, ?> prog : teamProgressMap.entrySet()) {
                            try {
                                Integer key = prog.getKey() instanceof Number ?
                                        ((Number) prog.getKey()).intValue() :
                                        Integer.parseInt(prog.getKey().toString());
                                Integer value = prog.getValue() instanceof Number ?
                                        ((Number) prog.getValue()).intValue() :
                                        Integer.parseInt(prog.getValue().toString());
                                converted.put(key, value);
                            } catch (NumberFormatException e) {
                                Bukkit.getLogger().warning("Error parsing progress entry: " +
                                        prog.getKey() + " = " + prog.getValue());
                            }
                        }
                        teamProgress.put(teamName, converted);
                    }
                }
            }
        }

        if (data.containsKey("completed")) {
            Object completedObj = data.get("completed");
            if (completedObj instanceof Map<?, ?> completedData) {
                for (Map.Entry<?, ?> entry : completedData.entrySet()) {
                    String teamName = entry.getKey().toString();
                    Object challengesObj = entry.getValue();

                    if (challengesObj instanceof List<?> challengesList) {
                        Set<Integer> challenges = new HashSet<>();
                        for (Object id : challengesList) {
                            try {
                                Integer challengeId = id instanceof Number ?
                                        ((Number) id).intValue() :
                                        Integer.parseInt(id.toString());
                                challenges.add(challengeId);
                            } catch (NumberFormatException e) {
                                Bukkit.getLogger().warning("Error parsing completed challenge: " + id);
                            }
                        }
                        completedChallenges.put(teamName, challenges);
                    }
                }
            }
        }

        if (data.containsKey("rows")) {
            Object rowsObj = data.get("rows");
            if (rowsObj instanceof Map<?, ?> rowsData) {
                for (Map.Entry<?, ?> entry : rowsData.entrySet()) {
                    String teamName = entry.getKey().toString();
                    Object rowsListObj = entry.getValue();

                    if (rowsListObj instanceof List<?> rowsList) {
                        Set<Integer> rows = new HashSet<>();
                        for (Object row : rowsList) {
                            try {
                                Integer rowId = row instanceof Number ?
                                        ((Number) row).intValue() :
                                        Integer.parseInt(row.toString());
                                rows.add(rowId);
                            } catch (NumberFormatException e) {
                                Bukkit.getLogger().warning("Error parsing row: " + row);
                            }
                        }
                        completedRows.put(teamName, rows);
                    }
                }
            }
        }

        if (data.containsKey("columns")) {
            Object columnsObj = data.get("columns");
            if (columnsObj instanceof Map<?, ?> columnsData) {
                for (Map.Entry<?, ?> entry : columnsData.entrySet()) {
                    String teamName = entry.getKey().toString();
                    Object columnsListObj = entry.getValue();

                    if (columnsListObj instanceof List<?> columnsList) {
                        Set<Integer> columns = new HashSet<>();
                        for (Object col : columnsList) {
                            try {
                                Integer colId = col instanceof Number ?
                                        ((Number) col).intValue() :
                                        Integer.parseInt(col.toString());
                                columns.add(colId);
                            } catch (NumberFormatException e) {
                                Bukkit.getLogger().warning("Error parsing column: " + col);
                            }
                        }
                        completedColumns.put(teamName, columns);
                    }
                }
            }
        }

        if (data.containsKey("diagonal1")) {
            Object diagonal1Obj = data.get("diagonal1");
            if (diagonal1Obj instanceof Map<?, ?> diagonal1Data) {
                for (Map.Entry<?, ?> entry : diagonal1Data.entrySet()) {
                    try {
                        Boolean value = entry.getValue() instanceof Boolean ?
                                (Boolean) entry.getValue() :
                                Boolean.parseBoolean(entry.getValue().toString());
                        completedDiagonal1.put(entry.getKey().toString(), value);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error parsing diagonal1: " + entry.getValue());
                    }
                }
            }
        }

        if (data.containsKey("diagonal2")) {
            Object diagonal2Obj = data.get("diagonal2");
            if (diagonal2Obj instanceof Map<?, ?> diagonal2Data) {
                for (Map.Entry<?, ?> entry : diagonal2Data.entrySet()) {
                    try {
                        Boolean value = entry.getValue() instanceof Boolean ?
                                (Boolean) entry.getValue() :
                                Boolean.parseBoolean(entry.getValue().toString());
                        completedDiagonal2.put(entry.getKey().toString(), value);
                    } catch (Exception e) {
                        Bukkit.getLogger().warning("Error parsing diagonal2: " + entry.getValue());
                    }
                }
            }
        }
    }

    public Map<String, Object> getProgressData() {
        Map<String, Object> data = new HashMap<>();

        Map<String, Map<String, Integer>> progressData = new HashMap<>();
        for (Map.Entry<String, Map<Integer, Integer>> entry : teamProgress.entrySet()) {
            Map<String, Integer> converted = new HashMap<>();
            for (Map.Entry<Integer, Integer> prog : entry.getValue().entrySet()) {
                converted.put(String.valueOf(prog.getKey()), prog.getValue());
            }
            progressData.put(entry.getKey(), converted);
        }
        data.put("progress", progressData);

        Map<String, List<Integer>> completedData = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : completedChallenges.entrySet()) {
            completedData.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        data.put("completed", completedData);

        Map<String, List<Integer>> rowsData = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : completedRows.entrySet()) {
            rowsData.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        data.put("rows", rowsData);

        Map<String, List<Integer>> columnsData = new HashMap<>();
        for (Map.Entry<String, Set<Integer>> entry : completedColumns.entrySet()) {
            columnsData.put(entry.getKey(), new ArrayList<>(entry.getValue()));
        }
        data.put("columns", columnsData);

        data.put("diagonal1", new HashMap<>(completedDiagonal1));
        data.put("diagonal2", new HashMap<>(completedDiagonal2));

        return data;
    }


    public Team getPlayerTeam(String playerName) {
        for (Team team : Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            if (team.hasEntry(playerName)) {
                return team;
            }
        }
        return null;
    }
}
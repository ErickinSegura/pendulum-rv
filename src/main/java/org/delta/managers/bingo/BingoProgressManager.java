package org.delta.managers.bingo;

import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class BingoProgressManager {
    private static BingoProgressManager instance;

    private final Map<String, Map<Integer, Integer>> teamProgress;

    private final Map<String, Set<Integer>> completedChallenges;

    private BingoProgressManager() {
        this.teamProgress = new HashMap<>();
        this.completedChallenges = new HashMap<>();
    }

    public static BingoProgressManager getInstance() {
        if (instance == null) {
            instance = new BingoProgressManager();
        }
        return instance;
    }

    public void addProgress(String teamName, int challengeId, int amount) {
        teamProgress.computeIfAbsent(teamName, k -> new HashMap<>());

        int currentProgress = teamProgress.get(teamName).getOrDefault(challengeId, 0);
        teamProgress.get(teamName).put(challengeId, currentProgress + amount);
    }

    public void setProgress(String teamName, int challengeId, int amount) {
        teamProgress.computeIfAbsent(teamName, k -> new HashMap<>());
        teamProgress.get(teamName).put(challengeId, amount);
    }

    public int getProgress(String teamName, int challengeId) {
        return teamProgress.getOrDefault(teamName, new HashMap<>())
                .getOrDefault(challengeId, 0);
    }

    public void completeChallenge(String teamName, int challengeId) {
        completedChallenges.computeIfAbsent(teamName, k -> new HashSet<>());
        completedChallenges.get(teamName).add(challengeId);
    }

    public boolean isChallengeCompleted(String teamName, int challengeId) {
        return completedChallenges.getOrDefault(teamName, new HashSet<>())
                .contains(challengeId);
    }

    public Set<Integer> getCompletedChallenges(String teamName) {
        return new HashSet<>(completedChallenges.getOrDefault(teamName, new HashSet<>()));
    }

    public void resetTeamProgress(String teamName) {
        teamProgress.remove(teamName);
        completedChallenges.remove(teamName);
    }

    public void resetAllProgress() {
        teamProgress.clear();
        completedChallenges.clear();
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
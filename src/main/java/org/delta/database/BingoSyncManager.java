package org.delta.database;

import org.delta.database.repositories.BingoChallengeRepository;
import org.delta.database.repositories.BingoProgressRepository;
import org.delta.database.repositories.BingoRoundRepository;
import org.delta.database.repositories.BingoScoreRepository;
import org.delta.managers.bingo.BingoChallenge;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.bingo.BingoScoreManager;
import org.delta.pendulum;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class BingoSyncManager {

    private static BingoSyncManager instance;

    private final Logger log;
    private final BingoRoundRepository roundRepo;
    private final BingoChallengeRepository challengeRepo;
    private final BingoProgressRepository progressRepo;
    private final BingoScoreRepository scoreRepo;

    private long cachedRoundId = -1L;
    private final Map<String, Long> teamIdCache = new ConcurrentHashMap<>();
    private final Map<Integer, Long> challengeIdCache = new ConcurrentHashMap<>();

    private BingoSyncManager(pendulum plugin, DatabaseManager db) {
        this.log = plugin.getLogger();
        this.roundRepo     = new BingoRoundRepository(db);
        this.challengeRepo = new BingoChallengeRepository(db);
        this.progressRepo  = new BingoProgressRepository(db);
        this.scoreRepo     = new BingoScoreRepository(db);

        refreshActiveRound();
    }

    public static BingoSyncManager getInstance(pendulum plugin, DatabaseManager db) {
        if (instance == null) {
            instance = new BingoSyncManager(plugin, db);
        }
        return instance;
    }

    public static BingoSyncManager getInstance() {
        return instance;
    }


    public void onNewBingoTableGenerated(int gridSize, Map<String, BingoChallenge> challenges) {
        roundRepo.createRound(gridSize)
                .thenAccept(roundId -> {
                    if (roundId == -1L) {
                        log.warning("[BingoSync] No se pudo crear la ronda en Supabase");
                        return;
                    }
                    cachedRoundId = roundId;
                    challengeIdCache.clear();
                    log.info("[BingoSync] Ronda " + roundId + " creada en Supabase");

                    challengeRepo.saveAll(roundId, challenges)
                            .thenRun(() -> log.info("[BingoSync] " + challenges.size() + " casillas sincronizadas"))
                            .exceptionally(e -> {
                                log.severe("[BingoSync] Error al sincronizar casillas: " + e.getMessage());
                                return null;
                            });
                })
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al crear ronda: " + e.getMessage());
                    return null;
                });
    }


    public void syncProgress(long teamId, int challengeId, int progress, boolean completed) {
        long roundId = cachedRoundId;
        if (roundId == -1L) {
            log.warning("[BingoSync] syncProgress ignorado — no hay ronda activa en caché");
            return;
        }

        challengeRepo.getChallengeId(roundId, challengeId)
                .thenAccept(dbChallengeId -> {
                    if (dbChallengeId == -1L) {
                        log.warning("[BingoSync] No se encontró challenge en DB: posición " + challengeId);
                        return;
                    }
                    progressRepo.upsertProgress(roundId, teamId, dbChallengeId, progress, completed)
                            .exceptionally(e -> {
                                log.severe("[BingoSync] Error al guardar progreso: " + e.getMessage());
                                return null;
                            });
                })
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al obtener challenge_id: " + e.getMessage());
                    return null;
                });
    }

    public void resetTeamProgress(long teamId) {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        progressRepo.resetTeamProgress(roundId, teamId)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al resetear progreso del equipo: " + e.getMessage());
                    return null;
                });
    }

    public void resetAllProgress() {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        progressRepo.resetAllProgress(roundId)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al resetear todo el progreso: " + e.getMessage());
                    return null;
                });
    }


    public void recordChallengeScore(
            long teamId, int challengeId,
            String description, int points, int position
    ) {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        challengeRepo.getChallengeId(roundId, challengeId)
                .thenAccept(dbChallengeId -> {
                    long idToUse = (dbChallengeId != -1L) ? dbChallengeId : -1L;
                    scoreRepo.recordChallengeScore(
                            roundId, teamId,
                            idToUse != -1L ? idToUse : 0L,
                            description, points, position
                    ).exceptionally(e -> {
                        log.severe("[BingoSync] Error al registrar puntaje de casilla: " + e.getMessage());
                        return null;
                    });
                });
    }

    public void recordLineScore(
            long teamId, String lineType,
            String description, int points, int position
    ) {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        scoreRepo.recordLineScore(roundId, teamId, lineType, description, points, position)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al registrar puntaje de línea: " + e.getMessage());
                    return null;
                });
    }


    public void recordFullBingoScore(long teamId, int points, int position) {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        scoreRepo.recordFullBingoScore(roundId, teamId, points, position)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al registrar full bingo: " + e.getMessage());
                    return null;
                });
    }


    public void resetTeamScores(long teamId) {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        scoreRepo.resetTeamScores(roundId, teamId)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al resetear puntajes del equipo: " + e.getMessage());
                    return null;
                });
    }


    public void resetAllScores() {
        long roundId = cachedRoundId;
        if (roundId == -1L) return;

        scoreRepo.resetAllScores(roundId)
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al resetear todos los puntajes: " + e.getMessage());
                    return null;
                });
    }


    public CompletableFuture<Void> refreshActiveRound() {
        return roundRepo.getActiveRoundId()
                .thenAccept(opt -> {
                    if (opt.isPresent()) {
                        cachedRoundId = opt.get();
                        challengeIdCache.clear();
                        log.info("[BingoSync] Ronda activa cargada: " + cachedRoundId);
                    } else {
                        cachedRoundId = -1L;
                        log.info("[BingoSync] No hay ronda activa en Supabase");
                    }
                })
                .exceptionally(e -> {
                    log.severe("[BingoSync] Error al cargar ronda activa: " + e.getMessage());
                    return null;
                });
    }

    public long resolveTeamId(String teamName) {
        Long cached = teamIdCache.get(teamName);
        if (cached != null) return cached;

        if (pendulum.getInstance().getDatabaseManager() == null || !pendulum.getInstance().getDatabaseManager().isConnected()) {
            return -1L;
        }
        try (Connection conn = pendulum.getInstance().getDatabaseManager().getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT id FROM teams WHERE name = ? LIMIT 1")) {
            stmt.setString(1, teamName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                long id = rs.getLong("id");
                teamIdCache.put(teamName, id);
                return id;
            }
        } catch (Exception e) {
            log.warning("[BingoSync] No se pudo resolver teamId para: " + teamName);
        }
        return -1L;
    }

    public CompletableFuture<Void> resyncAll() {
        long roundId = cachedRoundId;
        if (roundId == -1L) {
            log.warning("[BingoSync] resyncAll ignorado — no hay ronda activa en caché");
            return CompletableFuture.completedFuture(null);
        }

        BingoProgressManager progress = BingoProgressManager.getInstance();
        BingoScoreManager scores = BingoScoreManager.getInstance();
        if (progress == null || scores == null) {
            return CompletableFuture.completedFuture(null);
        }

        List<CompletableFuture<Void>> pending = new ArrayList<>();

        for (String teamName : progress.getAllTeamNames()) {
            long teamId = resolveTeamId(teamName);
            if (teamId == -1L) continue;

            Set<Integer> challengeIds = new HashSet<>();
            challengeIds.addAll(progress.getAllProgress(teamName).keySet());
            challengeIds.addAll(progress.getCompletedChallenges(teamName));

            for (int challengeId : challengeIds) {
                boolean completed = progress.isChallengeCompleted(teamName, challengeId);
                BingoChallenge challenge = BingoDataManager.getInstance().getChallenge(String.valueOf(challengeId));
                int value = (completed && challenge != null)
                        ? challenge.amount()
                        : progress.getProgress(teamName, challengeId);
                pending.add(pushProgress(roundId, teamId, challengeId, value, completed));
            }
        }

        Map<String, List<BingoScoreManager.ScoreEntry>> history = scores.getAllScoreHistory();
        CompletableFuture<Void> scoresDone = scoreRepo.resetAllScores(roundId).thenCompose(v -> {
            List<CompletableFuture<Void>> inserts = new ArrayList<>();
            for (Map.Entry<String, List<BingoScoreManager.ScoreEntry>> entry : history.entrySet()) {
                long teamId = resolveTeamId(entry.getKey());
                if (teamId == -1L) continue;
                for (BingoScoreManager.ScoreEntry score : entry.getValue()) {
                    inserts.add(scoreRepo.insertRawScore(
                            roundId, teamId, score.type(), score.description(),
                            score.points(), score.position()
                    ));
                }
            }
            return CompletableFuture.allOf(inserts.toArray(new CompletableFuture[0]));
        });
        pending.add(scoresDone);

        return CompletableFuture.allOf(pending.toArray(new CompletableFuture[0]))
                .whenComplete((v, e) -> {
                    if (e != null) {
                        log.severe("[BingoSync] Error durante resyncAll: " + e.getMessage());
                    } else {
                        log.info("[BingoSync] Resync completo de la ronda " + roundId);
                    }
                });
    }

    private CompletableFuture<Void> pushProgress(long roundId, long teamId, int challengeId, int progress, boolean completed) {
        return resolveChallengeId(roundId, challengeId)
                .thenCompose(dbChallengeId -> {
                    if (dbChallengeId == -1L) {
                        return CompletableFuture.completedFuture(null);
                    }
                    return progressRepo.upsertProgress(roundId, teamId, dbChallengeId, progress, completed);
                });
    }

    private CompletableFuture<Long> resolveChallengeId(long roundId, int position) {
        Long cached = challengeIdCache.get(position);
        if (cached != null) {
            return CompletableFuture.completedFuture(cached);
        }
        return challengeRepo.getChallengeId(roundId, position)
                .thenApply(dbChallengeId -> {
                    if (dbChallengeId != -1L) {
                        challengeIdCache.put(position, dbChallengeId);
                    }
                    return dbChallengeId;
                });
    }

    public long getCachedRoundId() {
        return cachedRoundId;
    }
}
package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class BingoScoreRepository {

    private final DatabaseManager db;

    public BingoScoreRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> recordChallengeScore(
            long roundId,
            long teamId,
            long challengeId,
            String description,
            int points,
            int position
    ) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return insertScoreEntry(new ScoreEntryData(
                roundId, teamId, "CHALLENGE", description, points, position,
                challengeId, null
        ));
    }

    public CompletableFuture<Void> recordLineScore(
            long roundId,
            long teamId,
            String lineType,
            String description,
            int points,
            int position
    ) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return insertScoreEntry(new ScoreEntryData(
                roundId, teamId, "LINE", description, points, position,
                null, lineType
        ));
    }

    public CompletableFuture<Void> recordFullBingoScore(
            long roundId,
            long teamId,
            int points,
            int position
    ) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return insertScoreEntry(new ScoreEntryData(
                roundId, teamId, "FULL_BINGO", "Bingo Completo", points, position,
                null, null
        ));
    }

    public CompletableFuture<Void> resetTeamScores(long roundId, long teamId) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM bingo_score_entries WHERE round_id = ? AND team_id = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.setLong(2, teamId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al resetear puntajes del equipo: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> resetAllScores(long roundId) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM bingo_score_entries WHERE round_id = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al resetear todos los puntajes: " + e.getMessage(), e);
            }
        });
    }


    private CompletableFuture<Void> insertScoreEntry(ScoreEntryData data) {
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO bingo_score_entries
                    (round_id, team_id, type, description, points, position,
                     challenge_id, line_type, earned_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, now())
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, data.roundId());
                stmt.setLong(2, data.teamId());
                stmt.setString(3, data.type());
                stmt.setString(4, data.description());
                stmt.setInt(5, data.points());
                stmt.setInt(6, data.position());

                if (data.challengeId() != null) {
                    stmt.setLong(7, data.challengeId());
                } else {
                    stmt.setNull(7, Types.BIGINT);
                }

                if (data.lineType() != null) {
                    stmt.setString(8, data.lineType());
                } else {
                    stmt.setNull(8, Types.VARCHAR);
                }

                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al insertar entrada de puntaje: " + e.getMessage(), e);
            }
        });
    }


    public record ScoreEntryData(
            long roundId,
            long teamId,
            String type,
            String description,
            int points,
            int position,
            Long challengeId,
            String lineType
    ) {}
}
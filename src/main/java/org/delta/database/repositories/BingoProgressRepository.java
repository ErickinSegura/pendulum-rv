package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

public class BingoProgressRepository {

    private final DatabaseManager db;

    public BingoProgressRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> upsertProgress(
            long roundId,
            long teamId,
            long challengeId,
            int progress,
            boolean completed
    ) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO bingo_team_progress
                    (round_id, team_id, challenge_id, progress, completed, completed_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, now())
                ON CONFLICT (round_id, team_id, challenge_id) DO UPDATE SET
                    progress     = EXCLUDED.progress,
                    completed    = EXCLUDED.completed,
                    completed_at = CASE
                        WHEN EXCLUDED.completed = true
                         AND bingo_team_progress.completed = false
                        THEN now()
                        ELSE bingo_team_progress.completed_at
                    END,
                    updated_at   = now()
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.setLong(2, teamId);
                stmt.setLong(3, challengeId);
                stmt.setInt(4, progress);
                stmt.setBoolean(5, completed);


                if (completed) {
                    stmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
                } else {
                    stmt.setNull(6, Types.TIMESTAMP);
                }

                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al guardar progreso: " + e.getMessage(), e);
            }
        });
    }


    public CompletableFuture<Void> resetTeamProgress(long roundId, long teamId) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                DELETE FROM bingo_team_progress
                WHERE round_id = ? AND team_id = ?
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.setLong(2, teamId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al resetear progreso del equipo: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> resetAllProgress(long roundId) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM bingo_team_progress WHERE round_id = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException("Error al resetear todo el progreso: " + e.getMessage(), e);
            }
        });
    }

    public record ProgressData(
            long id,
            long roundId,
            long teamId,
            long challengeId,
            int progress,
            boolean completed,
            Timestamp completedAt,
            Timestamp updatedAt
    ) {}
}
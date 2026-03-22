package org.delta.database.repositories;

import org.delta.database.DatabaseManager;
import org.delta.managers.bingo.BingoChallenge;

import java.sql.*;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class BingoChallengeRepository {

    private final DatabaseManager db;

    public BingoChallengeRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> saveAll(long roundId, Map<String, BingoChallenge> challenges) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO bingo_challenges
                    (round_id, position, type, target, amount, display_name, description, icon)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (round_id, position) DO UPDATE SET
                    type         = EXCLUDED.type,
                    target       = EXCLUDED.target,
                    amount       = EXCLUDED.amount,
                    display_name = EXCLUDED.display_name,
                    description  = EXCLUDED.description,
                    icon         = EXCLUDED.icon
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                for (Map.Entry<String, BingoChallenge> entry : challenges.entrySet()) {
                    BingoChallenge c = entry.getValue();

                    stmt.setLong(1, roundId);
                    stmt.setInt(2, c.id());
                    stmt.setString(3, c.type());
                    stmt.setString(4, c.target());
                    stmt.setInt(5, c.amount());
                    stmt.setString(6, c.displayName());
                    stmt.setString(7, c.description());
                    stmt.setString(8, c.icon());
                    stmt.addBatch();
                }

                stmt.executeBatch();

            } catch (SQLException e) {
                throw new RuntimeException("Error al guardar casillas de bingo: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Long> getChallengeId(long roundId, int position) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(-1L);
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id FROM bingo_challenges WHERE round_id = ? AND position = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, roundId);
                stmt.setInt(2, position);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getLong("id");
                }
                return -1L;

            } catch (SQLException e) {
                throw new RuntimeException("Error al obtener challenge_id: " + e.getMessage(), e);
            }
        });
    }
}
package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class BingoRoundRepository {

    private final DatabaseManager db;

    public BingoRoundRepository(DatabaseManager db) {
        this.db = db;
    }


    public CompletableFuture<Long> createRound(int gridSize) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(-1L);
        }
        return CompletableFuture.supplyAsync(() -> {
            try (Connection conn = db.getConnection()) {
                String closeOld = "UPDATE bingo_rounds SET is_active = false, ended_at = now() WHERE is_active = true";
                try (PreparedStatement stmt = conn.prepareStatement(closeOld)) {
                    stmt.executeUpdate();
                }

                String insert = """
                    INSERT INTO bingo_rounds (grid_size, started_at, is_active)
                    VALUES (?, now(), true)
                    RETURNING id
                    """;
                try (PreparedStatement stmt = conn.prepareStatement(insert)) {
                    stmt.setInt(1, gridSize);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        return rs.getLong("id");
                    }
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error al crear ronda de bingo: " + e.getMessage(), e);
            }
            return -1L;
        });
    }

    public CompletableFuture<Optional<Long>> getActiveRoundId() {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT id FROM bingo_rounds WHERE is_active = true LIMIT 1";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return Optional.of(rs.getLong("id"));
                }
                return Optional.empty();

            } catch (SQLException e) {
                throw new RuntimeException("Error al obtener ronda activa: " + e.getMessage(), e);
            }
        });
    }

}
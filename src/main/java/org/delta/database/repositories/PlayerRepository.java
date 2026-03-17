package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository {

    private final DatabaseManager db;

    public PlayerRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> update(UUID uuid, PlayerData data) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                UPDATE players
                SET name  = ?,
                    lives = ?,
                    x     = ?,
                    y     = ?,
                    z     = ?,
                    world = ?
                WHERE uuid = ?
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.name());
                stmt.setInt(2, data.lives());
                setNullableDouble(stmt, 3, data.x());
                setNullableDouble(stmt, 4, data.y());
                setNullableDouble(stmt, 5, data.z());
                stmt.setString(6, data.world());
                stmt.setString(7, uuid.toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void setNullableDouble(PreparedStatement stmt, int index, Double value) throws SQLException {
        if (value != null) {
            stmt.setDouble(index, value);
        } else {
            stmt.setNull(index, Types.NUMERIC);
        }
    }

    public record PlayerData(
            UUID uuid,
            String name,
            int lives,
            Double x,
            Double y,
            Double z,
            String world
    ) {}
}
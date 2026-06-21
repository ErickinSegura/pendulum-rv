package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

    public CompletableFuture<Void> upsert(UUID uuid, PlayerData data) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO players (uuid, name, lives, x, y, z, world)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (uuid) DO UPDATE SET
                    name  = EXCLUDED.name,
                    lives = EXCLUDED.lives,
                    x     = EXCLUDED.x,
                    y     = EXCLUDED.y,
                    z     = EXCLUDED.z,
                    world = EXCLUDED.world
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                stmt.setString(2, data.name());
                stmt.setInt(3, data.lives());
                setNullableDouble(stmt, 4, data.x());
                setNullableDouble(stmt, 5, data.y());
                setNullableDouble(stmt, 6, data.z());
                stmt.setString(7, data.world());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<List<PlayerRow>> obtenerTodos() {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(List.of());
        }
        return CompletableFuture.supplyAsync(() -> {
            List<PlayerRow> result = new ArrayList<>();
            String sql = "SELECT uuid, name FROM players";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    result.add(new PlayerRow(rs.getString("uuid"), rs.getString("name")));
                }

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return result;
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

    public record PlayerRow(String uuid, String name) {}
}
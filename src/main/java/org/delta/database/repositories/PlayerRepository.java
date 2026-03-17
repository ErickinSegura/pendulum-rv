package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerRepository {

    private final DatabaseManager db;

    public PlayerRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Optional<PlayerData>> find(UUID uuid) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(Optional.empty());
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM players WHERE uuid = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    return Optional.of(fromResultSet(rs));
                }
                return Optional.empty();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public CompletableFuture<Void> save(PlayerData data) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO players (uuid, name, lives, x, y, z, world)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (uuid) DO UPDATE
                SET name       = EXCLUDED.name,
                    lives      = EXCLUDED.lives,
                    x          = EXCLUDED.x,
                    y          = EXCLUDED.y,
                    z          = EXCLUDED.z,
                    world      = EXCLUDED.world
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.uuid().toString());
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

    public CompletableFuture<Void> delete(UUID uuid) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = "DELETE FROM players WHERE uuid = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, uuid.toString());
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private PlayerData fromResultSet(ResultSet rs) throws SQLException {
        Double x     = rs.getObject("x")     != null ? rs.getDouble("x")     : null;
        Double y     = rs.getObject("y")     != null ? rs.getDouble("y")     : null;
        Double z     = rs.getObject("z")     != null ? rs.getDouble("z")     : null;
        String world = rs.getString("world") != null ? rs.getString("world") : null;

        return new PlayerData(
                UUID.fromString(rs.getString("uuid")),
                rs.getString("name"),
                rs.getInt("lives"),
                x, y, z, world
        );
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
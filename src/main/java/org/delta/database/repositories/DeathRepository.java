package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Types;
import java.util.concurrent.CompletableFuture;

public class DeathRepository {

    private final DatabaseManager db;

    public DeathRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> recordDeath(DeathData data) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO player_deaths
                    (player_uuid, player_name, cause, death_message,
                     killer_uuid, killer_name,
                     player_team_id, killer_team_id,
                     x, y, z, world, died_at)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, now())
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.playerUuid());
                stmt.setString(2, data.playerName());
                stmt.setString(3, data.cause());
                setNullableString(stmt, 4, data.deathMessage());
                setNullableString(stmt, 5, data.killerUuid());
                setNullableString(stmt, 6, data.killerName());
                setNullableLong(stmt, 7, data.playerTeamId());
                setNullableLong(stmt, 8, data.killerTeamId());
                setNullableDouble(stmt, 9, data.x());
                setNullableDouble(stmt, 10, data.y());
                setNullableDouble(stmt, 11, data.z());
                setNullableString(stmt, 12, data.world());

                stmt.executeUpdate();

            } catch (Exception e) {
                throw new RuntimeException("Error al registrar muerte: " + e.getMessage(), e);
            }
        });
    }

    private void setNullableString(PreparedStatement s, int i, String v) throws Exception {
        if (v != null) s.setString(i, v); else s.setNull(i, Types.VARCHAR);
    }

    private void setNullableLong(PreparedStatement s, int i, Long v) throws Exception {
        if (v != null) s.setLong(i, v); else s.setNull(i, Types.BIGINT);
    }

    private void setNullableDouble(PreparedStatement s, int i, Double v) throws Exception {
        if (v != null) s.setDouble(i, v); else s.setNull(i, Types.NUMERIC);
    }

    public record DeathData(
            String playerUuid,
            String playerName,
            String cause,
            String deathMessage,
            String killerUuid,
            String killerName,
            Long playerTeamId,
            Long killerTeamId,
            Double x,
            Double y,
            Double z,
            String world
    ) {}
}
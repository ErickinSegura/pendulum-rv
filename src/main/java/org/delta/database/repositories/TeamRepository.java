package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class TeamRepository {
    private final DatabaseManager db;

    public TeamRepository(DatabaseManager db) {
        this.db = db;
    }

    public void updatePerk(String teamid, int perkid) {
        if (!db.isConnected()) {
            CompletableFuture.completedFuture(Optional.empty());
            return;
        }
        CompletableFuture.supplyAsync(() -> {
            String sql = "UPDATE teams SET perk = ? WHERE name = ? RETURNING *";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setInt(1, perkid);
                stmt.setString(2, teamid);
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

    public void clearPerk(String teamid) {
        if (!db.isConnected()) return;
        CompletableFuture.runAsync(() -> {
            String sql = "UPDATE teams SET perk = null WHERE name = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, teamid);
                stmt.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private TeamData fromResultSet(ResultSet rs) {
        try {
            return new TeamData(
                    rs.getInt("id"),
                    rs.getString("name"),
                    rs.getInt("perk")
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public record TeamData(
            int id,
            String name,
            int perk
    ) {}
}

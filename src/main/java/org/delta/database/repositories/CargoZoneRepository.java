package org.delta.database.repositories;

import org.delta.database.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.concurrent.CompletableFuture;

public class CargoZoneRepository {

    private final DatabaseManager db;

    public CargoZoneRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Void> recordZone(String name, String dimension, int x, int y, int z) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO cargo_zones (name, dimension, x, y, z, created_at)
                VALUES (?, ?, ?, ?, ?, now())
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, name);
                stmt.setString(2, dimension);
                stmt.setInt(3, x);
                stmt.setInt(4, y);
                stmt.setInt(5, z);

                stmt.executeUpdate();

            } catch (Exception e) {
                throw new RuntimeException("Error al registrar zona de carga: " + e.getMessage(), e);
            }
        });
    }
}

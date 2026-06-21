package org.delta.database.repositories;

import org.bukkit.Material;
import org.delta.database.DatabaseManager;
import org.delta.libs.castigo.Castigo;
import org.delta.libs.castigo.TipoCastigo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class CastigoRepository {

    private final DatabaseManager db;

    public CastigoRepository(DatabaseManager db) {
        this.db = db;
    }

    public Map<UUID, CastigoActivo> cargarActivos() {
        Map<UUID, CastigoActivo> activos = new HashMap<>();
        if (!db.isConnected()) {
            return activos;
        }
        String sql = "SELECT player_uuid, tipo, material, cantidad, aplicado FROM castigos_activos";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                TipoCastigo tipo;
                try {
                    tipo = TipoCastigo.valueOf(rs.getString("tipo"));
                } catch (IllegalArgumentException ex) {
                    continue;
                }

                Material material = null;
                String materialStr = rs.getString("material");
                if (materialStr != null) {
                    try {
                        material = Material.valueOf(materialStr);
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                Castigo castigo = new Castigo(tipo, "", material, rs.getInt("cantidad"));
                activos.put(UUID.fromString(rs.getString("player_uuid")),
                        new CastigoActivo(castigo, rs.getBoolean("aplicado")));
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al cargar castigos activos: " + e.getMessage(), e);
        }
        return activos;
    }

    public CompletableFuture<Void> guardar(String playerUuid, String playerName, Castigo castigo, boolean aplicado) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO castigos_activos
                    (player_uuid, player_name, tipo, material, cantidad, aplicado)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (player_uuid) DO UPDATE SET
                    player_name = EXCLUDED.player_name,
                    tipo        = EXCLUDED.tipo,
                    material    = EXCLUDED.material,
                    cantidad    = EXCLUDED.cantidad,
                    aplicado    = EXCLUDED.aplicado
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid);
                stmt.setString(2, playerName);
                stmt.setString(3, castigo.getTipo().name());
                if (castigo.getMaterial() != null) {
                    stmt.setString(4, castigo.getMaterial().name());
                } else {
                    stmt.setNull(4, Types.VARCHAR);
                }
                stmt.setInt(5, castigo.getCantidad());
                stmt.setBoolean(6, aplicado);

                stmt.executeUpdate();

            } catch (Exception e) {
                throw new RuntimeException("Error al guardar castigo activo: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> marcarAplicado(String playerUuid) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = "UPDATE castigos_activos SET aplicado = true WHERE player_uuid = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, playerUuid);
                stmt.executeUpdate();

            } catch (Exception e) {
                throw new RuntimeException("Error al marcar castigo aplicado: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Void> eliminarTodos() {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement("DELETE FROM castigos_activos")) {
                stmt.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException("Error al eliminar castigos activos: " + e.getMessage(), e);
            }
        });
    }

    public static final class CastigoActivo {
        private final Castigo castigo;
        private boolean aplicado;

        public CastigoActivo(Castigo castigo, boolean aplicado) {
            this.castigo = castigo;
            this.aplicado = aplicado;
        }

        public Castigo getCastigo() {
            return castigo;
        }

        public boolean isAplicado() {
            return aplicado;
        }

        public void setAplicado(boolean aplicado) {
            this.aplicado = aplicado;
        }
    }
}

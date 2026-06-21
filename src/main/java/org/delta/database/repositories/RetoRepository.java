package org.delta.database.repositories;

import org.delta.database.DatabaseManager;
import org.delta.libs.reto.Reto;
import org.delta.libs.reto.RetoItem;
import org.delta.libs.reto.RetoLogro;
import org.delta.libs.reto.RetoMinar;
import org.delta.libs.reto.RetoMobs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.concurrent.CompletableFuture;

public class RetoRepository {

    private final DatabaseManager db;

    public RetoRepository(DatabaseManager db) {
        this.db = db;
    }

    public CompletableFuture<Long> crearReto(RetoData data) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                INSERT INTO retos
                    (titulo, tipo, descripcion, objetivo, cantidad, recompensa, castigo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                RETURNING id
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setString(1, data.titulo());
                stmt.setString(2, data.tipo());
                setNullableString(stmt, 3, data.descripcion());
                setNullableString(stmt, 4, data.objetivo());
                setNullableInt(stmt, 5, data.cantidad());
                setNullableString(stmt, 6, data.recompensa());
                setNullableString(stmt, 7, data.castigo());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getLong("id");
                }
                return null;

            } catch (Exception e) {
                throw new RuntimeException("Error al crear reto en historial: " + e.getMessage(), e);
            }
        });
    }

    public Long ultimoRetoId() {
        if (!db.isConnected()) {
            return null;
        }
        String sql = "SELECT id FROM retos ORDER BY id DESC LIMIT 1";
        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) return rs.getLong("id");
            return null;

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar último reto: " + e.getMessage(), e);
        }
    }

    public CompletableFuture<Void> registrarCompletado(long retoId, String playerUuid, String playerName) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(null);
        }
        return CompletableFuture.runAsync(() -> {
            String sql = """
                INSERT INTO reto_completados
                    (reto_id, player_uuid, player_name, completado_en)
                VALUES (?, ?, ?, now())
                ON CONFLICT (reto_id, player_uuid) DO NOTHING
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, retoId);
                stmt.setString(2, playerUuid);
                stmt.setString(3, playerName);

                stmt.executeUpdate();

            } catch (Exception e) {
                throw new RuntimeException("Error al registrar reto completado: " + e.getMessage(), e);
            }
        });
    }

    public CompletableFuture<Boolean> yaCompleto(long retoId, String playerUuid) {
        if (!db.isConnected()) {
            return CompletableFuture.completedFuture(false);
        }
        return CompletableFuture.supplyAsync(() -> {
            String sql = """
                SELECT 1 FROM reto_completados
                WHERE reto_id = ? AND player_uuid = ?
                LIMIT 1
                """;
            try (Connection conn = db.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {

                stmt.setLong(1, retoId);
                stmt.setString(2, playerUuid);
                try (ResultSet rs = stmt.executeQuery()) {
                    return rs.next();
                }

            } catch (Exception e) {
                throw new RuntimeException("Error al consultar reto completado: " + e.getMessage(), e);
            }
        });
    }

    private void setNullableString(PreparedStatement s, int i, String v) throws Exception {
        if (v != null) s.setString(i, v); else s.setNull(i, Types.VARCHAR);
    }

    private void setNullableInt(PreparedStatement s, int i, Integer v) throws Exception {
        if (v != null) s.setInt(i, v); else s.setNull(i, Types.INTEGER);
    }

    public record RetoData(
            String titulo,
            String tipo,
            String descripcion,
            String objetivo,
            Integer cantidad,
            String recompensa,
            String castigo
    ) {
        public static RetoData from(Reto reto, String recompensa, String castigo) {
            String objetivo = null;
            Integer cantidad = null;

            if (reto instanceof RetoItem item) {
                objetivo = item.getMaterial().name();
                cantidad = item.getCantidad();
            } else if (reto instanceof RetoMobs mobs) {
                objetivo = mobs.getMobType().name();
                cantidad = mobs.getCantidad();
            } else if (reto instanceof RetoMinar minar) {
                objetivo = minar.getMaterial().name();
                cantidad = minar.getCantidad();
            } else if (reto instanceof RetoLogro logro) {
                objetivo = logro.getAdvancementKey();
            }

            return new RetoData(
                    reto.getTitulo(),
                    reto.getTipo().name(),
                    reto.getDescripcion(),
                    objetivo,
                    cantidad,
                    recompensa,
                    castigo
            );
        }
    }
}

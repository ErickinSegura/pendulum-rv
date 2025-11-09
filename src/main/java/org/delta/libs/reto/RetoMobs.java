package org.delta.libs.reto;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class RetoMobs extends Reto {
    private EntityType mobType;
    private int cantidad;
    private String scoreboardName;

    public RetoMobs(String titulo, EntityType mobType, int cantidad) {
        super(titulo, "Mata " + cantidad + " " + mobType.toString().toLowerCase().replace("_", " "), TipoReto.MATAR_MOBS);
        this.mobType = mobType;
        this.cantidad = cantidad;
        this.scoreboardName = "kill_" + mobType.toString().toLowerCase();
    }

    @Override
    public boolean verificarCompletado(Player player) {
        Objective obj = player.getScoreboard().getObjective(scoreboardName);
        if (obj != null) {
            Score score = obj.getScore(player.getName());
            return score.getScore() >= cantidad;
        }
        return false;
    }

    @Override
    public String obtenerProgreso(Player player) {
        Objective obj = player.getScoreboard().getObjective(scoreboardName);
        if (obj != null) {
            int current = obj.getScore(player.getName()).getScore();
            return current + "/" + cantidad;
        }
        return "0/" + cantidad;
    }

    @Override
    public void inicializar(Player player) {
        // Crear objetivo si no existe
        if (player.getScoreboard().getObjective(scoreboardName) == null) {
            player.getScoreboard().registerNewObjective(scoreboardName, "dummy",
                    "Matar " + mobType.toString());
        }
    }

    public EntityType getMobType() {
        return mobType;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getScoreboardName() {
        return scoreboardName;
    }
}
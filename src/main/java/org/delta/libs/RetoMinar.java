package org.delta.libs;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public class RetoMinar extends Reto {
    private Material material;
    private int cantidad;
    private String scoreboardName;

    public RetoMinar(String titulo, Material material, int cantidad) {
        super(titulo, "Mina " + cantidad + " bloques de " + material.toString().toLowerCase().replace("_", " "), TipoReto.MINAR_BLOQUES);
        this.material = material;
        this.cantidad = cantidad;
        this.scoreboardName = "mine_" + material.toString().toLowerCase();
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
        if (player.getScoreboard().getObjective(scoreboardName) == null) {
            player.getScoreboard().registerNewObjective(scoreboardName, "dummy",
                    "Minar " + material.toString());
        }
    }

    public Material getMaterial() {
        return material;
    }

    public int getCantidad() {
        return cantidad;
    }

    public String getScoreboardName() {
        return scoreboardName;
    }
}

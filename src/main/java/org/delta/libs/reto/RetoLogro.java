package org.delta.libs.reto;

import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.entity.Player;

public class RetoLogro extends Reto {
    private String advancementKey;

    public RetoLogro(String titulo, String advancementKey) {
        super(titulo, "Consigue el logro: " + advancementKey, TipoReto.LOGRO);
        this.advancementKey = advancementKey;
    }

    @Override
    public boolean verificarCompletado(Player player) {
        Advancement advancement = Bukkit.getAdvancement(org.bukkit.NamespacedKey.minecraft(advancementKey));
        if (advancement != null) {
            return player.getAdvancementProgress(advancement).isDone();
        }
        return false;
    }

    @Override
    public String obtenerProgreso(Player player) {
        return verificarCompletado(player) ? "Completado" : "Pendiente";
    }

    @Override
    public void inicializar(Player player) {
        // No necesita inicialización
    }

    public String getAdvancementKey() {
        return advancementKey;
    }
}

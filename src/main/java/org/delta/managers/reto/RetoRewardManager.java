package org.delta.managers.reto;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.PendulumSettings;

public class RetoRewardManager {
    private static RetoRewardManager instance;

    private RetoRewardManager() {}

    public static RetoRewardManager getInstance() {
        if (instance == null) {
            instance = new RetoRewardManager();
        }
        return instance;
    }

    public void otorgarRecompensa(Player player) {
        PendulumSettings settings = PendulumSettings.getInstance();
        ItemStack premio = settings.getStackPremio();

        if (premio != null && premio.getType() != Material.AIR) {
            player.getInventory().addItem(premio);
        }
    }

    public String obtenerDescripcionPremio() {
        return PendulumSettings.getInstance().getPremio();
    }

    public String obtenerDescripcionCastigo() {
        return PendulumSettings.getInstance().getCastigo();
    }
}
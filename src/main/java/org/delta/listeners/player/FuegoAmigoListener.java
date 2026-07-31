package org.delta.listeners.player;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.delta.libs.PendulumSettings;
import org.delta.managers.rango.RangoManager;

public class FuegoAmigoListener implements Listener {

    private final RangoManager rangoManager;

    public FuegoAmigoListener(RangoManager rangoManager) {
        this.rangoManager = rangoManager;
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        if (PendulumSettings.getInstance().isFuegoAmigo()) return;
        if (!(event.getEntity() instanceof Player victima)) return;

        Player atacante = resolverAtacante(event.getDamager());
        if (atacante == null || atacante.equals(victima)) return;

        if (rangoManager.mismoEquipo(atacante, victima)) {
            event.setCancelled(true);
        }
    }

    private Player resolverAtacante(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player player) {
            return player;
        }
        return null;
    }
}

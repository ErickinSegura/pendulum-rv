package org.delta.listeners.achievements;

import org.bukkit.entity.Player;
import org.bukkit.entity.PolarBear;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.delta.libs.PendulumSettings;
import org.delta.managers.achievements.Achievement;
import org.delta.managers.achievements.AchievementManager;

public class AchievementListener implements Listener {

    private static final int DIA_TOTEM_RIESGO = 20;

    private final AchievementManager achievements;

    public AchievementListener(AchievementManager achievements) {
        this.achievements = achievements;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onResurrect(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (PendulumSettings.getInstance().getDia() >= DIA_TOTEM_RIESGO) {
            achievements.unlock(player, Achievement.TENTANDO_AL_DESTINO);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPolarBear(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player player && event.getDamager() instanceof PolarBear) {
            achievements.unlock(player, Achievement.DEMASIADO_CERCA);
        }
    }
}

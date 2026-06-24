package org.delta.listeners.chargebase;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.managers.achievements.Achievement;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.managers.chargebase.ChargeBaseSpawnManager;
import org.delta.pendulum;

import java.util.Map;
import java.util.Random;

public class ChargeBaseDeathListener implements Listener {

    private static final double FRAGMENTO_CHANCE = 0.20;
    private static final double UNION_CHANCE = 0.10;
    private static final double HIBRIDO_BASICO_UNION_CHANCE = 0.01;

    private static final Map<String, DropEntry> DROPS = Map.ofEntries(
            Map.entry("atacante_basico",      new DropEntry("fragmento_ataque",   FRAGMENTO_CHANCE)),
            Map.entry("atacante_avanzado",    new DropEntry("union_ataque",       UNION_CHANCE)),
            Map.entry("defensor_basico",      new DropEntry("fragmento_defensa",  FRAGMENTO_CHANCE)),
            Map.entry("defensor_avanzado",    new DropEntry("union_defensa",      UNION_CHANCE)),
            Map.entry("controlador_basico",   new DropEntry("fragmento_control",  FRAGMENTO_CHANCE)),
            Map.entry("controlador_avanzado", new DropEntry("union_control",      UNION_CHANCE)),
            Map.entry("healer_basico",        new DropEntry("fragmento_heal",     FRAGMENTO_CHANCE)),
            Map.entry("healer_avanzado",      new DropEntry("union_heal",         UNION_CHANCE)),
            Map.entry("hibrido_basico",       new DropEntry("union_hibrida",      HIBRIDO_BASICO_UNION_CHANCE)),
            Map.entry("hibrido_avanzado",     new DropEntry("union_hibrida",      UNION_CHANCE))
    );

    private final ChargeBaseManager manager;
    private final Random rng = new Random();

    public ChargeBaseDeathListener(ChargeBaseManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        if (!manager.isActive()) return;
        ChargeBaseSpawnManager spawnManager = manager.getSpawnManager();
        if (spawnManager == null) return;
        if (!spawnManager.isManagedMob(event.getEntity().getUniqueId())) return;

        event.getDrops().clear();
        event.setDroppedExp(0);

        MobClass mobClass = spawnManager.getMobClass(event.getEntity().getUniqueId());
        spawnManager.registerKill(event.getEntity().getUniqueId());

        Player killer = event.getEntity().getKiller();
        if (killer == null || mobClass == null) return;

        var achievements = pendulum.getInstance().getAchievementManager();
        int kills = achievements.addProgress(killer, "cb_kills", 1);
        achievements.unlock(killer, Achievement.REPELIENDO_LA_OLEADA);
        if (kills >= 10) achievements.unlock(killer, Achievement.CAZADOR_DE_OLEADAS);
        if (kills >= 50) achievements.unlock(killer, Achievement.AZOTE_DE_LA_ZONA);

        int roles = achievements.addToSet(killer, "cb_roles", mobClass.name());
        if (roles >= 5) achievements.unlock(killer, Achievement.ESTRATEGA_DE_LA_ZONA);

        for (String tag : event.getEntity().getScoreboardTags()) {
            DropEntry drop = DROPS.get(tag);
            if (drop == null) continue;
            if (rng.nextDouble() <= drop.chance()) {
                ItemRegistry.get(drop.key()).ifPresent(item ->
                        event.getEntity().getWorld().dropItemNaturally(
                                event.getEntity().getLocation(), item.build()
                        )
                );
            }
            break;
        }
    }

    private record DropEntry(String key, double chance) {}
}
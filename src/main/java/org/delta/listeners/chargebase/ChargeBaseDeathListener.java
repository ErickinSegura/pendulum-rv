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

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChargeBaseDeathListener implements Listener {

    private static final Map<MobClass, List<DropEntry>> DROPS = Map.of(
            MobClass.ATACANTE,    List.of(new DropEntry("placeholder", 1), new DropEntry("placeholder", 0.3)),
            MobClass.DEFENSOR,    List.of(new DropEntry("placeholder", 1), new DropEntry("placeholder", 0.3)),
            MobClass.HEALER,      List.of(new DropEntry("placeholder", 1), new DropEntry("placeholder", 0.3)),
            MobClass.CONTROLADOR, List.of(new DropEntry("placeholder", 1), new DropEntry("placeholder", 0.3)),
            MobClass.HIBRIDO,     List.of(new DropEntry("placeholder", 1))
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

        List<DropEntry> drops = DROPS.get(mobClass);
        for (DropEntry drop : drops) {
            if (rng.nextDouble() <= drop.chance()) {
                ItemRegistry.get(drop.key()).ifPresent(item ->
                        event.getEntity().getWorld().dropItemNaturally(
                                event.getEntity().getLocation(), item.build()
                        )
                );
            }
        }
    }

    private record DropEntry(String key, double chance) {}
}
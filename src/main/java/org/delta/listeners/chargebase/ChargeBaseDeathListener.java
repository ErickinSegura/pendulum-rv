package org.delta.listeners.chargebase;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.managers.chargebase.ChargeBaseSpawnManager;

import java.util.List;
import java.util.Map;
import java.util.Random;

public class ChargeBaseDeathListener implements Listener {

    private static final Map<MobClass, List<DropEntry>> DROPS = Map.of(
            MobClass.ATACANTE,    List.of(new DropEntry("nucleo_impulso", 0.4), new DropEntry("garra_energizada", 0.3)),
            MobClass.DEFENSOR,    List.of(new DropEntry("nucleo_proteccion", 0.4), new DropEntry("fragmento_escudo", 0.3)),
            MobClass.HEALER,      List.of(new DropEntry("esencia_vital", 0.4), new DropEntry("nucleo_restauracion", 0.3)),
            MobClass.CONTROLADOR, List.of(new DropEntry("fragmento_temporal", 0.4), new DropEntry("nucleo_distorsion", 0.3)),
            MobClass.HIBRIDO,     List.of(new DropEntry("nucleo_inestable", 0.7))
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

        MobClass mobClass = spawnManager.getMobClass(event.getEntity().getUniqueId());
        spawnManager.registerKill(event.getEntity().getUniqueId());

        Player killer = event.getEntity().getKiller();
        if (killer == null) return;
        if (mobClass == null) return;

        event.getDrops().clear();

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
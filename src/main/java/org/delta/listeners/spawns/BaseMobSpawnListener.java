package org.delta.listeners.spawns;

import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.delta.libs.PendulumSettings;

import java.util.EnumSet;
import java.util.Random;
import java.util.Set;

public abstract class BaseMobSpawnListener implements Listener {

    private static final Set<CreatureSpawnEvent.SpawnReason> PROTECTED_REASONS = EnumSet.of(
            CreatureSpawnEvent.SpawnReason.CUSTOM,
            CreatureSpawnEvent.SpawnReason.COMMAND,
            CreatureSpawnEvent.SpawnReason.SPAWNER_EGG,
            CreatureSpawnEvent.SpawnReason.DISPENSE_EGG,
            CreatureSpawnEvent.SpawnReason.BUILD_WITHER,
            CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN,
            CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM
    );

    protected final PendulumSettings settings = PendulumSettings.getInstance();
    protected final Random random = new Random();

    protected boolean canModify(CreatureSpawnEvent event, int diaMinimo) {
        return settings.getDia() >= diaMinimo
                && !PROTECTED_REASONS.contains(event.getSpawnReason());
    }
}

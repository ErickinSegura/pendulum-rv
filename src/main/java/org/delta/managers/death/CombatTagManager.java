package org.delta.managers.death;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CombatTagManager {

    public static final long TAG_DURATION_MS = 15_000L;

    private final Map<UUID, CombatData> tags = new HashMap<>();

    public boolean isTagged(Player player) {
        CombatData data = tags.get(player.getUniqueId());
        if (data == null) return false;
        if (System.currentTimeMillis() >= data.expiry()) {
            tags.remove(player.getUniqueId());
            return false;
        }
        return true;
    }

    public boolean tag(Player player) {
        return tag(player, null);
    }

    public boolean tag(Player player, Player attacker) {
        boolean wasTagged = isTagged(player);
        CombatData existing = tags.get(player.getUniqueId());

        UUID attackerUuid = attacker != null ? attacker.getUniqueId()
                : (existing != null ? existing.attackerUuid() : null);
        String attackerName = attacker != null ? attacker.getName()
                : (existing != null ? existing.attackerName() : null);

        tags.put(player.getUniqueId(), new CombatData(
                System.currentTimeMillis() + TAG_DURATION_MS, attackerUuid, attackerName));
        return !wasTagged;
    }

    public void clear(Player player) {
        tags.remove(player.getUniqueId());
    }

    public UUID getLastAttackerUuid(Player player) {
        CombatData data = tags.get(player.getUniqueId());
        return data != null ? data.attackerUuid() : null;
    }

    public String getLastAttackerName(Player player) {
        CombatData data = tags.get(player.getUniqueId());
        return data != null ? data.attackerName() : null;
    }

    private record CombatData(long expiry, UUID attackerUuid, String attackerName) {}
}

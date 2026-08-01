package org.delta.managers.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface ServerEvent {

    String getId();

    String getDisplayName();

    void activate(Location anchor);

    void deactivate();

    default boolean restrictsMovement() {
        return false;
    }

    default double getMovementRadius() {
        return 0.0;
    }

    default boolean cancelsDamage() {
        return false;
    }

    default void onPlayerJoin(Player player, Location anchor) {
    }
}

package org.delta.listeners.chargebase;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.delta.libs.MessageUtils;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ChargeBaseZoneListener implements Listener {

    private final ChargeBaseManager manager;
    private final Set<UUID> insideZone = new HashSet<>();

    public ChargeBaseZoneListener(ChargeBaseManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!manager.isActive()) return;
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();
        boolean inside = manager.getActiveZone().isInside(event.getTo());
        boolean wasInside = insideZone.contains(uid);

        if (inside && !wasInside) {
            insideZone.add(uid);
            player.sendMessage(MessageUtils.color("&8[&d&l!&8] &7Entraste a la &d&lBase de Carga&7."));
        } else if (!inside && wasInside) {
            insideZone.remove(uid);
            player.sendMessage(MessageUtils.color("&8[&d&l!&8] &7Saliste de la &d&lBase de Carga&7."));
        }
    }
}
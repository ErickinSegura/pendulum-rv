package org.delta.listeners.player;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;

public class EndAccessListener implements Listener {

    private static final int DIA_APERTURA = 10;

    private final PendulumSettings settings = PendulumSettings.getInstance();

    @EventHandler
    public void onPortal(PlayerPortalEvent event) {
        if (settings.getDia() >= DIA_APERTURA) return;
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.END_PORTAL) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        player.sendMessage(MessageUtils.color("&5&lEl End está sellado hasta el día " + DIA_APERTURA + "."));
        player.playSound(player.getLocation(), Sound.BLOCK_BEACON_DEACTIVATE, 0.7f, 0.8f);
    }
}

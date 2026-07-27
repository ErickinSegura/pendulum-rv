package org.delta.listeners.player;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.delta.libs.MessageUtils;
import org.delta.managers.rango.RangoManager;

public class ChatListener implements Listener {

    private final RangoManager rangoManager;

    public ChatListener(RangoManager rangoManager) {
        this.rangoManager = rangoManager;
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.renderer((source, sourceDisplayName, message, viewer) -> Component.empty()
                .append(rangoManager.getPrefijo(source))
                .append(rangoManager.getNombre(source))
                .append(MessageUtils.color("&7: &f"))
                .append(message));
    }
}

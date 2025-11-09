package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;

public class MessageUtils {

    public static Component color(String message) {
        return LegacyComponentSerializer.legacyAmpersand().deserialize(message);
    }

    public static void sendConsole(String msg) {
        Bukkit.getConsoleSender().sendMessage(MessageUtils.color(msg));
    }
}

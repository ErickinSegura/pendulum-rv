package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class Icons {

    public static final Component ACTIVE_CLOCK = GsonComponentSerializer.gson().deserialize(
            "{\"atlas\":\"minecraft:items\",\"sprite\":\"item/clock_00\"}"
    );

    public static final Component INACTIVE_CLOCK = GsonComponentSerializer.gson().deserialize(
            "{\"atlas\":\"minecraft:items\",\"sprite\":\"item/clock_32\"}"
    );

    private Icons() {}
}
package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;

public class Icons {

    private static final Component[] CLOCK_FRAMES = new Component[64];

    static {
        for (int i = 0; i < 64; i++) {
            CLOCK_FRAMES[i] = GsonComponentSerializer.gson().deserialize(
                    String.format("{\"atlas\":\"minecraft:items\",\"sprite\":\"item/clock_%02d\"}", i)
            );
        }
    }

    public static final Component ACTIVE_CLOCK = CLOCK_FRAMES[0];
    public static final Component INACTIVE_CLOCK = CLOCK_FRAMES[32];


    public static Component getClockFrame(int frame) {
        if (frame < 0 || frame >= 64) {
            throw new IllegalArgumentException("Frame debe estar entre 0 y 63");
        }
        return CLOCK_FRAMES[frame];
    }

    private Icons() {}
}
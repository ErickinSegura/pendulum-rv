package org.delta.libs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import java.util.Random;

public class Icons {

    private static final Component[] CLOCK_FRAMES = new Component[64];
    private static final String[] ITEM_SPRITES = {
            "item/diamond", "item/emerald", "item/gold_ingot", "item/iron_ingot",
            "item/netherite_ingot", "item/diamond_sword", "item/enchanted_book",
            "item/nether_star", "item/totem_of_undying", "item/elytra",
            "item/golden_apple", "item/creeper_spawn_egg", "item/heart_of_the_sea",
            "item/trident", "item/bow", "item/netherite_spear", "item/fox_spawn_egg",
            "item/fishing_rod", "item/shears", "item/flint_and_steel"
    };


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

    public static Component getRandomItem(Random random) {
        String sprite = ITEM_SPRITES[random.nextInt(ITEM_SPRITES.length)];
        return GsonComponentSerializer.gson().deserialize(
                String.format("{\"atlas\":\"minecraft:items\",\"sprite\":\"%s\"}", sprite)
        );
    }

    private Icons() {}
}
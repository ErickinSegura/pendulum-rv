package org.delta.managers.perks;

import org.bukkit.NamespacedKey;

public enum Perk {

    BLOODLUST("bloodlust", "Bloodlust", PerkCategory.BENEFICIAL),
    LAST_STAND("last_stand", "Last Stand", PerkCategory.BENEFICIAL),

    FUMBLE("fumble", "Fumble", PerkCategory.HARMFUL);

    private final String displayName;
    private final PerkCategory category;

    Perk(String key, String displayName, PerkCategory category) {
        this.displayName = displayName;
        this.category = category;
    }

    public String getDisplayName() { return displayName; }
    public PerkCategory getCategory() { return category; }


    public enum PerkCategory {
        BENEFICIAL, HARMFUL
    }
}
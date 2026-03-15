package org.delta.managers.perks;

public enum Perk {

    LAST_STAND("last_stand", "Last Stand", PerkCategory.BENEFICIAL),
    LIFE_STEAL("life_steal", "Life Steal", PerkCategory.BENEFICIAL),

    FUMBLE("fumble", "Fumble", PerkCategory.HARMFUL),
    SHARED_SPACE("shared_space", "Shared Space", PerkCategory.HARMFUL);

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
package org.delta.managers.perks;

public enum Perk {

    LAST_STAND(1, "last_stand", "Last Stand", PerkCategory.BENEFICIAL),
    LIFE_STEAL(2, "life_steal", "Life Steal", PerkCategory.BENEFICIAL),

    FUMBLE(3, "fumble", "Fumble", PerkCategory.HARMFUL),
    SHARED_SPACE(4, "shared_space", "Shared Space", PerkCategory.HARMFUL);

    private final int id;
    private final String displayName;
    private final PerkCategory category;

    Perk(int id, String key, String displayName, PerkCategory category) {
        this.id = id;
        this.displayName = displayName;
        this.category = category;
    }

    public int getId() { return id; }
    public String getDisplayName() { return displayName; }
    public PerkCategory getCategory() { return category; }


    public enum PerkCategory {
        BENEFICIAL, HARMFUL
    }
}
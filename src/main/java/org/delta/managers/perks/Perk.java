package org.delta.managers.perks;

import org.bukkit.NamespacedKey;

public enum Perk {

    BLOODLUST("bloodlust", "Bloodlust", PerkCategory.BENEFICIAL),
    LAST_STAND("last_stand", "Last Stand", PerkCategory.BENEFICIAL),
    ARMOR_PIERCE("armor_pierce", "Armor Pierce", PerkCategory.BENEFICIAL),
    RALLY("rally", "Rally", PerkCategory.BENEFICIAL),
    FORTUNES_FAVOR("fortunes_favor", "Fortune's Favor", PerkCategory.BENEFICIAL),
    MERCHANT("merchant", "Merchant", PerkCategory.BENEFICIAL),
    SCAVENGER("scavenger", "Scavenger", PerkCategory.BENEFICIAL),
    SWIFT_BOOTS("swift_boots", "Swift Boots", PerkCategory.BENEFICIAL),
    FEATHERWEIGHT("featherweight", "Featherweight", PerkCategory.BENEFICIAL),
    RECALL("recall", "Recall", PerkCategory.BENEFICIAL),
    WARDENS_EYE("wardens_eye", "Warden's Eye", PerkCategory.BENEFICIAL),
    DEEP_POCKETS("deep_pockets", "Deep Pockets", PerkCategory.BENEFICIAL),
    BUNKER("bunker", "Bunker", PerkCategory.BENEFICIAL),
    GUARDIAN_ANGEL("guardian_angel", "Guardian Angel", PerkCategory.BENEFICIAL),

    GLASS_CANNON("glass_cannon", "Glass Cannon", PerkCategory.HARMFUL),
    CURSED_ARMOR("cursed_armor", "Cursed Armor", PerkCategory.HARMFUL),
    MARKED("marked", "Marked", PerkCategory.HARMFUL),
    FRAGILE("fragile", "Fragile", PerkCategory.HARMFUL),
    BAD_LUCK("bad_luck", "Bad Luck", PerkCategory.HARMFUL),
    TAXED("taxed", "Taxed", PerkCategory.HARMFUL),
    FUMBLE("fumble", "Fumble", PerkCategory.HARMFUL),
    HEAVY("heavy", "Heavy", PerkCategory.HARMFUL),
    GROUNDED("grounded", "Grounded", PerkCategory.HARMFUL),
    ROOTED("rooted", "Rooted", PerkCategory.HARMFUL),
    SPOTLIGHT("spotlight", "Spotlight", PerkCategory.HARMFUL),
    EXPOSED("exposed", "Exposed", PerkCategory.HARMFUL),
    BOUNTY("bounty", "Bounty", PerkCategory.HARMFUL);

    private final String key;
    private final String displayName;
    private final PerkCategory category;

    Perk(String key, String displayName, PerkCategory category) {
        this.key = key;
        this.displayName = displayName;
        this.category = category;
    }

    public String getKey() { return key; }
    public String getDisplayName() { return displayName; }
    public PerkCategory getCategory() { return category; }

    public enum PerkCategory {
        BENEFICIAL, HARMFUL
    }
}
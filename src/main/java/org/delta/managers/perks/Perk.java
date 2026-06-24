package org.delta.managers.perks;

public enum Perk {

    LAST_STAND(1, "last_stand", "Last Stand", PerkCategory.BENEFICIAL),
    LIFE_STEAL(2, "life_steal", "Life Steal", PerkCategory.BENEFICIAL),
    PIES_LIGEROS(5, "pies_ligeros", "Pies Ligeros", PerkCategory.BENEFICIAL),
    SANACION_COMPARTIDA(6, "sanacion_compartida", "Sanación Compartida", PerkCategory.BENEFICIAL),
    FORJA_EFICIENTE(7, "forja_eficiente", "Forja Eficiente", PerkCategory.BENEFICIAL),
    SALTO_DOBLE(11, "salto_doble", "Salto Doble", PerkCategory.BENEFICIAL),
    POSTURA_FIRME(12, "postura_firme", "Postura Firme", PerkCategory.BENEFICIAL),

    FUMBLE(3, "fumble", "Fumble", PerkCategory.HARMFUL),
    SHARED_SPACE(4, "shared_space", "Shared Space", PerkCategory.HARMFUL),
    HAMBRE_VORAZ(8, "hambre_voraz", "Hambre Voraz", PerkCategory.HARMFUL),
    ECO_VACIO(9, "eco_vacio", "Eco Vacío", PerkCategory.HARMFUL),
    VINCULO_DOLOROSO(10, "vinculo_doloroso", "Vínculo Doloroso", PerkCategory.HARMFUL),
    IMAN_GOLPES(13, "iman_golpes", "Imán de Golpes", PerkCategory.HARMFUL),
    FOTOFOBIA(14, "fotofobia", "Fotofobia", PerkCategory.HARMFUL);

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
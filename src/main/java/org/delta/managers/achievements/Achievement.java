package org.delta.managers.achievements;

import org.bukkit.Material;

import java.util.Arrays;
import java.util.Optional;

public enum Achievement {

    DEMASIADO_CERCA("escandaloso", "Escandaloso",
            "Te alcanza la explosión de un oso polar.",
            Material.POLAR_BEAR_SPAWN_EGG, Frame.GOAL, null, null),

    TENTANDO_AL_DESTINO("tentando_al_destino", "Tentando al Destino",
            "Sobrevive a un tótem cuando ya puede fallar.",
            Material.TOTEM_OF_UNDYING, Frame.CHALLENGE, null, null),

    ULTIMO_ALIENTO("ultimo_aliento", "Último Aliento",
            "Sobrevive gracias al perk Last Stand de tu equipo.",
            Material.SHIELD, Frame.GOAL, null, null),

    SED_DE_SANGRE("sed_de_sangre", "Sed de Sangre",
            "Roba vida con el perk Life Steal de tu equipo.",
            Material.REDSTONE, Frame.GOAL, null, null),

    ALTA_COSTURA("alta_costura", "Alta Costura",
            "Equipa una armadura completa con trims del mismo material y recibe su efecto.",
            Material.NETHERITE_CHESTPLATE, Frame.TASK, null, null),

    EN_EL_OJO_DEL_HURACAN("en_el_ojo_del_huracan", "En el Ojo del Huracán",
            "Entra en la zona de una Base de Carga activa.",
            Material.ECHO_SHARD, Frame.TASK, null, null),

    REPELIENDO_LA_OLEADA("repeliendo_la_oleada", "Repeliendo la Oleada",
            "Derrota a tu primer mob de la Base de Carga.",
            Material.IRON_SWORD, Frame.TASK, "en_el_ojo_del_huracan", null),

    CAZADOR_DE_OLEADAS("cazador_de_oleadas", "Cazador de Oleadas",
            "Derrota 10 mobs de la Base de Carga.",
            Material.NETHERITE_SWORD, Frame.GOAL, "repeliendo_la_oleada", null),

    AZOTE_DE_LA_ZONA("azote_de_la_zona", "Azote de la Zona",
            "Derrota 50 mobs de la Base de Carga.",
            Material.WITHER_SKELETON_SKULL, Frame.CHALLENGE, "cazador_de_oleadas", null),

    ESTRATEGA_DE_LA_ZONA("estratega_de_la_zona", "Estratega de la Zona",
            "Derrota un mob de cada rol de la Base de Carga.",
            Material.TARGET, Frame.CHALLENGE, "azote_de_la_zona", null),

    MODO_FRENESI("modo_frenesi", "Modo Frenesí",
            "Activa el Frenesí.",
            Material.BLAZE_POWDER, Frame.TASK, null, "frenesi"),

    FUEGO_DE_PAPAS("fuego_de_papas", "¡Fuego de Papas!",
            "Dispara el Lanzapapas.",
            Material.CROSSBOW, Frame.CHALLENGE, "modo_frenesi", "lanzapapas"),

    ESTOMAGO_DE_HIERRO("estomago_de_hierro", "Estómago de Hierro",
            "Cómete una Papa Explosiva y vive para contarlo.",
            Material.POTATO, Frame.CHALLENGE, "fuego_de_papas", "papa_explosiva"),

    FURIA_DESATADA("furia_desatada", "Furia Desatada",
            "Acaba con un enemigo mientras el Frenesí está activo.",
            Material.NETHERITE_AXE, Frame.CHALLENGE, "modo_frenesi", "frenesi"),

    GARRAS_FUERA("garras_fuera", "Máximo Alcance",
            "Empuña el Clawn.",
            Material.POPPED_CHORUS_FRUIT, Frame.TASK, null, "clawn"),

    COLUMPIO_URBANO("columpio_urbano", "Spider-Man",
            "Balancéate con el gancho.",
            Material.FISHING_ROD, Frame.CHALLENGE, "garras_fuera", "lazo"),

    TRAS_EL_CRISTAL("tras_el_cristal", "Bañate",
            "Despeja a los enemigos usando la onda expansiva de la varita de barrera.",
            Material.END_ROD, Frame.TASK, null, "varita_barrera"),

    BASTION_DE_CRISTAL("bastion_de_cristal", "Buen bait bro",
            "Atrae a los enemigos cercanos usando el cebo de la varita de barrera mejorada.",
            Material.END_ROD, Frame.CHALLENGE, "tras_el_cristal", "varita_barrera_mejorada"),

    RESERVAS_INFINITAS("reservas_infinitas", "Reservas Infinitas",
            "Sáciate con una Zanahoria Encantada.",
            Material.GOLDEN_CARROT, Frame.TASK, null, "zanahoria_encantada"),

    TOQUE_SANADOR("toque_sanador", "Toque Sanador",
            "Cura a un compañero con la Varita Curativa.",
            Material.GLISTERING_MELON_SLICE, Frame.CHALLENGE, "reservas_infinitas", "varita_curativa"),

    DESPENSA_LLENA("despensa_llena", "Despensa Llena",
            "Rellena una Zanahoria Encantada hasta el máximo de cargas.",
            Material.GOLD_BLOCK, Frame.GOAL, "reservas_infinitas", "zanahoria_encantada"),

    PRIMER_LATIDO("primer_latido", "Dirty",
            "Cómete tu primera Dirty Hearty y gana un corazón permanente.",
            Material.ROTTEN_FLESH, Frame.TASK, null, "dirty_hearty"),

    CORAZON_COMPLETO("corazon_completo", "Hearty",
            "Alcanza los 4 corazones permanentes de Dirty Hearty.",
            Material.ROTTEN_FLESH, Frame.GOAL, "primer_latido", "dirty_hearty"),

    YA_NO_CABE("ya_no_cabe", "Dirtiest",
            "Intenta comer otra Dirty Hearty con los corazones ya al máximo.",
            Material.ROTTEN_FLESH, Frame.CHALLENGE, "corazon_completo", "dirty_hearty"),

    DESAFIO_SUPERADO("desafio_superado", "Fácil, no?",
            "Entrega un reto.",
            Material.PAPER, Frame.TASK, null, null),

    VETERANO_DE_RETOS("veterano_de_retos", "GG EZ Trash",
            "Entrega 5 retos.",
            Material.NETHER_STAR, Frame.GOAL, "desafio_superado", null),

    CASILLA_MARCADA("casilla_marcada", "Cheeeck",
            "Completa una casilla del bingo de tu equipo.",
            Material.MAP, Frame.TASK, null, null),

    MAESTRO_DEL_BINGO("maestro_del_bingo", "Master Cheeeck",
            "Completa 5 casillas del bingo de tu equipo.",
            Material.FILLED_MAP, Frame.GOAL, "casilla_marcada", null),

    PLENO_AL_BINGO("pleno_al_bingo", "Cheeecker",
            "Completa una línea entera: fila, columna o diagonal.",
            Material.EMERALD_BLOCK, Frame.CHALLENGE, "maestro_del_bingo", null),

    MAESTRO_HERRERO("maestro_herrero", "Modificador de Modificadores",
            "Aplica un modificador a un objeto en la mesa de herrería.",
            Material.SMITHING_TABLE, Frame.TASK, null, null),

    A_PRUEBA_DE_TODO("a_prueba_de_todo", "A Prueba de Todo",
            "Aplica el modificador Irrompible a una herramienta o armadura.",
            Material.NETHERITE_CHESTPLATE, Frame.GOAL, "maestro_herrero", "unbreakable_modifier"),

    PESO_PLUMA("peso_pluma", "Peso Pluma",
            "Aplica el modificador Liviano a una pieza de armadura.",
            Material.FEATHER, Frame.GOAL, "maestro_herrero", "liviano_modifier"),

    TEMPLE_DE_ACERO("temple_de_acero", "Temple de Acero",
            "Aplica el modificador Temple a una pieza de armadura.",
            Material.NETHERITE_HELMET, Frame.GOAL, "maestro_herrero", "temple_modifier"),

    GUARDIAN_CAIDO("guardian_caido", "Guardián Caído",
            "Derrota al Guardián de la Forja en su torre.",
            Material.WITHER_SKELETON_SKULL, Frame.CHALLENGE, null, null),

    VIGILIA_ROTA("vigilia_rota", "Vigilia Rota",
            "Derrota al Custodio del Vacío en su santuario flotante.",
            Material.DRAGON_HEAD, Frame.CHALLENGE, null, null);

    public enum Frame {
        TASK("task"),
        GOAL("goal"),
        CHALLENGE("challenge");

        private final String json;

        Frame(String json) {
            this.json = json;
        }

        public String getJson() {
            return json;
        }
    }

    private final String id;
    private final String titulo;
    private final String descripcion;
    private final Material icono;
    private final Frame frame;
    private final String parentId;
    private final String itemKey;

    Achievement(String id, String titulo, String descripcion, Material icono, Frame frame,
                String parentId, String itemKey) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.icono = icono;
        this.frame = frame;
        this.parentId = parentId;
        this.itemKey = itemKey;
    }

    public String getId() {
        return id;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public Material getIcono() {
        return icono;
    }

    public Frame getFrame() {
        return frame;
    }

    public String getParentId() {
        return parentId;
    }

    public String getItemKey() {
        return itemKey;
    }

    public static Optional<Achievement> fromId(String id) {
        return Arrays.stream(values())
                .filter(a -> a.id.equalsIgnoreCase(id))
                .findFirst();
    }
}

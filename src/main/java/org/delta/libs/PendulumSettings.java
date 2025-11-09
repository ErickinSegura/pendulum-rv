package org.delta.libs;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.delta.pendulum;

import java.io.File;
import java.util.List;
import java.util.Map;

public class PendulumSettings {
    private static final PendulumSettings instance = new PendulumSettings();

    private String[] castigosDia0, op;
    private String premio, castigo;
    private int dia;
    private int jugadoresNoche;
    private ItemStack stackPremio;
    private String[] castigos;
    private Reto retoActual;
    private Reto[] retosDisponibles;

    private PendulumSettings() {}

    public void load() {
        File file = new File(pendulum.getInstance().getDataFolder(), "settings.yml");

        if (!file.exists()) {
            System.out.println("[Pendulum Debug] settings.yml no existe, creando uno nuevo...");
            pendulum.getInstance().saveResource("settings.yml", false);
        }

        YamlConfiguration config = new YamlConfiguration();

        try {
            config.load(file);
            System.out.println("[Pendulum Debug] Archivo cargado correctamente");
        } catch (Exception e) {
            System.out.println("[Pendulum Debug] Error al cargar el archivo:");
            e.printStackTrace();
        }

        // Cargar retos disponibles
        List<Map<?, ?>> retosConfig = config.getMapList("reto.retos");
        System.out.println("[Pendulum Debug] Cantidad de retos encontrados: " + retosConfig.size());
        retosDisponibles = new Reto[retosConfig.size()];

        for (int i = 0; i < retosConfig.size(); i++) {
            Map<?, ?> retoMap = retosConfig.get(i);
            String tipo = (String) retoMap.get("tipo");
            String titulo = (String) retoMap.get("titulo");

            switch (tipo.toUpperCase()) {
                case "ITEM":
                    int cantidad = (int) retoMap.get("cantidad");
                    Material material = Material.valueOf((String) retoMap.get("material"));
                    retosDisponibles[i] = new RetoItem(titulo, material, cantidad);
                    break;

                case "LOGRO":
                    String logro = (String) retoMap.get("logro");
                    retosDisponibles[i] = new RetoLogro(titulo, logro);
                    break;

                case "MATAR_MOBS":
                    int cantidadMobs = (int) retoMap.get("cantidad");
                    EntityType mob = EntityType.valueOf((String) retoMap.get("mob"));
                    retosDisponibles[i] = new RetoMobs(titulo, mob, cantidadMobs);
                    break;

                case "MINAR_BLOQUES":
                    int cantidadBloques = (int) retoMap.get("cantidad");
                    Material bloque = Material.valueOf((String) retoMap.get("material"));
                    retosDisponibles[i] = new RetoMinar(titulo, bloque, cantidadBloques);
                    break;

                default:
                    System.out.println("[Pendulum Debug] Tipo de reto desconocido: " + tipo);
            }
        }

        // Cargar reto actual (índice en el config)
        int indiceRetoActual = config.getInt("reto.retoActualIndex", 0);
        if (indiceRetoActual < retosDisponibles.length) {
            retoActual = retosDisponibles[indiceRetoActual];
        }

        // Cargar castigos
        castigos = config.getStringList("reto.castigos").toArray(new String[0]);
        castigosDia0 = config.getStringList("reto.castigos.dia0").toArray(new String[0]);

        // Cargar resto de configuraciones
        premio = config.getString("reto.premio");
        castigo = config.getString("reto.castigo");
        dia = config.getInt("mundo.dia");

        int cantidadPremio = config.getInt("reto.cantidadPremio");
        String materialPremioString = config.getString("reto.materialPremio");
        if (materialPremioString != null) {
            try {
                stackPremio = new ItemStack(Material.valueOf(materialPremioString), cantidadPremio);
            } catch (IllegalArgumentException e) {
                stackPremio = new ItemStack(Material.AIR, cantidadPremio);
            }
        }

        op = config.getStringList("permisos").toArray(new String[0]);
        jugadoresNoche = config.getInt("mundo.jugadoresNoche");

        System.out.println("[Pendulum Debug] Carga completada:");
        System.out.println("- Retos disponibles: " + (retosDisponibles != null ? retosDisponibles.length : 0));
        System.out.println("- Reto actual: " + (retoActual != null ? retoActual.getTitulo() : "ninguno"));
    }

    // Getters
    public Reto getRetoActual() {
        return retoActual;
    }

    public Reto[] getRetosDisponibles() {
        return retosDisponibles;
    }

    public String getPremio() {
        return premio;
    }

    public String getCastigo() {
        return castigo;
    }

    public ItemStack getStackPremio() {
        return stackPremio;
    }

    public String[] getCastigos() {
        return castigos;
    }

    public String[] getCastigosDia0() {
        return castigosDia0;
    }

    public String[] getOp() {
        return op;
    }

    public int getDia() {
        return dia;
    }

    public int getJugadoresNoche() {
        return jugadoresNoche;
    }

    public static PendulumSettings getInstance() {
        return instance;
    }
}

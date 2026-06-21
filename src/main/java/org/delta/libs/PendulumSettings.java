package org.delta.libs;

import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.castigo.Castigo;
import org.delta.libs.castigo.TipoCastigo;
import org.delta.libs.reto.*;
import org.delta.pendulum;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PendulumSettings {
    private static final PendulumSettings instance = new PendulumSettings();

    private String[] op;
    private String premio;
    private Castigo castigoActual;
    private Castigo[] castigos;
    private int dia;
    private int jugadoresNoche;
    private ItemStack stackPremio;
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

        List<Map<?, ?>> castigosConfig = config.getMapList("reto.castigos");
        System.out.println("[Pendulum Debug] Cantidad de castigos encontrados: " + castigosConfig.size());
        List<Castigo> listaCastigos = new ArrayList<>();
        for (Map<?, ?> castigoMap : castigosConfig) {
            Castigo castigo = parsearCastigo(castigoMap);
            if (castigo != null) listaCastigos.add(castigo);
        }
        castigos = listaCastigos.toArray(new Castigo[0]);

        int indiceRetoActual = config.getInt("reto.retoActualIndex", 0);
        if (indiceRetoActual < retosDisponibles.length) {
            retoActual = retosDisponibles[indiceRetoActual];
        }

        int indiceCastigoActual = config.getInt("reto.castigoActualIndex", 0);
        if (castigos.length > 0 && indiceCastigoActual >= 0 && indiceCastigoActual < castigos.length) {
            castigoActual = castigos[indiceCastigoActual];
        } else {
            castigoActual = null;
        }

        premio = config.getString("reto.premio");

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
        dia = config.getInt("mundo.dia");

        System.out.println("[Pendulum Debug] Carga completada:");
        System.out.println("- Retos disponibles: " + (retosDisponibles != null ? retosDisponibles.length : 0));
        System.out.println("- Reto actual: " + (retoActual != null ? retoActual.getTitulo() : "ninguno"));
        System.out.println("- Castigos disponibles: " + (castigos != null ? castigos.length : 0));
        System.out.println("- Castigo actual: " + getCastigo());
    }

    private Castigo parsearCastigo(Map<?, ?> map) {
        String tipoStr = (String) map.get("tipo");
        if (tipoStr == null) return null;

        TipoCastigo tipo;
        try {
            tipo = TipoCastigo.valueOf(tipoStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            System.out.println("[Pendulum Debug] Tipo de castigo desconocido: " + tipoStr);
            return null;
        }

        String descripcion = (String) map.get("descripcion");

        Material material = null;
        if (map.get("material") != null) {
            try {
                material = Material.valueOf((String) map.get("material"));
            } catch (IllegalArgumentException e) {
                material = null;
            }
        }

        int cantidad = map.get("cantidad") != null ? (int) map.get("cantidad") : 0;

        return new Castigo(tipo, descripcion != null ? descripcion : "", material, cantidad);
    }

    public Reto getRetoActual() {
        return retoActual;
    }

    public Reto[] getRetosDisponibles() {
        return retosDisponibles;
    }

    public int getJugadoresNoche() { return jugadoresNoche; }

    public String getPremio() {
        return premio;
    }

    public String getCastigo() {
        return castigoActual != null ? castigoActual.getDescripcion() : "Sin castigo definido";
    }

    public Castigo getCastigoActual() {
        return castigoActual;
    }

    public Castigo[] getCastigosDisponibles() {
        return castigos;
    }

    public ItemStack getStackPremio() {
        return stackPremio;
    }

    public String[] getOp() {
        return op;
    }

    public int getDia() {
        return dia;
    }

    public static PendulumSettings getInstance() {
        return instance;
    }
}
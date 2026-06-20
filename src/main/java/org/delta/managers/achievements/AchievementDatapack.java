package org.delta.managers.achievements;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.delta.customs.items.ItemRegistry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Optional;

public class AchievementDatapack {

    private static final String CRITERIO = "completar";
    private static final String PACK_ID = "pendulum_logros";
    private static final String NAMESPACE = "pendulum";
    private static final int VERSION = 5;
    private static final String FONDO = "minecraft:block/gold_block";

    private final Plugin plugin;

    public AchievementDatapack(Plugin plugin) {
        this.plugin = plugin;
    }

    public NamespacedKey getKey(Achievement achievement) {
        return new NamespacedKey(NAMESPACE, achievement.getId());
    }

    public boolean install() {
        if (Bukkit.getWorlds().isEmpty()) {
            plugin.getLogger().warning("[Logros] No hay mundos cargados; no se pudo instalar el datapack.");
            return false;
        }

        File datapacks = new File(Bukkit.getWorlds().get(0).getWorldFolder(), "datapacks");
        File pack = new File(datapacks, PACK_ID);
        File version = new File(pack, ".pendulum_version");

        if (pack.exists() && version.exists() && leerVersion(version) == VERSION) {
            return false;
        }

        try {
            if (pack.exists()) borrarRecursivo(pack);

            File advancementDir = new File(pack, "data/" + NAMESPACE + "/advancement");
            if (!advancementDir.exists() && !advancementDir.mkdirs()) {
                throw new IOException("No se pudo crear " + advancementDir);
            }

            escribir(new File(pack, "pack.mcmeta"), packMeta());
            escribir(new File(advancementDir, "root.json"), rootJson());
            for (Achievement achievement : Achievement.values()) {
                escribir(new File(advancementDir, achievement.getId() + ".json"), childJson(achievement));
            }
            escribir(version, String.valueOf(VERSION));

            plugin.getLogger().info("[Logros] Datapack '" + PACK_ID + "' instalado/actualizado en "
                    + pack.getPath());
            plugin.getLogger().info("[Logros] Reinicia el servidor (o ejecuta /minecraft:reload) "
                    + "una vez para activar los logros nativos.");
            return true;
        } catch (IOException e) {
            plugin.getLogger().warning("[Logros] Error escribiendo el datapack: " + e.getMessage());
            return false;
        }
    }

    public void award(Player player, Achievement achievement) {
        Advancement advancement = Bukkit.getAdvancement(getKey(achievement));
        if (advancement == null) return;

        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (!progress.isDone()) {
            for (String criterion : progress.getRemainingCriteria()) {
                progress.awardCriteria(criterion);
            }
        }
    }

    private int leerVersion(File file) {
        try {
            return Integer.parseInt(Files.readString(file.toPath(), StandardCharsets.UTF_8).trim());
        } catch (Exception e) {
            return -1;
        }
    }

    private void escribir(File file, String contenido) throws IOException {
        Files.writeString(file.toPath(), contenido, StandardCharsets.UTF_8);
    }

    private void borrarRecursivo(File file) {
        File[] hijos = file.listFiles();
        if (hijos != null) {
            for (File hijo : hijos) borrarRecursivo(hijo);
        }
        file.delete();
    }

    private String packMeta() {
        int[] formato = detectarFormato();
        if (formato != null) {
            int major = formato[0];
            int minor = formato[1];
            return "{"
                    + "\"pack\":{"
                    + "\"description\":\"Logros de Pendulum\","
                    + "\"min_format\":[" + major + ",0],"
                    + "\"max_format\":[" + major + "," + minor + "]"
                    + "}}";
        }
        return "{"
                + "\"pack\":{"
                + "\"pack_format\":81,"
                + "\"description\":\"Logros de Pendulum\""
                + "}}";
    }

    private int[] detectarFormato() {
        try {
            net.minecraft.server.packs.metadata.pack.PackFormat formato =
                    net.minecraft.SharedConstants.getCurrentVersion()
                            .packVersion(net.minecraft.server.packs.PackType.SERVER_DATA);
            return new int[]{formato.major(), formato.minor()};
        } catch (Throwable t) {
            plugin.getLogger().warning("[Logros] No se pudo detectar el formato de datapack: " + t.getMessage());
            return null;
        }
    }

    private String rootJson() {
        return "{"
                + "\"display\":{"
                + "\"icon\":{\"id\":\"minecraft:clock\"},"
                + "\"title\":\"Pendulum\","
                + "\"description\":\"Logros del servidor\","
                + "\"frame\":\"task\","
                + "\"show_toast\":false,"
                + "\"announce_to_chat\":false,"
                + "\"background\":\"" + FONDO + "\""
                + "},"
                + "\"criteria\":{\"" + CRITERIO + "\":{\"trigger\":\"minecraft:impossible\"}}"
                + "}";
    }

    private String childJson(Achievement achievement) {
        String parentKey = achievement.getParentId() != null
                ? NAMESPACE + ":" + achievement.getParentId()
                : NAMESPACE + ":root";
        return "{"
                + "\"parent\":\"" + parentKey + "\","
                + "\"display\":{"
                + "\"icon\":" + iconJson(achievement) + ","
                + "\"title\":\"" + escapar(achievement.getTitulo()) + "\","
                + "\"description\":\"" + escapar(achievement.getDescripcion()) + "\","
                + "\"frame\":\"" + achievement.getFrame().getJson() + "\","
                + "\"show_toast\":true,"
                + "\"announce_to_chat\":true,"
                + "\"hidden\":false"
                + "},"
                + "\"criteria\":{\"" + CRITERIO + "\":{\"trigger\":\"minecraft:impossible\"}},"
                + "\"requirements\":[[\"" + CRITERIO + "\"]]"
                + "}";
    }

    private String iconJson(Achievement achievement) {
        String matId = achievement.getIcono().getKey().getKey();
        Integer customModelData = null;

        if (achievement.getItemKey() != null) {
            Optional<org.delta.customs.items.CustomItem> item = ItemRegistry.get(achievement.getItemKey());
            if (item.isPresent()) {
                ItemStack stack = item.get().build();
                matId = stack.getType().getKey().getKey();
                ItemMeta meta = stack.getItemMeta();
                if (meta != null && meta.hasCustomModelData()) {
                    customModelData = meta.getCustomModelData();
                }
            }
        }

        String json = "{\"id\":\"minecraft:" + matId + "\"";
        if (customModelData != null) {
            json += ",\"components\":{\"minecraft:custom_model_data\":{\"floats\":[" + customModelData + ".0]}}";
        }
        return json + "}";
    }

    private String escapar(String texto) {
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}

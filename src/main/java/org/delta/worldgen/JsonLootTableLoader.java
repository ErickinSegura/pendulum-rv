package org.delta.worldgen;

import org.bukkit.Material;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class JsonLootTableLoader {

    private final File folder;
    private final Logger logger;
    private final Map<String, LootTable> loaded = new HashMap<>();

    public JsonLootTableLoader(File pluginDataFolder, Logger logger) {
        this.folder = new File(pluginDataFolder, "loottables");
        this.logger = logger;
        if (!folder.exists()) folder.mkdirs();
    }

    public void loadAll() {
        loaded.clear();
        File[] files = folder.listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return;
        for (File f : files) {
            LootTable lt = parse(f);
            if (lt != null) {
                loaded.put(lt.getId(), lt);
                logger.info("[JsonLootTableLoader] Cargada: " + lt.getId());
            }
        }
    }

    public LootTable get(String id) {
        return loaded.get(id);
    }

    private LootTable parse(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            String content = sb.toString();

            String id       = extractString(content, "id");
            int rollsMin    = extractInt(content, "rolls_min", 3);
            int rollsMax    = extractInt(content, "rolls_max", 7);

            LootTable.Builder b = new LootTable.Builder(id).rolls(rollsMin, rollsMax);

            // Parsear entries
            int arrStart = content.indexOf("\"entries\"");
            arrStart = content.indexOf("[", arrStart);
            int arrEnd = content.indexOf("]", arrStart);
            String arr = content.substring(arrStart + 1, arrEnd);

            for (String obj : arr.split("\\},\\s*\\{")) {
                obj = obj.replace("{", "").replace("}", "").trim();
                if (obj.isEmpty()) continue;

                String matStr = extractString(obj, "material");
                int min       = extractInt(obj, "min", 1);
                int max       = extractInt(obj, "max", 1);
                int weight    = extractInt(obj, "weight", 10);

                try {
                    Material mat = Material.valueOf(matStr);
                    b.entry(mat, min, max, weight);
                } catch (IllegalArgumentException e) {
                    logger.warning("[JsonLootTableLoader] Material desconocido: " + matStr);
                }
            }

            return b.build();

        } catch (Exception e) {
            logger.severe("[JsonLootTableLoader] Error leyendo " + file.getName() + ": " + e.getMessage());
            return null;
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de parsing sin dependencias externas
    // -------------------------------------------------------------------------

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx);
        int q1 = json.indexOf("\"", colon + 1);
        int q2 = json.indexOf("\"", q1 + 1);
        return json.substring(q1 + 1, q2);
    }

    private static int extractInt(String json, String key, int def) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return def;
        int colon = json.indexOf(":", idx);
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-')) end++;
        try { return Integer.parseInt(json.substring(start, end)); }
        catch (NumberFormatException e) { return def; }
    }

    private static double extractDouble(String json, String key, double def) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return def;
        int colon = json.indexOf(":", idx);
        int start = colon + 1;
        while (start < json.length() && (json.charAt(start) == ' ' || json.charAt(start) == '\n')) start++;
        int end = start;
        while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '-' || json.charAt(end) == '.')) end++;
        try { return Double.parseDouble(json.substring(start, end)); }
        catch (NumberFormatException e) { return def; }
    }
}
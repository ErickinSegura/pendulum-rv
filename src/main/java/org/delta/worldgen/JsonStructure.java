package org.delta.worldgen;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Biome;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import net.kyori.adventure.text.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JsonStructure extends StructureTemplate {

    private final File file;
    private final Logger logger;
    private final JsonLootTableLoader lootLoader;

    // Override opcionales desde código (si no están en el JSON se ignoran)
    private StructureDef.Rotation rotation = StructureDef.Rotation.ROT_0;

    public JsonStructure(File file, Logger logger, JsonLootTableLoader lootLoader) {
        this.file       = file;
        this.logger     = logger;
        this.lootLoader = lootLoader;
    }

    public JsonStructure rotation(StructureDef.Rotation r) { this.rotation = r; return this; }

    @Override
    public StructureDef build() {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
            String content = sb.toString();

            String id         = extractString(content, "id");
            double chance     = extractDouble(content, "spawnChance", 0.015);
            String modeStr    = extractString(content, "spawnMode");
            int minClearance  = extractInt(content, "minClearance", 10);
            int minAirY       = extractInt(content, "minAirY", 80);
            int maxAirY       = extractInt(content, "maxAirY", 180);

            StructureDef.Builder b = new StructureDef.Builder(id + "_" + rotation.name())
                    .spawnChance(chance)
                    .rotation(rotation);

            // Biomes
            List<String> biomeStrs = extractStringArray(content, "biomes");
            List<Biome> biomes = new ArrayList<>();
            for (String bs : biomeStrs) {
                try {
                    NamespacedKey key = NamespacedKey.minecraft(bs.toLowerCase());
                    Biome biome = Bukkit.getServer().getRegistry(Biome.class).get(key);
                    if (biome != null) biomes.add(biome);
                    else logger.warning("[JsonStructure] Bioma desconocido: " + bs);
                } catch (Exception e) {
                    logger.warning("[JsonStructure] Bioma desconocido: " + bs);
                }
            }
            if (!biomes.isEmpty()) b.biomes(biomes.toArray(new Biome[0]));

            // SpawnMode
            if ("AIR".equalsIgnoreCase(modeStr)) {
                b.airSpawn(minClearance, minAirY, maxAirY);
            }

            // Blocks
            String blocksArr = extractArray(content, "blocks");
            for (String obj : splitObjects(blocksArr)) {
                int x         = extractInt(obj, "x", 0);
                int y         = extractInt(obj, "y", 0);
                int z         = extractInt(obj, "z", 0);
                String matStr = extractString(obj, "m");
                String lootId = extractString(obj, "loot");

                Material mat = null;
                try { mat = Material.valueOf(matStr); }
                catch (IllegalArgumentException e) {
                    logger.warning("[JsonStructure] Material desconocido: " + matStr);
                    continue;
                }

                if (!lootId.isEmpty()) {
                    LootTable loot = lootLoader.get(lootId);
                    if (loot != null) b.chest(x, y, z, mat, loot);
                    else {
                        logger.warning("[JsonStructure] LootTable no encontrada: " + lootId);
                        b.block(x, y, z, mat);
                    }
                } else {
                    b.block(x, y, z, mat);
                }
            }

            // Entities
            String entitiesArr = extractArray(content, "entities");
            if (!entitiesArr.isEmpty()) {
                for (String obj : splitObjects(entitiesArr)) {
                    int x          = extractInt(obj, "x", 0);
                    int y          = extractInt(obj, "y", 0);
                    int z          = extractInt(obj, "z", 0);
                    String typeStr = extractString(obj, "type");
                    String name    = extractString(obj, "name");
                    double health  = extractDouble(obj, "health", -1);

                    // Equipment
                    String handStr   = extractString(obj, "hand");
                    String helmetStr = extractString(obj, "helmet");

                    EntityType type = null;
                    try { type = EntityType.valueOf(typeStr); }
                    catch (IllegalArgumentException e) {
                        logger.warning("[JsonStructure] EntityType desconocido: " + typeStr);
                        continue;
                    }

                    final String   finalName   = name;
                    final double   finalHealth = health;
                    final Material finalHand   = parseMaterialOrNull(handStr);
                    final Material finalHelmet = parseMaterialOrNull(helmetStr);
                    final EntityType finalType = type;

                    b.entity(x, y, z, finalType, entity -> {
                        if (!finalName.isEmpty()) {
                            entity.customName(Component.text(
                                    finalName.replace("&", "§")));
                            entity.setCustomNameVisible(true);
                        }
                        if (entity instanceof org.bukkit.entity.LivingEntity living) {
                            if (finalHealth > 0) {
                                var maxHp = living.getAttribute(
                                        org.bukkit.attribute.Attribute.MAX_HEALTH);
                                if (maxHp != null) maxHp.setBaseValue(finalHealth);
                                living.setHealth(finalHealth);
                            }
                            if (finalHand != null)
                                living.getEquipment().setItemInMainHand(
                                        new ItemStack(finalHand));
                            if (finalHelmet != null)
                                living.getEquipment().setHelmet(
                                        new ItemStack(finalHelmet));
                        }
                        if (entity instanceof org.bukkit.entity.Zombie zombie) {
                            zombie.setShouldBurnInDay(false);
                        }
                    });
                }
            }

            return b.build();

        } catch (Exception e) {
            logger.severe("[JsonStructure] Error cargando " + file.getName() + ": " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers de parsing
    // -------------------------------------------------------------------------

    private Material parseMaterialOrNull(String s) {
        if (s == null || s.isEmpty()) return null;
        try { return Material.valueOf(s); }
        catch (IllegalArgumentException e) { return null; }
    }

    private static String extractArray(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return "";
        int start = json.indexOf("[", idx);
        if (start < 0) return "";
        int depth = 0, end = start;
        while (end < json.length()) {
            char c = json.charAt(end);
            if (c == '[') depth++;
            else if (c == ']') { depth--; if (depth == 0) break; }
            end++;
        }
        return json.substring(start + 1, end);
    }

    private static List<String> splitObjects(String arr) {
        List<String> result = new ArrayList<>();
        int depth = 0, start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') { if (depth == 0) start = i; depth++; }
            else if (c == '}') { depth--; if (depth == 0 && start >= 0) result.add(arr.substring(start + 1, i)); }
        }
        return result;
    }

    private static List<String> extractStringArray(String json, String key) {
        List<String> result = new ArrayList<>();
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return result;
        int start = json.indexOf("[", idx);
        int end   = json.indexOf("]", start);
        String arr = json.substring(start + 1, end);
        for (String part : arr.split(",")) {
            String s = part.trim().replace("\"", "");
            if (!s.isEmpty()) result.add(s);
        }
        return result;
    }

    private static String extractString(String json, String key) {
        String search = "\"" + key + "\"";
        int idx = json.indexOf(search);
        if (idx < 0) return "";
        int colon = json.indexOf(":", idx);
        int q1    = json.indexOf("\"", colon + 1);
        if (q1 < 0) return "";
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
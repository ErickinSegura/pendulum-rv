package org.delta.managers.canje;

import org.bukkit.Bukkit;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.block.banner.Pattern;
import org.bukkit.block.banner.PatternType;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BannerMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionType;
import org.delta.libs.builders.ItemBuilder;
import org.delta.pendulum;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CanjeManager {

    private static CanjeManager instance;

    private final pendulum plugin;
    private final NamespacedKey redeemedKey;
    private final Map<String, Canje> canjes = new LinkedHashMap<>();
    private final List<Material> discos = new ArrayList<>();

    private boolean registrarCanjes;

    private CanjeManager(pendulum plugin) {
        this.plugin = plugin;
        this.redeemedKey = new NamespacedKey(plugin, "canjes_redimidos");
        for (Material material : Material.values()) {
            if (material.name().startsWith("MUSIC_DISC_")) {
                discos.add(material);
            }
        }
    }

    public static CanjeManager getInstance() {
        return instance;
    }

    public static void initialize(pendulum plugin) {
        if (instance == null) {
            instance = new CanjeManager(plugin);
        }
        instance.load();
    }

    public void load() {
        canjes.clear();

        File file = new File(plugin.getDataFolder(), "canjes.yml");
        if (!file.exists()) {
            plugin.saveResource("canjes.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        registrarCanjes = config.getBoolean("registrar-canjes", true);

        ConfigurationSection root = config.getConfigurationSection("canjes");
        if (root == null) {
            plugin.getLogger().warning("[Canjes] No se encontró la sección 'canjes' en canjes.yml");
            return;
        }

        for (String codigo : root.getKeys(false)) {
            ConfigurationSection section = root.getConfigurationSection(codigo);
            if (section == null) continue;

            String descripcion = section.getString("descripcion", "");
            List<ItemStack> items = new ArrayList<>();

            for (Map<?, ?> raw : section.getMapList("items")) {
                ItemStack item = parseItem(raw);
                if (item != null) items.add(item);
            }

            if (items.isEmpty()) {
                plugin.getLogger().warning("[Canjes] El código '" + codigo + "' no tiene recompensas válidas y será ignorado.");
                continue;
            }

            canjes.put(normalizar(codigo), new Canje(codigo, descripcion, items));
        }

        plugin.getLogger().info("[Canjes] Cargados " + canjes.size() + " códigos de canje.");
    }

    public Canje buscar(String codigo) {
        return canjes.get(normalizar(codigo));
    }

    public boolean yaCanjeado(Player player, String codigoNormalizado) {
        for (String redimido : leerRedimidos(player)) {
            if (redimido.equals(codigoNormalizado)) return true;
        }
        return false;
    }

    public void marcarCanjeado(Player player, String codigoNormalizado) {
        List<String> redimidos = leerRedimidos(player);
        if (redimidos.contains(codigoNormalizado)) return;
        redimidos.add(codigoNormalizado);

        PersistentDataContainer data = player.getPersistentDataContainer();
        data.set(redeemedKey, PersistentDataType.STRING, String.join(";", redimidos));

        if (registrarCanjes) {
            registrarAuditoria(player, codigoNormalizado);
        }
    }

    private List<String> leerRedimidos(Player player) {
        PersistentDataContainer data = player.getPersistentDataContainer();
        String raw = data.get(redeemedKey, PersistentDataType.STRING);
        List<String> resultado = new ArrayList<>();
        if (raw == null || raw.isBlank()) return resultado;
        for (String parte : raw.split(";")) {
            if (!parte.isBlank()) resultado.add(parte);
        }
        return resultado;
    }

    private void registrarAuditoria(Player player, String codigoNormalizado) {
        String fecha = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String linea = fecha + " | " + player.getName() + " (" + player.getUniqueId() + ") | " + codigoNormalizado;

        plugin.getLogger().info("[Canjes] " + linea);

        File log = new File(plugin.getDataFolder(), "canjes-registro.log");
        try (FileWriter writer = new FileWriter(log, true)) {
            writer.write(linea + System.lineSeparator());
        } catch (IOException e) {
            plugin.getLogger().warning("[Canjes] No se pudo escribir el registro de auditoría: " + e.getMessage());
        }
    }

    public static String normalizar(String codigo) {
        return codigo == null ? "" : codigo.trim().toUpperCase(Locale.ROOT);
    }

    private ItemStack parseItem(Map<?, ?> raw) {
        if (raw.get("custom_item") != null) {
            return parseCustomItem(raw);
        }

        Object materialObj = raw.get("material");
        boolean discoAleatorio = Boolean.TRUE.equals(raw.get("disco_aleatorio"));

        Material material;
        if (discoAleatorio && !discos.isEmpty()) {
            material = discos.get((int) (Math.random() * discos.size()));
        } else {
            if (materialObj == null) {
                plugin.getLogger().warning("[Canjes] Recompensa sin 'material' definido, ignorada.");
                return null;
            }
            try {
                material = Material.valueOf(materialObj.toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("[Canjes] Material inválido: " + materialObj);
                return null;
            }
        }

        int cantidad = raw.get("cantidad") instanceof Number n ? n.intValue() : 1;

        ItemBuilder builder = new ItemBuilder(material, Math.max(1, cantidad));

        if (raw.get("nombre") != null) {
            builder.setDisplayName(ItemBuilder.format(raw.get("nombre").toString()));
        }

        if (raw.get("lore") instanceof List<?> loreList) {
            List<String> lore = new ArrayList<>();
            for (Object line : loreList) lore.add(ItemBuilder.format(line.toString()));
            builder.setLore(lore);
        }

        if (Boolean.TRUE.equals(raw.get("inrompible"))) {
            builder.setUnbrekeable(true);
        }

        if (raw.get("encantamientos") instanceof List<?> enchList) {
            for (Object entry : enchList) {
                aplicarEncantamiento(builder, entry.toString());
            }
        }

        if (raw.get("atributos") instanceof List<?> attrList) {
            for (Object entry : attrList) {
                if (entry instanceof Map<?, ?> attrMap) aplicarAtributo(builder, attrMap);
            }
        }

        ItemStack item = builder.build();
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        if (Boolean.TRUE.equals(raw.get("brillo"))) {
            meta.setEnchantmentGlintOverride(true);
        }

        if (meta instanceof PotionMeta potionMeta && raw.get("pocion") != null) {
            aplicarPocion(potionMeta, raw.get("pocion").toString());
        }

        if (meta instanceof SkullMeta skullMeta) {
            Object textura = raw.get("cabeza_textura");
            if (textura != null && !textura.toString().isBlank()) {
                aplicarTextura(skullMeta, textura.toString());
            } else if (raw.get("cabeza_dueno") != null) {
                OfflinePlayer owner = Bukkit.getOfflinePlayer(raw.get("cabeza_dueno").toString());
                skullMeta.setOwningPlayer(owner);
            }
        }

        if (meta instanceof BannerMeta bannerMeta && raw.get("bandera_patrones") instanceof List<?> patrones) {
            for (Object entry : patrones) {
                Pattern pattern = parsePatron(entry.toString());
                if (pattern != null) bannerMeta.addPattern(pattern);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack parseCustomItem(Map<?, ?> raw) {
        String key = raw.get("custom_item").toString();
        var custom = org.delta.customs.items.ItemRegistry.get(key);
        if (custom.isEmpty()) {
            plugin.getLogger().warning("[Canjes] Ítem personalizado desconocido: " + key);
            return null;
        }

        ItemStack item = custom.get().build();
        if (raw.get("cantidad") instanceof Number n) {
            item.setAmount(Math.max(1, n.intValue()));
        }
        return item;
    }

    private void aplicarEncantamiento(ItemBuilder builder, String raw) {
        String[] parts = raw.split(":");
        Enchantment enchantment = Registry.ENCHANTMENT.get(
                NamespacedKey.minecraft(parts[0].toLowerCase(Locale.ROOT)));
        if (enchantment == null) {
            plugin.getLogger().warning("[Canjes] Encantamiento inválido: " + parts[0]);
            return;
        }
        int nivel = parts.length > 1 ? parseInt(parts[1], 1) : 1;
        builder.addEnchant(enchantment, nivel);
    }

    private void aplicarAtributo(ItemBuilder builder, Map<?, ?> attrMap) {
        Object atributoObj = attrMap.get("atributo");
        if (atributoObj == null) return;

        Attribute attribute = Registry.ATTRIBUTE.get(
                NamespacedKey.minecraft(atributoObj.toString().toLowerCase(Locale.ROOT)));
        if (attribute == null) {
            plugin.getLogger().warning("[Canjes] Atributo inválido: " + atributoObj);
            return;
        }

        double cantidad = attrMap.get("cantidad") instanceof Number n ? n.doubleValue() : 0.0;
        String nombre = attrMap.get("nombre") != null
                ? attrMap.get("nombre").toString()
                : "canje_" + atributoObj.toString().toLowerCase(Locale.ROOT);

        AttributeModifier.Operation operation = AttributeModifier.Operation.ADD_NUMBER;
        if (attrMap.get("operacion") != null) {
            try {
                operation = AttributeModifier.Operation.valueOf(attrMap.get("operacion").toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }

        EquipmentSlot slot = null;
        if (attrMap.get("slot") != null) {
            try {
                slot = EquipmentSlot.valueOf(attrMap.get("slot").toString().toUpperCase(Locale.ROOT));
            } catch (IllegalArgumentException ignored) {
            }
        }

        builder.addAttributeModifier(attribute, nombre, cantidad, operation, slot);
        builder.hideAttributes();
    }

    private void aplicarTextura(SkullMeta meta, String textura) {
        try {
            com.destroystokyo.paper.profile.PlayerProfile profile =
                    Bukkit.createProfile(java.util.UUID.randomUUID(), null);
            profile.setProperty(new com.destroystokyo.paper.profile.ProfileProperty("textures", textura));
            meta.setPlayerProfile(profile);
        } catch (Exception e) {
            plugin.getLogger().warning("[Canjes] Textura de cabeza inválida: " + e.getMessage());
        }
    }

    private void aplicarPocion(PotionMeta meta, String tipo) {
        try {
            meta.setBasePotionType(PotionType.valueOf(tipo.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Canjes] Tipo de poción inválido: " + tipo);
        }
    }

    private Pattern parsePatron(String raw) {
        String[] parts = raw.split(":");
        if (parts.length < 2) return null;

        PatternType type = Registry.BANNER_PATTERN.get(
                NamespacedKey.minecraft(parts[0].toLowerCase(Locale.ROOT)));
        if (type == null) {
            plugin.getLogger().warning("[Canjes] Patrón de estandarte inválido: " + parts[0]);
            return null;
        }

        DyeColor color;
        try {
            color = DyeColor.valueOf(parts[1].toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[Canjes] Color de estandarte inválido: " + parts[1]);
            return null;
        }

        return new Pattern(color, type);
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public record Canje(String codigo, String descripcion, List<ItemStack> items) {
        public List<ItemStack> copiaItems() {
            List<ItemStack> copia = new ArrayList<>();
            for (ItemStack item : items) copia.add(item.clone());
            return copia;
        }
    }
}

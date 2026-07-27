package org.delta.managers.rango;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.delta.libs.MessageUtils;
import org.delta.libs.rango.Rango;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RangoManager {

    private final Plugin plugin;
    private File file;
    private FileConfiguration data;

    public RangoManager(Plugin plugin) {
        this.plugin = plugin;
        recargar();
    }

    public void recargar() {
        file = new File(plugin.getDataFolder(), "players.yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().warning("[Rangos] No se pudo crear players.yml: " + e.getMessage());
            }
        }
        data = YamlConfiguration.loadConfiguration(file);
    }

    public Rango getRango(Player player) {
        String valor = data.getString("players." + player.getName() + ".rango", "");
        try {
            return Rango.valueOf(valor.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return Rango.MIEMBRO;
        }
    }

    public String getEquipoPrefix(Player player) {
        return data.getString("players." + player.getName() + ".equipoPrefix", "");
    }

    public Component getPrefijo(Player player) {
        return Component.empty()
                .append(getRangoComponent(player))
                .append(getEquipoComponent(player));
    }

    public Component getRangoComponent(Player player) {
        Rango rango = getRango(player);
        Component texto = MessageUtils.color(rango.getColor() + rango.getEtiqueta());
        Component hover = MessageUtils.color(rango.getDescripcion());
        return texto.hoverEvent(HoverEvent.showText(hover));
    }

    public Component getEquipoComponent(Player player) {
        String equipo = getEquipoPrefix(player);
        Component texto = MessageUtils.color(equipo);
        if (equipo.isEmpty()) {
            return texto;
        }

        List<String> miembros = getMiembrosEquipo(equipo);
        Component hover = MessageUtils.color("&7Integrantes del equipo:");
        if (miembros.isEmpty()) {
            hover = hover.append(MessageUtils.color("\n&8(ninguno)"));
        } else {
            for (String nombre : miembros) {
                boolean online = Bukkit.getPlayerExact(nombre) != null;
                hover = hover.append(MessageUtils.color("\n" + (online ? "&a● " : "&7● ") + "&f" + nombre));
            }
        }
        return texto.hoverEvent(HoverEvent.showText(hover));
    }

    private List<String> getMiembrosEquipo(String equipoPrefix) {
        List<String> miembros = new ArrayList<>();
        ConfigurationSection section = data.getConfigurationSection("players");
        if (section == null) {
            return miembros;
        }
        for (String nombre : section.getKeys(false)) {
            String prefix = data.getString("players." + nombre + ".equipoPrefix", "");
            if (!prefix.isEmpty() && prefix.equals(equipoPrefix)) {
                miembros.add(nombre);
            }
        }
        return miembros;
    }

    public Component getNombre(Player player) {
        return MessageUtils.color("&f" + player.getName());
    }
}

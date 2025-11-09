package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;

import java.io.File;
import java.io.IOException;

import static org.bukkit.Bukkit.getServer;

public class DiaCommand implements SubCommand {

    @Override
    public String getName() {
        return "dia";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 1) {
            showUsage(player);
            return;
        }

        int nuevoDia;
        try {
            nuevoDia = Integer.parseInt(args[1]);

            if (nuevoDia < 0) {
                player.sendMessage(MessageUtils.color("&c✘ El día debe ser un número positivo."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.color("&c✘ Debes especificar un número válido."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            showUsage(player);
            return;
        }

        int diaActual = PendulumSettings.getInstance().getDia();

        if (actualizarDiaEnConfig(nuevoDia)) {
            PendulumSettings.getInstance().load();

            getServer().broadcast(MessageUtils.color(""));
            getServer().broadcast(MessageUtils.color("&8&m                                                    "));
            getServer().broadcast(MessageUtils.color("&8[&6&l!&8] &7El día del servidor ha cambiado a: &a&l" + nuevoDia));
            getServer().broadcast(MessageUtils.color("&8&m                                                    "));
            getServer().broadcast(MessageUtils.color(""));
            for (Player online : Bukkit.getOnlinePlayers()) {
                online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.2f);
            }

            Bukkit.getLogger().info("[Pendulum] " + player.getName() + " cambió el día de " + diaActual + " a " + nuevoDia);
        } else {
            player.sendMessage(MessageUtils.color("&c✘ Error al actualizar el día. Revisa la consola."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
        }
    }

    private boolean actualizarDiaEnConfig(int nuevoDia) {
        try {
            File file = new File(Bukkit.getPluginManager().getPlugin("Pendulum").getDataFolder(), "settings.yml");

            if (!file.exists()) {
                Bukkit.getLogger().severe("[Pendulum] El archivo settings.yml no existe.");
                return false;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("mundo.dia", nuevoDia);
            config.save(file);

            return true;
        } catch (IOException e) {
            Bukkit.getLogger().severe("[Pendulum] Error al actualizar el día en el config:");
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {

    }
}
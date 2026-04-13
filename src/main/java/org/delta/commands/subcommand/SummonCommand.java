package org.delta.commands.subcommand;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.MobRegistry;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

public class SummonCommand implements SubCommand {

    private final pendulum plugin;

    public SummonCommand(pendulum plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "summon";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (args.length < 2) {
            showUsage(player);
            return;
        }

        String mobKey = args[1].toLowerCase();

        Location spawnLocation;

        if (args.length >= 5) {
            try {
                double x = parseCoord(args[2], player.getLocation().getX());
                double y = parseCoord(args[3], player.getLocation().getY());
                double z = parseCoord(args[4], player.getLocation().getZ());
                spawnLocation = new Location(player.getWorld(), x, y, z);
            } catch (NumberFormatException e) {
                player.sendMessage(MessageUtils.color("&c✘ Coordenadas inválidas."));
                showUsage(player);
                return;
            }
        } else {
            spawnLocation = player.getLocation();
        }

        CustomMob customMob = MobRegistry.get(mobKey, plugin, spawnLocation).orElse(null);
        if (customMob == null) {
            player.sendMessage(MessageUtils.color("&c✘ Mob &f" + mobKey + " &cno encontrado."));
            showUsage(player);
            return;
        }

        customMob.build();
        player.sendMessage(MessageUtils.color("&a✔ Spawneaste &f" + mobKey
                + " &aen &f" + (int) spawnLocation.getX()
                + " " + (int) spawnLocation.getY()
                + " " + (int) spawnLocation.getZ()));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    // Soporta coordenadas relativas tipo ~ de Minecraft
    private double parseCoord(String input, double relative) {
        if (input.startsWith("~")) {
            String rest = input.substring(1);
            return relative + (rest.isEmpty() ? 0 : Double.parseDouble(rest));
        }
        return Double.parseDouble(input);
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DE SUMMON&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl summon <mob> &8[x] [y] [z]"));
        player.sendMessage(MessageUtils.color("&7Ejemplo: &f/pdl summon zombie_vengador ~ ~1 ~"));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}
package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

public class ChargeBaseCommand implements SubCommand {

    @Override
    public String getName() {
        return "chargebase";
    }

    @Override
    public void execute(Player player, String[] args) {
        ChargeBaseManager manager = pendulum.getInstance().getChargeBaseManager();

        if (args.length == 1) {
            showUsage(player);
            return;
        }

        // /pendulum chargebase info
        if (args[1].equalsIgnoreCase("info")) {
            if (!manager.isActive()) {
                player.sendMessage(MessageUtils.color("&c✘ No hay ninguna Base de Carga activa."));
                player.sendMessage(MessageUtils.color("&8└ &7Próxima en: &e" + manager.getTimeUntilNext()));
                return;
            }
            var zone = manager.getActiveZone();
            var center = zone.getCenter();
            player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lBASE DE CARGA&r &d&l&k|&r &8&l≪"));
            player.sendMessage(MessageUtils.color("&8└ &7Centro: &e" + (int)center.getX() + ", " + (int)center.getZ()));
            player.sendMessage(MessageUtils.color("&8└ &7Radio actual: &e" + String.format("%.1f", zone.getCurrentRadius())));
            player.sendMessage(MessageUtils.color("&8└ &7Radio inicial: &e" + (int)zone.getInitialRadius()));
            long remaining = manager.getRemainingTicks();
            long minutes = (remaining / 20) / 60;
            long seconds = (remaining / 20) % 60;
            player.sendMessage(MessageUtils.color("&8└ &7Tiempo restante: &e" + minutes + "m " + seconds + "s"));
            return;
        }

        // /pendulum chargebase start [radio]
        if (args[1].equalsIgnoreCase("start")) {
            if (manager.isActive()) {
                player.sendMessage(MessageUtils.color("&c✘ Ya hay una Base de Carga activa."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }

            double radius = 150.0;
            if (args.length == 3) {
                try {
                    radius = Double.parseDouble(args[2]);
                } catch (NumberFormatException e) {
                    player.sendMessage(MessageUtils.color("&c✘ Radio inválido."));
                    return;
                }
            }

            manager.startEventAt(player.getLocation(), radius);
            player.sendMessage(MessageUtils.color("&a✔ Base de Carga generada en tu posición."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.2f);
            return;
        }

        // /pendulum chargebase stop
        if (args[1].equalsIgnoreCase("stop")) {
            if (!manager.isActive()) {
                player.sendMessage(MessageUtils.color("&c✘ No hay ninguna Base de Carga activa."));
                return;
            }
            manager.forceEnd();
            player.sendMessage(MessageUtils.color("&a✔ Base de Carga terminada."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.7f, 1.2f);
            return;
        }

        showUsage(player);
    }

    @Override
    public boolean requiresPermission() { return true; }

    @Override
    public void showUsage(Player player) {
        player.sendMessage(MessageUtils.color("&8└ &7/pendulum chargebase start &8[radio]"));
        player.sendMessage(MessageUtils.color("&8└ &7/pendulum chargebase stop"));
        player.sendMessage(MessageUtils.color("&8└ &7/pendulum chargebase info"));
    }
}
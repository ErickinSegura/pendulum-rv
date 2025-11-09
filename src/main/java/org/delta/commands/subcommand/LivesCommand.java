package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

public class LivesCommand implements SubCommand {

    @Override
    public String getName() {
        return "relojs";
    }

    @Override
    public void execute(Player player, String[] args) {
        // /pendulum lives - Ver tus propias vidas
        if (args.length == 1) {
            showOwnLives(player);
            return;
        }

        // /pendulum lives <jugador> - Ver vidas de otro jugador
        if (args.length == 2) {
            showPlayerLives(player, args[1]);
            return;
        }

        // /pendulum lives set <jugador> <cantidad> - Setear vidas (admin)
        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            if (!isAdmin(player)) {
                player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
                return;
            }
            setPlayerLives(player, args[2], args[3]);
            return;
        }

        // /pendulum lives reset <jugador> - Resetear vidas (admin)
        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!isAdmin(player)) {
                player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
                return;
            }
            resetPlayerLives(player, args[2]);
            return;
        }

        // Si no coincide con ninguna sintaxis válida, mostrar uso
        showUsage(player);
    }

    @Override
    public void showUsage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        player.sendMessage(MessageUtils.color("&c&l⚠ Sintaxis incorrecta"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usos disponibles:"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum lives &8- &7Ver tus vidas"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum lives <jugador> &8- &7Ver vidas de otro"));

        if (isAdmin(player)) {
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum lives set <jugador> <cantidad> &8- &7Setear vidas"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum lives reset <jugador> &8- &7Resetear vidas"));
        }
        player.sendMessage("");
    }

    private void showOwnLives(Player player) {
        int lives = pendulum.getInstance().getLifeManager().getLives(player);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lTUS VIDAS&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &7Vidas restantes: " + getLifeDisplay(lives)));
        player.sendMessage("");
    }

    private void showPlayerLives(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int lives = pendulum.getInstance().getLifeManager().getLives(target);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lVIDAS DE " + target.getName().toUpperCase() + "&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &7Vidas restantes: " + getLifeDisplay(lives)));
        player.sendMessage("");
    }

    private void setPlayerLives(Player player, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.color("&cLa cantidad debe ser un número válido."));
            showUsage(player);
            return;
        }

        if (amount < 0 || amount > 3) {
            player.sendMessage(MessageUtils.color("&cLa cantidad debe estar entre 0 y 3."));
            showUsage(player);
            return;
        }

        pendulum.getInstance().getLifeManager().setLives(target, amount);

        player.sendMessage(MessageUtils.color("&aHas establecido las vidas de &e" + target.getName() + " &aa &d" + amount + "&a."));
        target.sendMessage(MessageUtils.color("&eTus vidas han sido establecidas a " + getLifeDisplay(amount) + "&e."));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
    }

    private void resetPlayerLives(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        pendulum.getInstance().getLifeManager().resetLives(target);

        player.sendMessage(MessageUtils.color("&aHas reseteado las vidas de &e" + target.getName() + " &aa &d3&a."));
        target.sendMessage(MessageUtils.color("&eTus vidas han sido reseteadas a " + getLifeDisplay(3) + "&e."));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
    }

    private String getLifeDisplay(int lives) {
        StringBuilder display = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            if (i < lives) {
                display.append("&a⏰");
            } else {
                display.append("&8⏰");
            }
            if (i < 2) display.append(" ");
        }
        return display.toString();
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("pendulum.admin") ||
                java.util.Arrays.asList(PendulumSettings.getInstance().getOp())
                        .contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }
}

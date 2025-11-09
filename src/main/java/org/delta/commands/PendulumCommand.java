package org.delta.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.delta.commands.subcommand.*;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.jetbrains.annotations.NotNull;


import java.util.HashMap;
import java.util.Map;

public class PendulumCommand implements CommandExecutor {
    private final Map<String, SubCommand> subCommands;

    public PendulumCommand() {
        this.subCommands = new HashMap<>();
        registerSubCommands();
    }

    private void registerSubCommands() {
        addSubCommand(new InfoCommand());
        addSubCommand(new RetoCommand());
        addSubCommand(new EntregarCommand());
        addSubCommand(new ResetRetoCommand());
        addSubCommand(new RuletaCommand());
        addSubCommand(new LivesCommand());
    }

    private void addSubCommand(SubCommand command) {
        subCommands.put(command.getName().toLowerCase(), command);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(MessageUtils.color("&cEste comando solo puede ser ejecutado por un jugador."));
            return true;
        }

        if (args.length == 0) {
            subCommands.get("info").execute(player, args);
            return true;
        }

        String subCommandName = args[0].toLowerCase();
        SubCommand subCommand = subCommands.get(subCommandName);

        if (subCommand == null) {
            player.sendMessage(MessageUtils.color("&cComando no reconocido."));
            return true;
        }

        if (subCommand.requiresPermission() && !checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
            return true;
        }

        subCommand.execute(player, args);
        return true;
    }

    private boolean checkPermission(Player player) {
        return player.hasPermission("pendulum.admin") ||
                java.util.Arrays.asList(PendulumSettings.getInstance().getOp())
                        .contains(player.getName());
    }
}
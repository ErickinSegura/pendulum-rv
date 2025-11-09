package org.delta.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.delta.libs.PendulumSettings;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandCompletion implements TabCompleter {
    private final Map<String, List<String>> subCommandCompletions;

    public CommandCompletion() {
        this.subCommandCompletions = new HashMap<>();
        initializeCompletions();
    }

    private void initializeCompletions() {
        // Comandos básicos disponibles para todos
        List<String> basicCommands = Arrays.asList(
                "reto", "info", "entregar", "relojs"
        );
        subCommandCompletions.put("basic", basicCommands);

        // Comandos de admin (ejemplo - agregar los que necesites)
        List<String> adminCommands = Arrays.asList(
                "reset_reto", "ruleta", "dia"
        );
        subCommandCompletions.put("admin", adminCommands);


        subCommandCompletions.put("relojs", Arrays.asList(
                "set", "reset"
        ));

    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        // Completar primer argumento (subcomandos)
        if (args.length == 1) {
            List<String> completions = new ArrayList<>(subCommandCompletions.get("basic"));

            // Agregar comandos de admin si tiene permisos
            if (checkPermission(player)) {
                List<String> adminCommands = subCommandCompletions.get("admin");
                if (adminCommands != null) {
                    completions.addAll(adminCommands);
                }
            }

            return filterCompletions(completions, args[0]);
        }

        // Completar segundo argumento según el subcomando
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("relojs") && checkPermission(player)) {
                List<String> spawnCompletions = subCommandCompletions.get("relojs");
                if (spawnCompletions != null) {
                    return filterCompletions(spawnCompletions, args[1]);
                }
            }
        }

        return Collections.emptyList();
    }

    private List<String> filterCompletions(List<String> completions, String partial) {
        if (completions == null || completions.isEmpty()) {
            return Collections.emptyList();
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted()
                .toList();
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) {
            return false;
        }
        return Arrays.asList(ops).contains(player.getName());
    }
}
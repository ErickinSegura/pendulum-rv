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
                "reto", "info", "entregar"
        );
        subCommandCompletions.put("basic", basicCommands);

        // Comandos de admin (ejemplo - agregar los que necesites)
        List<String> adminCommands = Arrays.asList(
                "reset_reto", "ruleta", "give"
        );
        subCommandCompletions.put("admin", adminCommands);

        // Completions para subcomandos específicos
        subCommandCompletions.put("give", Arrays.asList(
                "premio", "item", "totem"
        ));

        subCommandCompletions.put("spawn", Arrays.asList(
                "zombie", "skeleton", "creeper"
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
            if (args[0].equalsIgnoreCase("give") && checkPermission(player)) {
                List<String> giveCompletions = subCommandCompletions.get("give");
                if (giveCompletions != null) {
                    return filterCompletions(giveCompletions, args[1]);
                }
            } else if (args[0].equalsIgnoreCase("spawn") && checkPermission(player)) {
                List<String> spawnCompletions = subCommandCompletions.get("spawn");
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
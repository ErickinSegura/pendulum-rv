package org.delta.commands;

import org.bukkit.Bukkit;
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
                "reto", "info", "entregar", "relojs", "bingo"
        );
        subCommandCompletions.put("basic", basicCommands);

        // Comandos de admin
        List<String> adminCommands = Arrays.asList(
                "reset_reto", "ruleta", "dia", "bingo_reset", "bingo_reload"
        );
        subCommandCompletions.put("admin", adminCommands);

        // Subcomandos para relojs
        subCommandCompletions.put("relojs", Arrays.asList(
                "set", "reset"
        ));

        // Subcomandos para bingo (admin)
        subCommandCompletions.put("bingo_admin", Arrays.asList(
                "reset", "reload", "resetteam", "complete"
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
            // Subcomandos de relojs
            if (args[0].equalsIgnoreCase("relojs") && checkPermission(player)) {
                List<String> relojsCompletions = subCommandCompletions.get("relojs");
                if (relojsCompletions != null) {
                    return filterCompletions(relojsCompletions, args[1]);
                }
            }

            // Subcomandos de bingo (si es admin)
            if (args[0].equalsIgnoreCase("bingo") && checkPermission(player)) {
                List<String> bingoAdminCompletions = subCommandCompletions.get("bingo_admin");
                if (bingoAdminCompletions != null) {
                    return filterCompletions(bingoAdminCompletions, args[1]);
                }
            }
        }

        // Completar tercer argumento
        if (args.length == 3) {
            // Para bingo resetteam, autocompletar nombres de teams
            if (args[0].equalsIgnoreCase("bingo") &&
                    args[1].equalsIgnoreCase("resetteam") &&
                    checkPermission(player)) {
                return getTeamNames(args[2]);
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

    private List<String> getTeamNames(String partial) {
        return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .map(team -> team.getName())
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted()
                .toList();
    }
}
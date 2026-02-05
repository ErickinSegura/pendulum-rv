package org.delta.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.delta.libs.PendulumSettings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandCompletion implements TabCompleter {
    private final Map<String, List<String>> subCommandCompletions;

    public CommandCompletion() {
        this.subCommandCompletions = new HashMap<>();
        initializeCompletions();
    }

    private void initializeCompletions() {
        List<String> basicCommands = Arrays.asList(
                "reto", "info", "relojs", "bingo", "health", "chest"
        );
        subCommandCompletions.put("basic", basicCommands);

        List<String> adminCommands = Arrays.asList(
                "dia"
        );
        subCommandCompletions.put("admin", adminCommands);

        subCommandCompletions.put("reto", Arrays.asList(
                "entregar"
        ));

        subCommandCompletions.put("reto_admin", Arrays.asList(
                "reset", "ruleta", "lista"
        ));

        subCommandCompletions.put("relojs", Arrays.asList(
                "set", "reset", "sacrifice"
        ));

        subCommandCompletions.put("bingo", Arrays.asList(
                "stats", "lb"
        ));

        subCommandCompletions.put("bingo_admin", Arrays.asList(
                "reset"
        ));

        subCommandCompletions.put("health_admin", Arrays.asList(
                "set", "reset", "sacrifice"
        ));

        subCommandCompletions.put("chest_admin", Arrays.asList(
                "config", "info", "open"
        ));
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                      @NotNull String label, String[] args) {
        if (!(sender instanceof Player player)) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> completions = new ArrayList<>(subCommandCompletions.get("basic"));

            if (checkPermission(player)) {
                List<String> adminCommands = subCommandCompletions.get("admin");
                if (adminCommands != null) {
                    completions.addAll(adminCommands);
                }
            }

            return filterCompletions(completions, args[0]);
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reto")) {
                List<String> retoCompletions = new ArrayList<>(subCommandCompletions.get("reto"));
                if (checkPermission(player)) {
                    List<String> retoAdminCompletions = subCommandCompletions.get("reto_admin");
                    if (retoAdminCompletions != null) {
                        retoCompletions.addAll(retoAdminCompletions);
                    }
                }
                return filterCompletions(retoCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("relojs") && checkPermission(player)) {
                List<String> relojsCompletions = subCommandCompletions.get("relojs");
                if (relojsCompletions != null) {
                    return filterCompletions(relojsCompletions, args[1]);
                }
            }

            if (args[0].equalsIgnoreCase("bingo")) {
                List<String> bingoCompletions = new ArrayList<>(subCommandCompletions.get("bingo"));
                if (checkPermission(player)) {
                    List<String> bingoAdminCompletions = subCommandCompletions.get("bingo_admin");
                    if (bingoAdminCompletions != null) {
                        bingoCompletions.addAll(bingoAdminCompletions);
                    }
                }
                return filterCompletions(bingoCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("health")) {
                List<String> healthCompletions = new ArrayList<>(getOnlinePlayerNames(args[1]));
                if (checkPermission(player)) {
                    List<String> healthAdminCompletions = subCommandCompletions.get("health_admin");
                    if (healthAdminCompletions != null) {
                        healthCompletions.addAll(healthAdminCompletions);
                    }
                }
                return filterCompletions(healthCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("chest") && checkPermission(player)) {
                List<String> chestAdminCompletions = subCommandCompletions.get("chest_admin");
                if (chestAdminCompletions != null) {
                    return filterCompletions(chestAdminCompletions, args[1]);
                }
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reto") &&
                    args[1].equalsIgnoreCase("reset") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("relojs") &&
                    (args[1].equalsIgnoreCase("set") ||
                            args[1].equalsIgnoreCase("reset") ||
                            args[1].equalsIgnoreCase("sacrifice")) &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("bingo") &&
                    args[1].equalsIgnoreCase("reset") &&
                    checkPermission(player)) {
                List<String> completions = new ArrayList<>(getTeamNames(args[2]));
                completions.add("confirm");
                return filterCompletions(completions, args[2]);
            }

            if (args[0].equalsIgnoreCase("bingo") &&
                    args[1].equalsIgnoreCase("lb")) {
                List<String> completions = new ArrayList<>(getTeamNames(args[2]));
                return filterCompletions(completions, args[2]);
            }

            if (args[0].equalsIgnoreCase("health") &&
                    (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("reset")) &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("health") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("chest") &&
                    (args[1].equalsIgnoreCase("config")) &&
                    checkPermission(player)) {
                return getTeamNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("chest") &&
                    args[1].equalsIgnoreCase("info") &&
                    checkPermission(player)) {
                return getTeamNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("chest") &&
                    (args[1].equalsIgnoreCase("open")) &&
                    checkPermission(player)) {
                return getTeamNames(args[2]);
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("relojs") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return Collections.emptyList();
            }

            if (args[0].equalsIgnoreCase("health") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return Collections.emptyList();
            }

            if (args[0].equalsIgnoreCase("chest") &&
                    (args[1].equalsIgnoreCase("config") || args[1].equalsIgnoreCase("configurar")) &&
                    checkPermission(player)) {
                return Arrays.asList("1", "2", "3", "4", "5", "6");
            }
        }

        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("relojs") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[4]);
            }

            if (args[0].equalsIgnoreCase("health") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[4]);
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

    private List<String> getOnlinePlayerNames(String partial) {
        return Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted()
                .collect(Collectors.toList());
    }

    private List<String> getTeamNames(String partial) {
        return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .map(team -> team.getName())
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted()
                .toList();
    }
}
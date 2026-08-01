package org.delta.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.mobs.MobRegistry;
import org.delta.libs.PendulumSettings;
import org.delta.managers.perks.Perk;
import org.delta.worldgen.StructurePopulator;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.stream.Collectors;

public class CommandCompletion implements TabCompleter {
    private final Map<String, List<String>> subCommandCompletions;
    private final StructurePopulator structurePopulator;

    public CommandCompletion(StructurePopulator structurePopulator) {
        this.structurePopulator = structurePopulator;
        this.subCommandCompletions = new HashMap<>();
        initializeCompletions();
    }

    private void initializeCompletions() {
        List<String> basicCommands = Arrays.asList(
                "reto", "info", "relojes", "bingo", "chest", "perk", "canjear", "check"
        );
        subCommandCompletions.put("basic", basicCommands);

        List<String> adminCommands = Arrays.asList(
                "dia", "give", "summon", "chargebase", "structdev", "dirtyhearty", "health"
        );
        subCommandCompletions.put("admin", adminCommands);

        subCommandCompletions.put("reto", Arrays.asList(
                "entregar"
        ));

        subCommandCompletions.put("reto_admin", Arrays.asList(
                "reset", "ruleta", "lista"
        ));

        subCommandCompletions.put("relojes", Arrays.asList(
                "set", "reset", "sacrifice"
        ));

        subCommandCompletions.put("bingo", Arrays.asList(
                "stats", "lb"
        ));

        subCommandCompletions.put("bingo_admin", Arrays.asList(
                "reset", "generate", "debug"
        ));

        subCommandCompletions.put("health_admin", Arrays.asList(
                "set", "reset", "sacrifice"
        ));

        subCommandCompletions.put("chest_admin", Arrays.asList(
                "config", "info", "open"
        ));

        subCommandCompletions.put("perk_admin", Arrays.asList(
                "assign", "remove", "reset", "resetall"
        ));

        subCommandCompletions.put("give_items", new ArrayList<>(ItemRegistry.getKeys()));

        subCommandCompletions.put("summon_mobs", new ArrayList<>(MobRegistry.getKeys()));

        subCommandCompletions.put("perk_perks", Arrays.asList(
                Arrays.stream(Perk.values())
                        .map(p -> p.name().toLowerCase())
                        .toArray(String[]::new)
        ));

        subCommandCompletions.put("chargebase", List.of(
                "info"
        ));

        subCommandCompletions.put("chargebase_admin", Arrays.asList(
                "start", "stop"
        ));

        subCommandCompletions.put("structdev_admin", Arrays.asList(
                "wand", "scan", "spawn", "list"
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

            if (args[0].equalsIgnoreCase("relojes") && checkPermission(player)) {
                List<String> relojesCompletions = subCommandCompletions.get("relojes");
                if (relojesCompletions != null) {
                    return filterCompletions(relojesCompletions, args[1]);
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

            if (args[0].equalsIgnoreCase("health") && checkPermission(player)) {
                List<String> healthCompletions = new ArrayList<>(getOnlinePlayerNames(args[1]));
                List<String> healthAdminCompletions = subCommandCompletions.get("health_admin");
                if (healthAdminCompletions != null) {
                    healthCompletions.addAll(healthAdminCompletions);
                }
                return filterCompletions(healthCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("chest") && checkPermission(player)) {
                List<String> chestAdminCompletions = subCommandCompletions.get("chest_admin");
                if (chestAdminCompletions != null) {
                    return filterCompletions(chestAdminCompletions, args[1]);
                }
            }

            if (args[0].equalsIgnoreCase("perk")) {
                List<String> perkCompletions = new ArrayList<>(List.of("list"));
                if (checkPermission(player)) {
                    perkCompletions.addAll(subCommandCompletions.get("perk_admin"));
                }
                return filterCompletions(perkCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("give") && checkPermission(player)) {
                return filterCompletions(subCommandCompletions.get("give_items"), args[1]);
            }

            if (args[0].equalsIgnoreCase("summon") && checkPermission(player)) {
                return filterCompletions(subCommandCompletions.get("summon_mobs"), args[1]);
            }

            if (args[0].equalsIgnoreCase("chargebase")) {
                List<String> chargebaseCompletions = new ArrayList<>(subCommandCompletions.get("chargebase"));
                if (checkPermission(player)) {
                    List<String> chargebaseAdminCompletions = subCommandCompletions.get("chargebase_admin");
                    if (chargebaseAdminCompletions != null) {
                        chargebaseCompletions.addAll(chargebaseAdminCompletions);
                    }
                }
                return filterCompletions(chargebaseCompletions, args[1]);
            }

            if (args[0].equalsIgnoreCase("structdev") && checkPermission(player)) {
                return filterCompletions(subCommandCompletions.get("structdev_admin"), args[1]);
            }

            if (args[0].equalsIgnoreCase("dirtyhearty") && checkPermission(player)) {
                return filterCompletions(List.of("reset"), args[1]);
            }


        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("reto") &&
                    args[1].equalsIgnoreCase("reset") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("relojes") &&
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

            if (args[0].equalsIgnoreCase("perk") &&
                    (args[1].equalsIgnoreCase("assign") ||
                            args[1].equalsIgnoreCase("remove") ||
                            args[1].equalsIgnoreCase("reset") ||
                            args[1].equalsIgnoreCase("list"))) {
                return getTeamNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("give") && checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }

            if (args[0].equalsIgnoreCase("summon") && checkPermission(player)) {
                return List.of(String.valueOf((int) player.getLocation().getX()), "~");
            }

            if (args[0].equalsIgnoreCase("structdev") &&
                    args[1].equalsIgnoreCase("spawn") &&
                    checkPermission(player)) {
                return filterCompletions(getStructureIds(), args[2]);
            }

            if (args[0].equalsIgnoreCase("dirtyhearty") &&
                    args[1].equalsIgnoreCase("reset") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[2]);
            }
        }

        if (args.length == 4) {
            if (args[0].equalsIgnoreCase("relojes") &&
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

            if (args[0].equalsIgnoreCase("perk") &&
                    (args[1].equalsIgnoreCase("assign") || args[1].equalsIgnoreCase("remove")) &&
                    checkPermission(player)) {
                return filterCompletions(subCommandCompletions.get("perk_perks"), args[3]);
            }

            if (args[0].equalsIgnoreCase("summon") && checkPermission(player)) {
                return List.of(String.valueOf((int) player.getLocation().getY()), "~");
            }
        }

        if (args.length == 5) {
            if (args[0].equalsIgnoreCase("relojes") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[4]);
            }

            if (args[0].equalsIgnoreCase("health") &&
                    args[1].equalsIgnoreCase("sacrifice") &&
                    checkPermission(player)) {
                return getOnlinePlayerNames(args[4]);
            }

            if (args[0].equalsIgnoreCase("summon") && checkPermission(player)) {
                return List.of(String.valueOf((int) player.getLocation().getY()), "~");
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

    private List<String> getStructureIds() {
        if (structurePopulator == null) {
            return Collections.emptyList();
        }
        return structurePopulator.getStructures().stream()
                .map(org.delta.worldgen.StructureDef::getId)
                .sorted()
                .toList();
    }

    private List<String> getTeamNames(String partial) {
        return Bukkit.getScoreboardManager().getMainScoreboard().getTeams().stream()
                .map(team -> team.getName())
                .filter(name -> name.toLowerCase().startsWith(partial.toLowerCase()))
                .sorted()
                .toList();
    }
}
package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.managers.teamChest.TeamChestManager;

public class TeamChestCommand implements SubCommand {

    private final TeamChestManager manager;

    public TeamChestCommand() {
        this.manager = TeamChestManager.getInstance();
    }

    @Override
    public String getName() {
        return "chest";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            openChest(player);
            return;
        }

        String subcomando = args[1].toLowerCase();

        switch (subcomando) {
            case "config" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length < 4) {
                        player.sendMessage(MessageUtils.color("&c✘ Uso: /pdl teamchest config <equipo> <filas>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                        return;
                    }
                    setSize(player, args[2], args[3]);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                }
            }
            case "info" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    showInfo(player, args.length > 2 ? args[2] : null);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                }
            }
            case "open" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length < 3) {
                        player.sendMessage(MessageUtils.color("&c✘ Uso: /pdl chest open <equipo>"));
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                        return;
                    }
                    openSpecificTeamChest(player, args[2]);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                }
            }
            default -> {
                player.sendMessage(MessageUtils.color("&c✘ Subcomando no reconocido."));
                showUsage(player);
            }
        }
    }

    private void openChest(Player player) {
        Scoreboard scoreboard = player.getScoreboard();
        Team team = scoreboard.getEntryTeam(player.getName());

        if (team == null) {
            player.sendMessage(MessageUtils.color("&c✘ No estás en ningún equipo."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        String teamName = team.getName();

        if (manager.isChestOpen(teamName)) {
            String playerInChest = manager.getPlayerInChest(teamName);
            player.sendMessage(MessageUtils.color("&c✘ El cofre del equipo está siendo usado por &e" + playerInChest));
            player.sendMessage(MessageUtils.color("&7Espera a que termine de usarlo."));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 1.0f);
            return;
        }

        if (manager.openChest(player, teamName)) {
            player.openInventory(manager.getTeamChest(teamName));
            player.sendMessage(MessageUtils.color("&a✔ Abriendo cofre del equipo &e" + teamName + "&a..."));
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
        } else {
            player.sendMessage(MessageUtils.color("&c✘ No se pudo abrir el cofre. Intenta de nuevo."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    private void openSpecificTeamChest(Player admin, String teamName) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = scoreboard.getTeam(teamName);

        if (team == null) {
            admin.sendMessage(MessageUtils.color("&c✘ El equipo &e" + teamName + " &cno existe."));
            admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (manager.isChestOpen(teamName)) {
            String playerInChest = manager.getPlayerInChest(teamName);
            admin.sendMessage(MessageUtils.color("&e⚠ &7El cofre está siendo usado por &e" + playerInChest));
            admin.sendMessage(MessageUtils.color("&7Abriendo de todos modos como administrador..."));
        }

        if (manager.openChest(admin, teamName)) {
            admin.openInventory(manager.getTeamChest(teamName));
            admin.sendMessage("");
            admin.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOFRE DEL EQUIPO&r &d&l&k|&r &8&l≪"));
            admin.sendMessage("");
            admin.sendMessage(MessageUtils.color("&8└ &7Equipo: &d" + teamName));
            admin.sendMessage(MessageUtils.color("&8└ &7Modo: &6Admin"));
            admin.sendMessage("");
            admin.playSound(admin.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.5f, 1.0f);
        } else {
            admin.sendMessage(MessageUtils.color("&c✘ No se pudo abrir el cofre. Intenta de nuevo."));
            admin.playSound(admin.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    private void setSize(Player executor, String teamName, String rowsStr) {
        int rows;
        try {
            rows = Integer.parseInt(rowsStr);
        } catch (NumberFormatException e) {
            executor.sendMessage(MessageUtils.color("&c✘ El número de filas debe ser un número entre 1 y 6."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (rows < 1 || rows > 6) {
            executor.sendMessage(MessageUtils.color("&c✘ El número de filas debe estar entre 1 y 6."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (manager.isChestOpen(teamName)) {
            String playerInChest = manager.getPlayerInChest(teamName);
            executor.sendMessage(MessageUtils.color("&c✘ No puedes configurar el cofre mientras &e" + playerInChest + " &clo está usando."));
            executor.playSound(executor.getLocation(), Sound.BLOCK_CHEST_LOCKED, 0.5f, 1.0f);
            return;
        }

        if (manager.setTeamChestSize(teamName, rows)) {
            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOFRE CONFIGURADO&r &d&l&k|&r &8&l≪"));
            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8└ &7Equipo: &d" + teamName));
            executor.sendMessage(MessageUtils.color("&8└ &7Filas: &d" + rows + " &8(" + (rows * 9) + " slots)"));
            executor.sendMessage("");
            executor.playSound(executor.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.5f);

            Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                for (String entry : team.getEntries()) {
                    Player member = Bukkit.getPlayer(entry);
                    if (member != null && !member.equals(executor)) {
                        member.sendMessage(MessageUtils.color("&6⚠ &7El cofre de tu equipo ha sido reconfigurado a &d" + rows + " filas&7."));
                        member.playSound(member.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.3f, 1.0f);
                    }
                }
            }
        } else {
            executor.sendMessage(MessageUtils.color("&c✘ Error al configurar el cofre."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    private void showInfo(Player executor, String teamName) {
        if (teamName == null) {
            Scoreboard scoreboard = executor.getScoreboard();
            Team team = scoreboard.getEntryTeam(executor.getName());
            if (team == null) {
                executor.sendMessage(MessageUtils.color("&c✘ Debes especificar un equipo o estar en uno."));
                executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
            teamName = team.getName();
        }

        int rows = manager.getTeamChestRows(teamName);
        boolean isOpen = manager.isChestOpen(teamName);
        String playerUsing = manager.getPlayerInChest(teamName);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lINFO DEL COFRE&r &d&l&k|&r &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Equipo: &d" + teamName));
        executor.sendMessage(MessageUtils.color("&8└ &7Capacidad: &d" + rows + " filas &8(" + (rows * 9) + " slots)"));

        if (isOpen && playerUsing != null) {
            executor.sendMessage(MessageUtils.color("&8└ &7Estado: &c Abierto por &e" + playerUsing));
        } else {
            executor.sendMessage(MessageUtils.color("&8└ &7Estado: &a Cerrado"));
        }

        executor.sendMessage("");
        executor.playSound(executor.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DEL TEAM CHEST&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl chest &7- Abrir cofre de tu equipo"));

        if (checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&d/pdl chest open <equipo> &7- Abrir cofre de un equipo &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl chest config <equipo> <filas> &7- Configurar tamaño &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl chest info [equipo] &7- Ver información del cofre &8(Admin)"));
        }

        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}
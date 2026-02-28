package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.managers.perks.Perk;
import org.delta.managers.perks.PerkManager;

import java.util.Arrays;

public class PerkCommand implements SubCommand {

    @Override
    public String getName() {
        return "perk";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2) {
            showUsage(player);
            return;
        }

        String action = args[1].toLowerCase();

        switch (action) {
            case "assign" -> {
                if (!isAdmin(player)) { denyPermission(player); return; }
                if (args.length < 4) { showUsage(player); return; }
                String teamId = args[2];
                Perk perk = parsePerk(player, args[3]);
                if (perk == null) return;

                PerkManager.getInstance().assignPerk(teamId, perk);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
                player.sendMessage(MessageUtils.color("&aAsignada la perk &f" + perk.getDisplayName() + " &aal equipo &f" + teamId));
            }
            case "remove" -> {
                if (!isAdmin(player)) { denyPermission(player); return; }
                if (args.length < 4) { showUsage(player); return; }
                String teamId = args[2];
                Perk perk = parsePerk(player, args[3]);
                if (perk == null) return;

                PerkManager.getInstance().removePerk(teamId, perk);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                player.sendMessage(MessageUtils.color("&cRemovida la perk &f" + perk.getDisplayName() + " &cdel equipo &f" + teamId));
            }
            case "reset" -> {
                if (!isAdmin(player)) { denyPermission(player); return; }
                if (args.length < 3) { showUsage(player); return; }
                String teamId = args[2];

                PerkManager.getInstance().resetTeam(teamId);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.0f);
                player.sendMessage(MessageUtils.color("&eReseteadas las perks del equipo &f" + teamId));
            }
            case "resetall" -> {
                if (!isAdmin(player)) { denyPermission(player); return; }

                PerkManager.getInstance().resetAll();
                player.playSound(player.getLocation(), Sound.ENTITY_WITHER_DEATH, 0.4f, 1.2f);
                player.sendMessage(MessageUtils.color("&eReseteadas las perks de &ctodos &elos equipos."));
            }
            case "list" -> {
                if (args.length < 3) { showUsage(player); return; }
                String teamId = args[2];

                var perks = PerkManager.getInstance().getTeamPerks(teamId);
                player.sendMessage("");
                player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lPERKS — " + teamId + "&r &d&l&k|&r &8&l≪"));
                player.sendMessage("");
                if (perks.isEmpty()) {
                    player.sendMessage(MessageUtils.color("&8└ &7Sin perks activas."));
                } else {
                    perks.forEach(p -> {
                        String color = p.getCategory() == Perk.PerkCategory.BENEFICIAL ? "&a" : "&c";
                        String tag = p.getCategory() == Perk.PerkCategory.BENEFICIAL ? "&8[&a+&8]" : "&8[&c-&8]";
                        player.sendMessage(MessageUtils.color("&8└ " + tag + " " + color + p.getDisplayName()));
                    });
                }
                player.sendMessage("");
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
            }
            default -> showUsage(player);
        }
    }

    private Perk parsePerk(Player player, String name) {
        try {
            return Perk.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            player.sendMessage(MessageUtils.color("&cLa perk &f" + name + " &cno existe."));
            return null;
        }
    }

    private void denyPermission(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("pendulum.admin") ||
                Arrays.asList(PendulumSettings.getInstance().getOp()).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        player.sendMessage(MessageUtils.color("&c&l⚠ Sintaxis incorrecta"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usos disponibles:"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum perk list <equipo> &8- &7Ver perks activas"));
        if (isAdmin(player)) {
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum perk assign <equipo> <perk> &8- &7Asignar perk"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum perk remove <equipo> <perk> &8- &7Quitar perk"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum perk reset <equipo> &8- &7Resetear perks de un equipo"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum perk resetall &8- &7Resetear perks de todos"));
        }
        player.sendMessage("");
    }
}
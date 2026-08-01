package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class CheckCommand implements SubCommand {

    @Override
    public String getName() {
        return "check";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length >= 2) {
            Bukkit.dispatchCommand(player, "ptl check " + args[1]);
        } else {
            Bukkit.dispatchCommand(player, "ptl check");
        }
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {

    }
}

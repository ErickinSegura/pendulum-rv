package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

public class CompletionsCommand implements SubCommand {

    private final pendulum plugin;

    public CompletionsCommand(pendulum plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "completions";
    }

    @Override
    public void execute(Player player, String[] args) {
        boolean visibles = plugin.getAdminCompletionManager().toggle(player);
        player.updateCommands();

        if (visibles) {
            player.sendMessage(MessageUtils.color("&8[&6&l!&8] &7Completions de admin &a&lactivadas&7."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.7f, 1.4f);
        } else {
            player.sendMessage(MessageUtils.color("&8[&6&l!&8] &7Completions de admin &c&locultas&7."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.7f, 0.8f);
        }
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {

    }
}

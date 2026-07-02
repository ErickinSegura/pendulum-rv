package org.delta.commands.subcommand;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.managers.dirtyhearty.DirtyHeartyManager;

public class DirtyHeartyCommand implements SubCommand {

    @Override
    public String getName() {
        return "dirtyhearty";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length < 2 || !args[1].equalsIgnoreCase("reset")) {
            showUsage(player);
            return;
        }

        if (args.length >= 3) {
            resetPlayer(player, args[2]);
        } else {
            resetAll(player);
        }
    }

    private void resetPlayer(Player executor, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador '" + targetName + "' no está conectado."));
            playError(executor);
            return;
        }

        int before = DirtyHeartyManager.getCount(target);
        DirtyHeartyManager.reset(target);
        playReset(target);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &c&l&k|&r &4&lDIRTY HEARTY RESETEADO&r &c&l&k|&r &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Jugador: &c" + target.getName()));
        executor.sendMessage(MessageUtils.color("&8└ &7Corazones removidos: &c" + before));
        executor.sendMessage("");

        if (!target.equals(executor)) {
            target.sendMessage("");
            target.sendMessage(MessageUtils.color("&8&l≫ &c&l&k|&r &4&lDIRTY HEARTY RESETEADO&r &c&l&k|&r &8&l≪"));
            target.sendMessage("");
            target.sendMessage(MessageUtils.color("&8└ &7Tus corazones de Dirty Hearty han sido removidos."));
            target.sendMessage(MessageUtils.color("&8└ &7Reseteado por: &c" + executor.getName()));
            target.sendMessage("");
        }
    }

    private void resetAll(Player executor) {
        int affected = 0;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (DirtyHeartyManager.getCount(online) > 0) affected++;
            DirtyHeartyManager.reset(online);
            playReset(online);
        }

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &c&l&k|&r &4&lDIRTY HEARTY RESETEADO&r &c&l&k|&r &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Se reseteó a todos los jugadores conectados."));
        executor.sendMessage(MessageUtils.color("&8└ &7Jugadores afectados: &c" + affected));
        executor.sendMessage("");
    }

    private void playReset(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_WITHER_HURT, 0.5f, 1.2f);
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.SMOKE, loc, 20, 0.4, 0.5, 0.4, 0.05);
    }

    private void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &c&l&k|&r &4&lDIRTY HEARTY&r &c&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&c/pdl dirtyhearty reset &7- Resetear a todos los conectados"));
        player.sendMessage(MessageUtils.color("&c/pdl dirtyhearty reset <jugador> &7- Resetear a un jugador"));
        player.sendMessage("");
        playError(player);
    }
}

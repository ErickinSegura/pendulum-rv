package org.delta.commands.subcommand;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.MessageUtils;
import org.delta.managers.canje.CanjeManager;

import java.util.ArrayList;
import java.util.List;

public class CanjearCommand implements SubCommand {

    private static final String PERMISSION = "pendulum.canjear";

    @Override
    public String getName() {
        return "canjear";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!player.hasPermission(PERMISSION)) {
            player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para canjear códigos."));
            playError(player);
            return;
        }

        if (args.length < 2) {
            showUsage(player);
            return;
        }

        CanjeManager manager = CanjeManager.getInstance();
        if (manager == null) {
            player.sendMessage(MessageUtils.color("&c✘ El sistema de canjes no está disponible en este momento."));
            playError(player);
            return;
        }

        String codigoIngresado = args[1];
        String codigoNormalizado = CanjeManager.normalizar(codigoIngresado);

        CanjeManager.Canje canje = manager.buscar(codigoNormalizado);
        if (canje == null) {
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8&l≫ &c&l&k|&r &4&lCÓDIGO INVÁLIDO&r &c&l&k|&r &8&l≪"));
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8└ &7El código &f" + codigoIngresado + " &7no existe."));
            player.sendMessage(MessageUtils.color("&8└ &7Revisa que lo hayas escrito correctamente."));
            player.sendMessage("");
            playError(player);
            return;
        }

        if (manager.yaCanjeado(player, codigoNormalizado)) {
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|&r &e&lCÓDIGO YA CANJEADO&r &6&l&k|&r &8&l≪"));
            player.sendMessage("");
            player.sendMessage(MessageUtils.color("&8└ &7Ya canjeaste el código &f" + canje.codigo() + " &7anteriormente."));
            player.sendMessage(MessageUtils.color("&8└ &7Cada código solo puede canjearse una vez por jugador."));
            player.sendMessage("");
            playError(player);
            return;
        }

        entregarRecompensas(player, canje.copiaItems());
        manager.marcarCanjeado(player, codigoNormalizado);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &a&l&k|&r &2&l¡CÓDIGO CANJEADO!&r &a&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &7Código: &a" + canje.codigo()));
        if (!canje.descripcion().isBlank()) {
            player.sendMessage(MessageUtils.color("&8└ &7Recompensa: " + canje.descripcion()));
        }
        player.sendMessage(MessageUtils.color("&8└ &7Revisa tu inventario."));
        player.sendMessage("");

        playSuccess(player);
    }

    private void entregarRecompensas(Player player, List<ItemStack> items) {
        List<ItemStack> sobrantes = new ArrayList<>();
        for (ItemStack item : items) {
            sobrantes.addAll(player.getInventory().addItem(item).values());
        }

        if (!sobrantes.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack sobrante : sobrantes) {
                player.getWorld().dropItemNaturally(loc, sobrante);
            }
            player.sendMessage(MessageUtils.color("&e⚠ Tu inventario estaba lleno; algunos ítems se soltaron a tus pies."));
        }
    }

    private void playSuccess(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 2.0f);
        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 20, 0.4, 0.6, 0.4, 0.15);
        player.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, loc, 12, 0.4, 0.6, 0.4, 0.1);
    }

    private void playError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCANJEAR CÓDIGO&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl canjear <código> &7- Canjea un código de easter egg"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Descubre códigos secretos explorando el servidor y la web."));
        player.sendMessage("");
        playError(player);
    }
}

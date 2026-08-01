package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.managers.event.EventManager;
import org.delta.managers.event.ServerEvent;
import org.delta.pendulum;

public class EventCommand implements SubCommand {

    private final pendulum plugin;

    public EventCommand(pendulum plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getName() {
        return "evento";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (args.length < 2) {
            showUsage(player);
            return;
        }

        EventManager manager = plugin.getEventManager();
        String accion = args[1].toLowerCase();

        switch (accion) {
            case "activar" -> activar(player, args, manager);
            case "desactivar" -> desactivar(player, manager);
            default -> showUsage(player);
        }
    }

    private void activar(Player player, String[] args, EventManager manager) {
        if (args.length < 3) {
            showUsage(player);
            return;
        }

        if (manager.isActive()) {
            player.sendMessage(MessageUtils.color("&c✘ Ya hay un evento activo: &f"
                    + manager.getActiveEvent().getDisplayName()));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        String id = args[2].toLowerCase();
        ServerEvent evento = manager.get(id);
        if (evento == null) {
            player.sendMessage(MessageUtils.color("&c✘ Evento &f" + id + " &cno encontrado."));
            showUsage(player);
            return;
        }

        if (manager.activate(id, player.getLocation())) {
            player.sendMessage(MessageUtils.color("&a✔ Evento &f" + evento.getDisplayName() + " &aactivado."));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        } else {
            player.sendMessage(MessageUtils.color("&c✘ No se pudo activar el evento."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        }
    }

    private void desactivar(Player player, EventManager manager) {
        if (!manager.isActive()) {
            player.sendMessage(MessageUtils.color("&c✘ No hay ningún evento activo."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        String nombre = manager.getActiveEvent().getDisplayName();
        manager.deactivate();
        player.sendMessage(MessageUtils.color("&a✔ Evento &f" + nombre + " &adesactivado."));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return player.hasPermission("pendulum.admin")
                || java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DE EVENTOS&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl evento activar <evento>"));
        player.sendMessage(MessageUtils.color("&d/pdl evento desactivar"));
        player.sendMessage(MessageUtils.color("&7Eventos: &finauguracion"));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}

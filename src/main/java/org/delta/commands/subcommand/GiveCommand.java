package org.delta.commands.subcommand;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.commands.menu.GiveMenuHolder;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.ItemRegistry;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;

public class GiveCommand implements SubCommand {

    @Override
    public String getName() {
        return "give";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (!checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (args.length < 2) {
            player.openInventory(new GiveMenuHolder().getInventory());
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.6f, 1.2f);
            return;
        }

        String itemKey = args[1].toLowerCase();

        Player target = player;
        if (args.length >= 3) {
            target = org.bukkit.Bukkit.getPlayer(args[2]);
            if (target == null) {
                player.sendMessage(MessageUtils.color("&c✘ El jugador &f" + args[2] + " &cno está conectado."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
                return;
            }
        }

        CustomItem customItem = ItemRegistry.get(itemKey).orElse(null);
        if (customItem == null) {
            player.sendMessage(MessageUtils.color("&c✘ Item &f" + itemKey + " &cno encontrado."));
            showUsage(player);
            return;
        }

        target.getInventory().addItem(customItem.build());
        notify(player, target, itemKey);
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
    }

    private void notify(Player ejecutor, Player objetivo, String itemNombre) {
        if (ejecutor.equals(objetivo)) {
            ejecutor.sendMessage(MessageUtils.color("&a✔ Recibiste: &f" + itemNombre));
        } else {
            ejecutor.sendMessage(MessageUtils.color("&a✔ Le diste &f" + itemNombre + " &aa &d" + objetivo.getName()));
            objetivo.sendMessage(MessageUtils.color("&a✔ Recibiste: &f" + itemNombre + " &ade &d" + ejecutor.getName()));
            objetivo.playSound(objetivo.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        }
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return true;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DE GIVE&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl give <item> &8[jugador]"));
        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
    }
}
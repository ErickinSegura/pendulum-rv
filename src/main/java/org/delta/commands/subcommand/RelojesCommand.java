package org.delta.commands.subcommand;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.delta.libs.Icons;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

public class RelojesCommand implements SubCommand {

    @Override
    public String getName() {
        return "relojes";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            showOwnLives(player);
            return;
        }

        if (args.length == 2) {
            showPlayerLives(player, args[1]);
            return;
        }

        if (args.length == 4 && args[1].equalsIgnoreCase("set")) {
            if (!isAdmin(player)) {
                player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
                return;
            }
            setPlayerLives(player, args[2], args[3]);
            return;
        }

        if (args.length == 3 && args[1].equalsIgnoreCase("reset")) {
            if (!isAdmin(player)) {
                player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
                return;
            }
            resetPlayerLives(player, args[2]);
            return;
        }

        if (args.length == 5 && args[1].equalsIgnoreCase("sacrifice")) {
            if (!isAdmin(player)) {
                player.sendMessage(MessageUtils.color("&cNo tienes permisos para ejecutar este comando."));
                return;
            }
            sacrificeLives(player, args[2], args[3], args[4]);
            return;
        }

        showUsage(player);
    }

    @Override
    public void showUsage(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
        player.sendMessage(MessageUtils.color("&c&l⚠ Sintaxis incorrecta"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Usos disponibles:"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum relojes &8- &7Ver tus relojes"));
        player.sendMessage(MessageUtils.color("&8▪ &e/pendulum relojes <jugador> &8- &7Ver relojes de otro"));

        if (isAdmin(player)) {
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum relojes set <jugador> <cantidad> &8- &7Setear relojes"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum relojes reset <jugador> &8- &7Resetear relojes"));
            player.sendMessage(MessageUtils.color("&8▪ &e/pendulum relojes sacrifice <sacrificador> <cantidad> <receptor> &8- &7Sacrificar relojes"));
        }
        player.sendMessage("");
    }

    private void showOwnLives(Player player) {
        int lives = pendulum.getInstance().getLifeManager().getLives(player);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lTUS RELOJES&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &7Relojes restantes: ").append(getLifeDisplay(lives)));
        player.sendMessage("");
    }

    private void showPlayerLives(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int lives = pendulum.getInstance().getLifeManager().getLives(target);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lRELOJES DE " + target.getName() + "&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &7Relojes restantes: ").append(getLifeDisplay(lives)));
        player.sendMessage("");
    }

    private void setPlayerLives(Player player, String targetName, String amountStr) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            player.sendMessage(MessageUtils.color("&cLa cantidad debe ser un número válido."));
            showUsage(player);
            return;
        }

        int maxLives = PendulumSettings.getInstance().getVidas();
        if (amount < 0 || amount > maxLives) {
            player.sendMessage(MessageUtils.color("&cLa cantidad debe estar entre 0 y " + maxLives + "."));
            showUsage(player);
            return;
        }

        pendulum.getInstance().getLifeManager().setLives(target, amount);

        player.sendMessage(MessageUtils.color("&aHas establecido los relojes de &e" + target.getName() + " &aa &d" + amount + "&a."));
        target.sendMessage(MessageUtils.color("&eTus relojes han sido establecidos a ").append(getLifeDisplay(amount)).append(MessageUtils.color("&e.")));
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
    }

    private void resetPlayerLives(Player player, String targetName) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(MessageUtils.color("&cEl jugador &e" + targetName + " &cno está conectado."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        pendulum.getInstance().getLifeManager().resetLives(target);

        int maxLives = PendulumSettings.getInstance().getVidas();
        player.sendMessage(MessageUtils.color("&aHas reseteado los relojes de &e" + target.getName() + " &aa &d" + maxLives + "&a."));
        target.sendMessage(MessageUtils.color("&eTus relojes han sido reseteados a ").append(getLifeDisplay(maxLives)).append(MessageUtils.color("&e.")));

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
        target.playSound(target.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);
    }

    private void sacrificeLives(Player executor, String sacrificerName, String amountStr, String receiverName) {
        Player sacrificer = Bukkit.getPlayer(sacrificerName);
        Player receiver = Bukkit.getPlayer(receiverName);

        if (sacrificer == null) {
            executor.sendMessage(MessageUtils.color("&cEl jugador sacrificador &e" + sacrificerName + " &cno está conectado."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (receiver == null) {
            executor.sendMessage(MessageUtils.color("&cEl jugador receptor &e" + receiverName + " &cno está conectado."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (sacrificer.equals(receiver)) {
            executor.sendMessage(MessageUtils.color("&cUn jugador no puede sacrificarse a sí mismo."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(amountStr);
        } catch (NumberFormatException e) {
            executor.sendMessage(MessageUtils.color("&cLa cantidad debe ser un número válido."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        if (amount <= 0) {
            executor.sendMessage(MessageUtils.color("&cLa cantidad debe ser mayor a 0."));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int sacrificerLives = pendulum.getInstance().getLifeManager().getLives(sacrificer);
        int receiverLives = pendulum.getInstance().getLifeManager().getLives(receiver);

        if (amount > sacrificerLives) {
            executor.sendMessage(MessageUtils.color("&cEl jugador &e" + sacrificerName + " &cno tiene suficientes relojes."));
            executor.sendMessage(MessageUtils.color("&cRelojes actuales: &d" + sacrificerLives + " &c| Intenta sacrificar: &d" + amount));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int maxLives = PendulumSettings.getInstance().getVidas();
        int newReceiverLives = receiverLives + amount;
        if (newReceiverLives > maxLives) {
            executor.sendMessage(MessageUtils.color("&cEl receptor superaría el límite de " + maxLives + " relojes."));
            executor.sendMessage(MessageUtils.color("&cRelojes actuales: &d" + receiverLives + " &c| Resultado: &d" + newReceiverLives));
            executor.playSound(executor.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
            return;
        }

        int newSacrificerLives = sacrificerLives - amount;

        pendulum.getInstance().getLifeManager().setLives(sacrificer, newSacrificerLives);
        pendulum.getInstance().getLifeManager().setLives(receiver, newReceiverLives);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lSACRIFICIO REALIZADO&r &d&l&k|&r &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Sacrificador: &e" + sacrificer.getName()));
        executor.sendMessage(MessageUtils.color("&8└ &7Relojes sacrificados: &c-" + amount));
        executor.sendMessage(MessageUtils.color("&8└ &7Nueva cantidad de relojes: ").append(getLifeDisplay(newSacrificerLives)));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Receptor: &e" + receiver.getName()));
        executor.sendMessage(MessageUtils.color("&8└ &7Relojes recibidos: &a+" + amount));
        executor.sendMessage(MessageUtils.color("&8└ &7Nueva cantidad de relojes: ").append(getLifeDisplay(newReceiverLives)));
        executor.sendMessage("");

        executor.playSound(executor.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);

        if (!sacrificer.equals(executor)) {
            sacrificer.sendMessage("");
            sacrificer.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lHAS SACRIFICADO RELOJES&r &d&l&k|&r &8&l≪"));
            sacrificer.sendMessage("");
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Has sacrificado: &c-" + amount + " reloj(s)"));
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Tus nueva cantidad de relojes: ").append(getLifeDisplay(newSacrificerLives)));
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Receptor: &e" + receiver.getName()));
            sacrificer.sendMessage("");
            sacrificer.playSound(sacrificer.getLocation(), Sound.ENTITY_WITHER_HURT, 0.6f, 0.8f);
        }

        if (!receiver.equals(executor)) {
            receiver.sendMessage("");
            receiver.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lHAS RECIBIDO RELOJES&r &d&l&k|&r &8&l≪"));
            receiver.sendMessage("");
            receiver.sendMessage(MessageUtils.color("&8└ &7Has recibido: &a+" + amount + " reloj(s)"));
            receiver.sendMessage(MessageUtils.color("&8└ &7Tus nueva cantidad de relojes: ").append(getLifeDisplay(newReceiverLives)));
            receiver.sendMessage(MessageUtils.color("&8└ &7Sacrificado por: &e" + sacrificer.getName()));
            receiver.sendMessage("");
            receiver.playSound(receiver.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        }

        Component anuncio = MessageUtils.color("&8&l≫ &d&l&k|&r &6&lSACRIFICIO DE RELOJES&r &d&l&k|&r &8&l≪ &7" + sacrificer.getName() + " &eha sacrificado &c" + amount + " reloj(s) &epara &a" + receiver.getName());
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(anuncio);
        Bukkit.broadcast(Component.empty());
    }

    private Component getLifeDisplay(int lives) {
        Component display = Component.empty();
        int maxLives = PendulumSettings.getInstance().getVidas();
        for (int i = 0; i < maxLives; i++) {
            display = display.append(i < lives ? Icons.ACTIVE_CLOCK : Icons.INACTIVE_CLOCK);
        }
        return display;
    }

    private boolean isAdmin(Player player) {
        return player.hasPermission("pendulum.admin") ||
                java.util.Arrays.asList(PendulumSettings.getInstance().getOp())
                        .contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }
}
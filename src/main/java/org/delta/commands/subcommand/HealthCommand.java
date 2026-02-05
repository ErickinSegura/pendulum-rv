package org.delta.commands.subcommand;

import net.kyori.adventure.text.Component;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;

public class HealthCommand implements SubCommand {

    @Override
    public String getName() {
        return "health";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            showPlayerHealth(player, player);
            return;
        }

        String subcommand = args[1].toLowerCase();

        switch (subcommand) {
            case "set" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length < 4) {
                        player.sendMessage(MessageUtils.color("&c✘ Uso: /pdl health set <jugador> <cantidad>"));
                        playErrorSound(player);
                    } else {
                        setHealth(args[2], args[3], player);
                    }
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    playErrorSound(player);
                }
            }
            case "reset" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length < 3) {
                        player.sendMessage(MessageUtils.color("&c✘ Uso: /pdl health reset <jugador>"));
                        playErrorSound(player);
                    } else {
                        resetHealth(args[2], player);
                    }
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    playErrorSound(player);
                }
            }
            case "sacrifice" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length < 5) {
                        player.sendMessage(MessageUtils.color("&c✘ Uso: /pdl health sacrifice <sacrificador> <cantidad> <receptor>"));
                        playErrorSound(player);
                    } else {
                        sacrificeHealth(args[2], args[3], args[4], player);
                    }
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    playErrorSound(player);
                }
            }
            default -> {
                Player target = Bukkit.getPlayer(subcommand);
                if (target != null) {
                    showPlayerHealth(player, target);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ El jugador '" + subcommand + "' no está conectado."));
                    playErrorSound(player);
                }
            }
        }
    }

    private void setHealth(String targetName, String amountStr, Player executor) {
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador '" + targetName + "' no está conectado."));
            playErrorSound(executor);
            return;
        }

        double hearts;
        try {
            hearts = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            executor.sendMessage(MessageUtils.color("&c✘ La cantidad debe ser un número válido."));
            playErrorSound(executor);
            return;
        }

        if (hearts < 1 || hearts > 2048) {
            executor.sendMessage(MessageUtils.color("&c✘ La cantidad debe estar entre 1 y 2048 corazones."));
            playErrorSound(executor);
            return;
        }

        double maxHealth = hearts * 2.0;

        AttributeInstance healthAttribute = target.getAttribute(Attribute.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.setBaseValue(maxHealth);

            if (target.getHealth() > maxHealth) {
                target.setHealth(maxHealth);
            }

            playSuccessEffects(target);

            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lVIDA MODIFICADA&r &d&l&k|&r &8&l≪"));
            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8└ &7Jugador: &d" + target.getName()));
            executor.sendMessage(MessageUtils.color("&8└ &7Nueva vida: &d" + hearts + " corazones"));
            executor.sendMessage("");

            if (!target.equals(executor)) {
                target.sendMessage("");
                target.sendMessage(MessageUtils.color("\"&8&l≫ &d&l&k|&r &6&lVIDA ACTUALIZADA&r &d&l&k|&r &8&l≪\""));
                target.sendMessage("");
                target.sendMessage(MessageUtils.color("&8└ &7Tu vida máxima ha sido establecida a: &d" + hearts + " corazones"));
                target.sendMessage(MessageUtils.color("&8└ &7Modificado por: &d" + executor.getName()));
                target.sendMessage("");
            }

        } else {
            executor.sendMessage(MessageUtils.color("&c✘ Error al modificar el atributo de vida."));
            playErrorSound(executor);
        }
    }

    private void resetHealth(String targetName, Player executor) {
        Player target = Bukkit.getPlayer(targetName);
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);

        if (target != null) {
            resetPlayerHealth(target, executor);
        } else if (targetOffline.hasPlayedBefore()) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador debe estar conectado para resetear su vida."));
            playErrorSound(executor);
        } else {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador '" + targetName + "' no existe."));
            playErrorSound(executor);
        }
    }

    private void resetPlayerHealth(Player target, Player executor) {
        AttributeInstance healthAttribute = target.getAttribute(Attribute.MAX_HEALTH);

        if (healthAttribute != null) {
            healthAttribute.setBaseValue(20.0);
            target.setHealth(20.0);

            playResetEffects(target);

            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lVIDA RESETEADA&r &d&l&k|&r &8&l≪"));
            executor.sendMessage("");
            executor.sendMessage(MessageUtils.color("&8└ &7Jugador: &d" + target.getName()));
            executor.sendMessage(MessageUtils.color("&8└ &7Vida restaurada a: &d10 corazones"));
            executor.sendMessage("");

            if (!target.equals(executor)) {
                target.sendMessage("");
                target.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lVIDA RESETEADA&r &d&l&k|&r &8&l≪"));
                target.sendMessage("");
                target.sendMessage(MessageUtils.color("&8└ &7Tu vida ha sido restaurada a: &d10 corazones"));
                target.sendMessage(MessageUtils.color("&8└ &7Reseteado por: &d" + executor.getName()));
                target.sendMessage("");
            }

        } else {
            executor.sendMessage(MessageUtils.color("&c✘ Error al resetear el atributo de vida."));
            playErrorSound(executor);
        }
    }

    private void sacrificeHealth(String sacrificerName, String amountStr, String receiverName, Player executor) {
        Player sacrificer = Bukkit.getPlayer(sacrificerName);
        Player receiver = Bukkit.getPlayer(receiverName);

        if (sacrificer == null) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador sacrificador '" + sacrificerName + "' no está conectado."));
            playErrorSound(executor);
            return;
        }

        if (receiver == null) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador receptor '" + receiverName + "' no está conectado."));
            playErrorSound(executor);
            return;
        }

        if (sacrificer.equals(receiver)) {
            executor.sendMessage(MessageUtils.color("&c✘ Un jugador no puede sacrificarse a sí mismo."));
            playErrorSound(executor);
            return;
        }

        double hearts;
        try {
            hearts = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            executor.sendMessage(MessageUtils.color("&c✘ La cantidad debe ser un número válido."));
            playErrorSound(executor);
            return;
        }

        if (hearts <= 0) {
            executor.sendMessage(MessageUtils.color("&c✘ La cantidad debe ser mayor a 0."));
            playErrorSound(executor);
            return;
        }

        AttributeInstance sacrificerHealthAttr = sacrificer.getAttribute(Attribute.MAX_HEALTH);
        AttributeInstance receiverHealthAttr = receiver.getAttribute(Attribute.MAX_HEALTH);

        if (sacrificerHealthAttr == null || receiverHealthAttr == null) {
            executor.sendMessage(MessageUtils.color("&c✘ Error al obtener los atributos de vida."));
            playErrorSound(executor);
            return;
        }

        double sacrificerMaxHealth = sacrificerHealthAttr.getBaseValue();
        double sacrificerMaxHearts = sacrificerMaxHealth / 2.0;
        double receiverMaxHealth = receiverHealthAttr.getBaseValue();
        double receiverMaxHearts = receiverMaxHealth / 2.0;

        if (hearts >= sacrificerMaxHearts) {
            executor.sendMessage(MessageUtils.color("&c✘ El jugador '" + sacrificerName + "' no tiene suficientes corazones."));
            executor.sendMessage(MessageUtils.color("&c✘ Corazones actuales: &d" + sacrificerMaxHearts + " &c| Intenta sacrificar: &d" + hearts));
            playErrorSound(executor);
            return;
        }

        double newReceiverHearts = receiverMaxHearts + hearts;
        if (newReceiverHearts > 2048) {
            executor.sendMessage(MessageUtils.color("&c✘ El receptor superaría el límite de 2048 corazones."));
            executor.sendMessage(MessageUtils.color("&c✘ Corazones actuales: &d" + receiverMaxHearts + " &c| Resultado: &d" + newReceiverHearts));
            playErrorSound(executor);
            return;
        }

        double newSacrificerMaxHealth = (sacrificerMaxHearts - hearts) * 2.0;
        double newReceiverMaxHealth = newReceiverHearts * 2.0;

        sacrificerHealthAttr.setBaseValue(newSacrificerMaxHealth);
        receiverHealthAttr.setBaseValue(newReceiverMaxHealth);

        if (sacrificer.getHealth() > newSacrificerMaxHealth) {
            sacrificer.setHealth(newSacrificerMaxHealth);
        }

        playSacrificeEffects(sacrificer, receiver);

        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lSACRIFICIO REALIZADO&r &d&l&k|&r &8&l≪"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Sacrificador: &d" + sacrificer.getName()));
        executor.sendMessage(MessageUtils.color("&8└ &7Corazones sacrificados: &c-" + hearts));
        executor.sendMessage(MessageUtils.color("&8└ &7Nueva vida: &d" + (sacrificerMaxHearts - hearts) + " corazones"));
        executor.sendMessage("");
        executor.sendMessage(MessageUtils.color("&8└ &7Receptor: &d" + receiver.getName()));
        executor.sendMessage(MessageUtils.color("&8└ &7Corazones recibidos: &a+" + hearts));
        executor.sendMessage(MessageUtils.color("&8└ &7Nueva vida: &d" + newReceiverHearts + " corazones"));
        executor.sendMessage("");

        if (!sacrificer.equals(executor)) {
            sacrificer.sendMessage("");
            sacrificer.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lHAS SACRIFICADO VIDA&r &d&l&k|&r &8&l≪"));
            sacrificer.sendMessage("");
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Has sacrificado: &c-" + hearts + " corazones"));
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Tu nueva vida: &d" + (sacrificerMaxHearts - hearts) + " corazones"));
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Receptor: &d" + receiver.getName()));
            sacrificer.sendMessage(MessageUtils.color("&8└ &7Ejecutado por: &d" + executor.getName()));
            sacrificer.sendMessage("");
        }

        if (!receiver.equals(executor)) {
            receiver.sendMessage("");
            receiver.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lHAS RECIBIDO VIDA&r &d&l&k|&r &8&l≪"));
            receiver.sendMessage("");
            receiver.sendMessage(MessageUtils.color("&8└ &7Has recibido: &a+" + hearts + " corazones"));
            receiver.sendMessage(MessageUtils.color("&8└ &7Tu nueva vida: &d" + newReceiverHearts + " corazones"));
            receiver.sendMessage(MessageUtils.color("&8└ &7Sacrificado por: &d" + sacrificer.getName()));
            receiver.sendMessage(MessageUtils.color("&8└ &7Ejecutado por: &d" + executor.getName()));
            receiver.sendMessage("");
        }

        Component anuncio = MessageUtils.color("&8&l≫ &d&l&k|&r &6&lSACRIFICIO DE CORAZONES&r &d&l&k|&r &8&l≪ &7" + sacrificer.getName() + " &eha sacrificado &c" + hearts + " corazon(es) &epara &a" + receiver.getName());
        Bukkit.broadcast(Component.empty());
        Bukkit.broadcast(anuncio);
        Bukkit.broadcast(Component.empty());
    }

    private void showPlayerHealth(Player viewer, Player target) {
        AttributeInstance healthAttribute = target.getAttribute(Attribute.MAX_HEALTH);

        if (healthAttribute == null) {
            viewer.sendMessage(MessageUtils.color("&c✘ No se pudo obtener la información de vida."));
            playErrorSound(viewer);
            return;
        }

        double maxHealth = healthAttribute.getBaseValue();
        double currentHealth = target.getHealth();
        double hearts = maxHealth / 2.0;

        viewer.playSound(viewer.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        viewer.sendMessage("");
        viewer.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lINFO DE VIDA&r &d&l&k|&r &8&l≪"));
        viewer.sendMessage("");
        viewer.sendMessage(MessageUtils.color("&8└ &7Jugador: &d" + target.getName()));
        viewer.sendMessage(MessageUtils.color("&8└ &7Vida máxima: &d" + hearts + " corazones"));

        String healthBar = generateHealthBar(currentHealth, maxHealth);
        viewer.sendMessage(MessageUtils.color("&8└ &7" + healthBar));
        viewer.sendMessage("");

        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }

    private String generateHealthBar(double current, double max) {
        int barLength = 20;
        int progress = (int) ((current / max) * barLength);

        StringBuilder bar = new StringBuilder();
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("&c▰");
            } else {
                bar.append("&7▱");
            }
        }

        int percentage = (int) ((current / max) * 100);
        return bar + " &f" + percentage + "%";
    }

    private void playSuccessEffects(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 2.0f);

        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.HEART, loc, 10, 0.5, 0.5, 0.5, 0.1);
        player.getWorld().spawnParticle(Particle.TOTEM_OF_UNDYING, loc, 15, 0.3, 0.5, 0.3, 0.1);
    }

    private void playResetEffects(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.7f, 1.0f);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, 1.2f);

        Location loc = player.getLocation().add(0, 1, 0);
        player.getWorld().spawnParticle(Particle.ENCHANT, loc, 20, 0.5, 0.5, 0.5, 1);
    }

    private void playSacrificeEffects(Player sacrificer, Player receiver) {
        sacrificer.playSound(sacrificer.getLocation(), Sound.ENTITY_WITHER_HURT, 0.6f, 0.8f);
        sacrificer.playSound(sacrificer.getLocation(), Sound.BLOCK_PORTAL_AMBIENT, 0.4f, 1.5f);

        Location sacrificerLoc = sacrificer.getLocation().add(0, 1, 0);
        sacrificer.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR, sacrificerLoc, 15, 0.5, 0.5, 0.5, 0.1);
        sacrificer.getWorld().spawnParticle(Particle.SMOKE, sacrificerLoc, 20, 0.3, 0.5, 0.3, 0.05);

        receiver.playSound(receiver.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.2f);
        receiver.playSound(receiver.getLocation(), Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.8f);

        Location receiverLoc = receiver.getLocation().add(0, 1, 0);
        receiver.getWorld().spawnParticle(Particle.HEART, receiverLoc, 20, 0.5, 0.5, 0.5, 0.1);
        receiver.getWorld().spawnParticle(Particle.HAPPY_VILLAGER, receiverLoc, 15, 0.5, 0.5, 0.5, 0.1);
        receiver.getWorld().spawnParticle(Particle.ENCHANTED_HIT, receiverLoc, 10, 0.3, 0.5, 0.3, 0.1);
    }

    private void playErrorSound(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.5f, 1.0f);
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
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DE VIDA&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl health &7- Ver tu propia vida"));
        player.sendMessage(MessageUtils.color("&d/pdl health <jugador> &7- Ver la vida de un jugador"));

        if (checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&d/pdl health set <jugador> <cantidad> &7- Establecer vida &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl health reset <jugador> &7- Resetear vida a 10 corazones &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl health sacrifice <sacrificador> <cantidad> <receptor> &7- Sacrificar vida &8(Admin)"));
        }

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Rango válido: &d1-2048 corazones"));
        player.sendMessage("");
        playErrorSound(player);
    }
}
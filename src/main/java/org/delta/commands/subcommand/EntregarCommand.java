package org.delta.commands.subcommand;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.libs.Reto;
import org.delta.libs.RetoItem;

public class EntregarCommand implements SubCommand {

    @Override
    public String getName() {
        return "entregar";
    }

    @Override
    public void execute(Player player, String[] args) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto reto = settings.getRetoActual();

        // Verificar que hay un reto activo
        if (reto == null) {
            player.sendMessage(MessageUtils.color("&c✘ No hay un reto activo en este momento."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Verificar si ya entregó el reto
        Objective retoObj = player.getScoreboard().getObjective("reto");
        if (retoObj == null) {
            retoObj = player.getScoreboard().registerNewObjective("reto", "dummy", "Reto Completado");
        }

        Score score = retoObj.getScore(player.getName());
        if (score.getScore() > 0) {
            player.sendMessage(MessageUtils.color("&c✘ Ya has entregado tu reto."));
            player.sendMessage(MessageUtils.color("&7Espera al siguiente bloque para un nuevo reto."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Verificar si completó el reto
        if (!reto.verificarCompletado(player)) {
            player.sendMessage(MessageUtils.color("&c✘ Aún no has completado el reto."));
            player.sendMessage(MessageUtils.color("&7Progreso actual: &d" + reto.obtenerProgreso(player)));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Si es un reto de items, consumir los items
        if (reto instanceof RetoItem) {
            RetoItem retoItem = (RetoItem) reto;
            if (!consumirItems(player, retoItem)) {
                player.sendMessage(MessageUtils.color("&c✘ No tienes suficientes items en tu inventario."));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
                return;
            }
        }

        // Marcar como entregado
        score.setScore(1);

        // Dar premio
        if (settings.getStackPremio() != null && settings.getStackPremio().getType() != Material.AIR) {
            player.getInventory().addItem(settings.getStackPremio());
        }

        // Efectos visuales y sonoros
        reproducirEfectos(player);

        // Mensajes al jugador
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &a&l✔ RETO COMPLETADO &6&l&k|||&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7¡Felicidades! Has completado el reto:"));
        player.sendMessage(MessageUtils.color("&8└ &d" + reto.getTitulo()));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7Recompensa recibida:"));
        player.sendMessage(MessageUtils.color("&8└ &a" + settings.getPremio()));
        player.sendMessage("");

        // Anuncio global
        anunciarEntrega(player, reto);
    }

    private boolean consumirItems(Player player, RetoItem retoItem) {
        Material material = retoItem.getMaterial();
        int cantidadNecesaria = retoItem.getCantidad();
        int cantidadEncontrada = 0;

        // Contar items
        for (org.bukkit.inventory.ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                cantidadEncontrada += item.getAmount();
            }
        }

        if (cantidadEncontrada < cantidadNecesaria) {
            return false;
        }

        // Consumir items
        int restante = cantidadNecesaria;
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            org.bukkit.inventory.ItemStack item = player.getInventory().getItem(i);
            if (item != null && item.getType() == material) {
                if (item.getAmount() <= restante) {
                    restante -= item.getAmount();
                    player.getInventory().setItem(i, null);
                } else {
                    item.setAmount(item.getAmount() - restante);
                    restante = 0;
                }

                if (restante == 0) break;
            }
        }

        return true;
    }

    private void reproducirEfectos(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");

        // Sonido de logro
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        // Explosión de partículas inicial
        world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                loc.clone().add(0, 1, 0), 100, 0.5, 1, 0.5, 0.1);

        world.spawnParticle(Particle.HAPPY_VILLAGER,
                loc.clone().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);

        // Espiral ascendente de partículas
        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }

                // Espiral doble
                for (int i = 0; i < 2; i++) {
                    double offsetAngle = angle + (i * Math.PI);
                    double x = Math.cos(offsetAngle) * 0.8;
                    double z = Math.sin(offsetAngle) * 0.8;
                    double y = ticks * 0.08;

                    Location particleLoc = loc.clone().add(x, y, z);
                    world.spawnParticle(Particle.EFFECT, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ENCHANT, particleLoc, 2, 0, 0, 0, 0.5);
                }

                angle += Math.PI / 10;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        // Círculo de partículas en el suelo
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }

                double radius = 2.0 * (ticks / 40.0);
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = loc.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);

        // Lluvia de partículas desde arriba
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    double x = (Math.random() - 0.5) * 2;
                    double z = (Math.random() - 0.5) * 2;
                    double y = 3 + Math.random() * 2;

                    Location particleLoc = loc.clone().add(x, y, z);
                    world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0, -0.5, 0, 0.1);
                    world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, -0.3, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 2L);
    }

    private void anunciarEntrega(Player player, Reto reto) {

        // Anunciar a todos los jugadores online
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage("");
            online.sendMessage(MessageUtils.color("&8&m                                                    "));
            online.sendMessage(MessageUtils.color(
                    "&8[&6&l!&8] &d" + player.getName() + " &7ha completado el reto: &a" + reto.getTitulo()
            ));
            online.sendMessage(MessageUtils.color("&8&m                                                    "));
            online.sendMessage("");

            // Sonido para todos excepto el que entregó
            if (!online.equals(player)) {
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.5f);
            }
        }
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }
}
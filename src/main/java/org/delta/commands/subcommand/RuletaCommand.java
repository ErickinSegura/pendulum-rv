package org.delta.commands.subcommand;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.libs.Reto;
import org.delta.listeners.RetoListener;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class RuletaCommand implements SubCommand {

    @Override
    public String getName() {
        return "ruleta";
    }

    @Override
    public void execute(Player player, String[] args) {

        PendulumSettings settings = PendulumSettings.getInstance();
        Reto[] retos = settings.getRetosDisponibles();

        if (retos == null || retos.length == 0) {
            player.sendMessage(MessageUtils.color("&c✘ No hay retos disponibles."));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            return;
        }

        // Mensaje inicial
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &d&lGIRANDO LA RULETA&r &6&l&k|||&r &8&l≪"));
        player.sendMessage("");

        // Anuncio global
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.sendMessage(MessageUtils.color("&8[&6&l!&8] &d" + player.getName() + " &7está girando la ruleta de retos..."));
            online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_HARP, 1f, 0.8f);
        }

        // Animación de ruleta
        animarRuleta(player, retos);
    }

    private void animarRuleta(Player player, Reto[] retos) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");
        Random random = new Random();

        // Elegir el ganador de antemano
        int indiceGanador = random.nextInt(retos.length);
        Reto retoGanador = retos[indiceGanador];

        new BukkitRunnable() {
            int ticks = 0;
            int currentIndex = 0;
            int delayActual = 2;
            int totalSpins = 50 + random.nextInt(30); // Entre 50 y 80 giros
            int targetIndex = indiceGanador;

            @Override
            public void run() {
                if (ticks >= totalSpins) {
                    // Finalizar ruleta
                    finalizarRuleta(player, retoGanador, indiceGanador);
                    cancel();
                    return;
                }

                // Asegurar que termine en el índice ganador
                if (ticks == totalSpins - 1) {
                    currentIndex = targetIndex;
                } else {
                    currentIndex = (currentIndex + 1) % retos.length;
                }

                // Mostrar reto actual en el chat
                mostrarRetoActual(player, retos[currentIndex], ticks, totalSpins);

                // Mostrar barra de progreso
                mostrarRetoRuleta(player, ticks, totalSpins);

                // Efectos de sonido dinámicos
                reproducirSonidoRuleta(player, ticks, totalSpins);

                // Partículas durante el giro
                if (ticks % 3 == 0) {
                    Location loc = player.getLocation().add(0, 2, 0);
                    player.getWorld().spawnParticle(Particle.PORTAL, loc, 5, 0.3, 0.3, 0.3, 0.02);
                }

                // Efectos especiales en momentos clave
                if (ticks == (int)(totalSpins * 0.75)) {
                    player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 0.3f, 1.8f);
                }

                // Actualizar delay para el próximo tick
                delayActual = calcularDelay(ticks, totalSpins);

                ticks++;
            }

            private int calcularDelay(int tick, int total) {
                double progress = (double)tick / total;

                if (progress < 0.3) {
                    return 2; // Rápido al inicio
                } else if (progress < 0.5) {
                    return 3; // Acelera un poco
                } else if (progress < 0.7) {
                    return 5; // Empieza a desacelerar
                } else if (progress < 0.85) {
                    return 8; // Más lento
                } else if (progress < 0.95) {
                    return 12; // Muy lento
                } else {
                    return 15; // Casi parado
                }
            }

            private void mostrarRetoActual(Player player, Reto reto, int tick, int total) {
                double progress = (double)tick / total;
                String color;
                String simbolo;

                // Cambiar color y símbolo según el progreso
                if (progress < 0.3) {
                    color = "§a§l"; // Verde (rápido)
                    simbolo = "⚡";
                } else if (progress < 0.6) {
                    color = "§e§l"; // Amarillo (medio)
                    simbolo = "⭐";
                } else if (progress < 0.85) {
                    color = "§6§l"; // Naranja (lento)
                    simbolo = "◆";
                } else {
                    color = "§c§l"; // Rojo (muy lento)
                    simbolo = "✦";
                }

                // Animación de caracteres aleatorios (efecto matriz)
                String titulo = reto.getTitulo();
                String tituloAnimado;

                if (progress < 0.8) {
                    // Efecto de texto aleatorio al inicio
                    int charsAleatorios = (int)((1 - progress) * titulo.length());
                    StringBuilder sb = new StringBuilder();
                    Random rand = new Random();
                    String chars = "▓▒░█▄▀■□▪▫";

                    for (int i = 0; i < titulo.length(); i++) {
                        if (i < charsAleatorios && rand.nextBoolean()) {
                            sb.append(chars.charAt(rand.nextInt(chars.length())));
                        } else {
                            sb.append(titulo.charAt(i));
                        }
                    }
                    tituloAnimado = sb.toString();
                } else {
                    // Al final, mostrar texto claro
                    tituloAnimado = titulo;
                }

                // Title principal con el reto
                String titleText = color + simbolo + " " + tituloAnimado + " " + simbolo;

                // Subtitle con indicador de velocidad
                String velocidad;
                if (progress < 0.3) {
                    velocidad = "§a§l⚡⚡⚡ RÁPIDO ⚡⚡⚡";
                } else if (progress < 0.6) {
                    velocidad = "§e§l⚡⚡ MEDIO ⚡⚡";
                } else if (progress < 0.9) {
                    velocidad = "§6§l⚡ DESACELERANDO... ⚡";
                } else {
                    velocidad = "§c§l✦ CASI... ✦";
                }

                // Método compatible con Spigot/Bukkit
                player.sendTitle(titleText, velocidad, 0, 10, 5);
            }

            private void reproducirSonidoRuleta(Player player, int tick, int total) {
                double progress = (double)tick / total;

                // Pitch aumenta con el progreso (más agudo = más lento)
                float pitch = 0.5f + (float)progress * 1.8f;

                // Volumen disminuye al final para crear tensión
                float volume = progress > 0.9 ? 0.4f : 0.6f;

                // Diferentes sonidos según la etapa
                if (progress < 0.7) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, volume, pitch);
                } else if (progress < 0.9) {
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, volume, pitch);
                } else {
                    // Al final, tick más dramático
                    player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, volume, pitch);
                }
            }
        }.runTaskTimer(plugin, 0L, 2L); // Empieza inmediatamente, actualiza cada 2 ticks
    }

    private void mostrarRetoRuleta(Player player, int tick, int total) {
        // Calcular progreso visual
        int barLength = 30;
        int progress = (int)((tick / (float)total) * barLength);

        StringBuilder bar = new StringBuilder("&8[");
        for (int i = 0; i < barLength; i++) {
            if (i < progress) {
                bar.append("&d▰");
            } else {
                bar.append("&7▱");
            }
        }
        bar.append("&8]");


        // Action bar para progreso
        player.sendActionBar(MessageUtils.color(bar.toString()));
    }

    private void finalizarRuleta(Player player, Reto retoGanador, int indice) {
        // NUEVO: Resetear todos los retos antes de actualizar
        resetearTodosLosRetos();

        // Actualizar el config
        actualizarConfig(indice);

        // Recargar settings
        PendulumSettings.getInstance().load();

        // Efectos visuales y sonoros
        Location loc = player.getLocation();
        World world = player.getWorld();

        // Sonido de victoria
        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        // Partículas
        world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 2, 0), 50, 0.5, 0.5, 0.5, 0.2);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);


        // Mensaje detallado
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&l&k|||&r &a&lRESULTADO&r &6&l&k|||&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&7El nuevo reto es:"));
        player.sendMessage(MessageUtils.color("&8└ &d&l" + retoGanador.getTitulo()));
        player.sendMessage(MessageUtils.color("&8└ &eRetos reseteados para todos los jugadores"));
        player.sendMessage("");

        // Anuncio global

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.equals(player)) {
                online.sendMessage("");
                online.sendMessage(MessageUtils.color("&8&m                                                    "));
                online.sendMessage(MessageUtils.color(
                        "&8[&6&l!&8] &7Nuevo reto: &d" + retoGanador.getTitulo()
                ));
                online.sendMessage(MessageUtils.color("&7¡Los retos han sido reseteados para todos!"));
                online.sendMessage(MessageUtils.color("&8&m                                                    "));
                online.sendMessage("");
                online.playSound(online.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.7f, 1.5f);
            }
        }
    }

    /**
     * Resetea los retos de todos los jugadores (online y offline)
     */
    private void resetearTodosLosRetos() {
        // Resetear jugadores en línea
        for (Player online : Bukkit.getOnlinePlayers()) {
            Objective retoObj = online.getScoreboard().getObjective("reto");
            if (retoObj != null) {
                retoObj.getScore(online.getName()).setScore(0);
            }
        }

        // Resetear jugadores offline
        Scoreboard mainScoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Objective mainRetoObj = mainScoreboard.getObjective("reto");

        if (mainRetoObj != null) {
            for (OfflinePlayer offline : Bukkit.getOfflinePlayers()) {
                if (!offline.isOnline() && mainRetoObj.getScore(offline.getName()).isScoreSet()) {
                    mainRetoObj.getScore(offline.getName()).setScore(0);
                }
            }
        }

        // Resetear el registro de completados en el listener
        RetoListener listener = RetoListener.getInstance();
        if (listener != null) {
            listener.resetearCompletados();
        }

        Bukkit.getLogger().info("[Pendulum] Retos reseteados para todos los jugadores por la ruleta.");
    }

    private void actualizarConfig(int nuevoIndice) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");
        File file = new File(plugin.getDataFolder(), "settings.yml");

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("reto.retoActualIndex", nuevoIndice);
            config.save(file);

            Bukkit.getLogger().info("[Pendulum] Reto actualizado a índice: " + nuevoIndice);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[Pendulum] Error al actualizar el config:");
            e.printStackTrace();
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
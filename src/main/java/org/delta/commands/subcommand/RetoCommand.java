package org.delta.commands.subcommand;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.Icons;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.libs.reto.Reto;
import org.delta.libs.reto.RetoItem;
import org.delta.managers.reto.*;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Random;

public class RetoCommand implements SubCommand {

    private final RetoManager retoManager;
    private final RetoRewardManager rewardManager;
    private final RetoEffectsManager effectsManager;
    private final RetoNotificationManager notificationManager;

    public RetoCommand() {
        this.retoManager = RetoManager.getInstance();
        this.rewardManager = RetoRewardManager.getInstance();
        this.effectsManager = RetoEffectsManager.getInstance();
        this.notificationManager = RetoNotificationManager.getInstance();
    }

    @Override
    public String getName() {
        return "reto";
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length == 1) {
            mostrarInfoReto(player);
            return;
        }

        String subcomando = args[1].toLowerCase();

        switch (subcomando) {
            case "entregar" -> entregarReto(player);
            case "lista" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    mostrarListaCompletados(player);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    effectsManager.reproducirSonidoError(player);
                }
            }
            case "reset" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    if (args.length == 2) {
                        resetearTodos(player);
                    } else {
                        resetearJugador(args[2], player);
                    }
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    effectsManager.reproducirSonidoError(player);
                }
            }
            case "ruleta" -> {
                if (!requiresPermission() || checkPermission(player)) {
                    girarRuleta(player);
                } else {
                    player.sendMessage(MessageUtils.color("&c✘ No tienes permisos para este comando."));
                    effectsManager.reproducirSonidoError(player);
                }
            }
            default -> {
                player.sendMessage(MessageUtils.color("&c✘ Subcomando no reconocido."));
                showUsage(player);
            }
        }
    }


    private void mostrarInfoReto(Player player) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto reto = settings.getRetoActual();

        if (reto == null) {
            player.sendMessage(MessageUtils.color("&c¡No hay un reto activo!"));
            return;
        }

        boolean retoCumplido = retoManager.verificarCompletado(player);

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lDETALLES DEL RETO&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");

        sendRetoDetail(player, "", "Desafío", reto.getTitulo());
        sendRetoDetail(player, "", "Recompensa", rewardManager.obtenerDescripcionPremio());
        sendRetoDetail(player, "", "Castigo", rewardManager.obtenerDescripcionCastigo());

        player.sendMessage("");
        String progreso = retoManager.obtenerProgreso(player);
        player.sendMessage(MessageUtils.color("&8└ &7Progreso: &d" + progreso));

        player.sendMessage("");
        if (retoCumplido) {
            player.sendMessage(MessageUtils.color("&8└ &7Estado: &a✔ Completado"));
            player.sendMessage(MessageUtils.color("&8   &7¡Felicitaciones por completar el reto!"));
        } else {
            player.sendMessage(MessageUtils.color("&8└ &7Estado: &c✘ Pendiente"));
            player.sendMessage(MessageUtils.color("&8   &7Usa &d/pdl reto entregar &7cuando completes el reto"));
        }

        player.sendMessage("");

        float pitch = retoCumplido ? 1.5f : 1.0f;
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, pitch);
    }

    private void mostrarListaCompletados(Player player) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto reto = settings.getRetoActual();

        if (reto == null) {
            player.sendMessage(MessageUtils.color("&c✘ No hay un reto activo."));
            effectsManager.reproducirSonidoError(player);
            return;
        }

        List<String> completados = retoManager.obtenerJugadoresCompletados();

        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1.2f);

        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &6&lJUGADORES QUE COMPLETARON EL RETO &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8└ &d" + reto.getTitulo()));
        player.sendMessage("");

        if (completados.isEmpty()) {
            player.sendMessage(MessageUtils.color("&8   &7Ningún jugador ha completado el reto aún."));
        } else {
            player.sendMessage(MessageUtils.color("&8   &7Total: &d" + completados.size() + " jugador(es)"));
            player.sendMessage("");

            for (int i = 0; i < completados.size(); i++) {
                String jugador = completados.get(i);
                String prefijo = (i == completados.size() - 1) ? "&8   └ " : "&8   ├ ";
                player.sendMessage(MessageUtils.color(prefijo + "&d" + jugador));
            }
        }

        player.sendMessage("");
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 0.5f, 1.0f);
    }

    private void entregarReto(Player player) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto reto = settings.getRetoActual();

        if (reto == null) {
            notificationManager.enviarMensajeNoActivo(player);
            return;
        }

        if (retoManager.yaEntrego(player)) {
            notificationManager.enviarMensajeYaEntregado(player);
            return;
        }

        if (!retoManager.verificarCompletado(player)) {
            String progreso = retoManager.obtenerProgreso(player);
            notificationManager.enviarMensajeNoCompletado(player, progreso);
            return;
        }

        if (reto instanceof RetoItem retoItem) {
            if (!retoManager.consumirItems(player, retoItem)) {
                notificationManager.enviarMensajeItemsInsuficientes(player);
                return;
            }
        }

        retoManager.marcarComoEntregado(player);

        rewardManager.otorgarRecompensa(player);

        effectsManager.reproducirEfectosCompletado(player);

        notificationManager.enviarMensajeCompletado(player, reto.getTitulo(),
                rewardManager.obtenerDescripcionPremio());

        notificationManager.anunciarEntrega(player, reto);
    }

    private void resetearTodos(Player executor) {
        RetoManager.ResetResult result = retoManager.resetearTodos();

        notificationManager.enviarMensajeResetExitoso(executor,
                result.getTotal(), result.getOnline(), result.getOffline());

        notificationManager.anunciarResetGlobal(executor);
    }

    private void resetearJugador(String targetName, Player executor) {
        Player targetOnline = Bukkit.getPlayer(targetName);
        OfflinePlayer targetOffline = Bukkit.getOfflinePlayer(targetName);

        boolean reseteado = false;

        if (targetOnline != null) {
            retoManager.resetearJugador(targetOnline);
            reseteado = true;

            notificationManager.enviarMensajeResetIndividual(executor, targetName, true);

            notificationManager.notificarResetIndividual(targetOnline, executor);
        }
        else if (targetOffline.hasPlayedBefore()) {
            retoManager.resetearJugadorOffline(targetOffline);
            reseteado = true;

            notificationManager.enviarMensajeResetIndividual(executor, targetName, false);
        } else {
            notificationManager.enviarMensajeJugadorNoEncontrado(executor, targetName);
            return;
        }

        if (!reseteado) {
            notificationManager.enviarMensajeErrorReset(executor, targetName);
        }
    }

    private void girarRuleta(Player player) {
        PendulumSettings settings = PendulumSettings.getInstance();
        Reto[] retos = settings.getRetosDisponibles();
        String[] castigos = settings.getCastigos();

        if (retos == null || retos.length == 0) {
            player.sendMessage(MessageUtils.color("&c✘ No hay retos disponibles."));
            effectsManager.reproducirSonidoError(player);
            return;
        }

        if (castigos == null || castigos.length == 0) {
            player.sendMessage(MessageUtils.color("&c✘ No hay castigos disponibles."));
            effectsManager.reproducirSonidoError(player);
            return;
        }

        notificationManager.anunciarRuleta(player);

        animarRuleta(player, retos, castigos);
    }

    private void animarRuleta(Player player, Reto[] retos, String[] castigos) {
        Random random = new Random();

        int indiceRetoGanador = random.nextInt(retos.length);
        int indiceCastigoGanador = random.nextInt(castigos.length);
        Reto retoGanador = retos[indiceRetoGanador];
        String castigoGanador = castigos[indiceCastigoGanador];

        World world = player.getWorld();
        long originalTime = world.getTime();

        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            animarRuletaParaJugador(onlinePlayer, world, originalTime);
        }

        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");

        final int CLOCK_CYCLES = 2;
        final long TICKS_PER_FRAME = 1L;
        final int framesPerCycle = 64;
        final int totalFrames = framesPerCycle * CLOCK_CYCLES;

        long totalTicks = totalFrames * TICKS_PER_FRAME;

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            finalizarRuleta(player, retoGanador, castigoGanador, indiceRetoGanador, indiceCastigoGanador);
        }, totalTicks);
    }

    private void animarRuletaParaJugador(Player viewer, World world, long originalTime) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");
        Random random = new Random();

        final int CLOCK_CYCLES = 2;
        final long TICKS_PER_FRAME = 1L;
        final int framesPerCycle = 64;
        final int totalFrames = framesPerCycle * CLOCK_CYCLES;
        final long totalAnimationTicks = totalFrames * TICKS_PER_FRAME;
        final long timePerTick = (24000L * CLOCK_CYCLES) / totalAnimationTicks;

        Component subtitle = MessageUtils.color("&dGirando la ruleta...");

        for (int i = 0; i < totalFrames; i++) {
            final int frameIndex = i % 64;

            final long delay = i * TICKS_PER_FRAME;
            final int frameNumber = i;

            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (viewer.isOnline()) {
                    Component leftItem = Icons.getRandomItem(random);
                    Component clock = Icons.getClockFrame(frameIndex);
                    Component rightItem = Icons.getRandomItem(random);

                    Component titleComponent = Component.text()
                            .append(leftItem)
                            .append(Component.text("  "))
                            .append(clock)
                            .append(Component.text("  "))
                            .append(rightItem)
                            .build();

                    Title title = Title.title(
                            titleComponent,
                            subtitle,
                            Title.Times.times(
                                    Duration.ZERO,
                                    Duration.ofMillis(150),
                                    Duration.ZERO
                            )
                    );

                    viewer.showTitle(title);

                    long newTime = (originalTime + (timePerTick * frameNumber * 2)) % 24000;
                    world.setTime(newTime);

                    if (frameNumber % 3 == 0) {
                        Location loc = viewer.getLocation().add(0, 2, 0);
                        world.spawnParticle(Particle.PORTAL, loc, 5, 0.3, 0.3, 0.3, 0.02);
                    }

                    double progress = (double) frameNumber / totalFrames;
                    float pitch = 0.5f + (float) progress * 1.8f;
                    float volume = progress > 0.9 ? 0.4f : 0.6f;

                    if (progress < 0.7) {
                        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, volume, pitch);
                    } else if (progress < 0.9) {
                        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASEDRUM, volume, pitch);
                    } else {
                        viewer.playSound(viewer.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, volume, pitch);
                    }
                }
            }, delay);
        }

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (viewer.isOnline()) {
                world.setTime(originalTime);

                Component finalSubtitle = MessageUtils.color("&a¡Reto seleccionado!");

                Title finalTitle = Title.title(
                        Icons.ACTIVE_CLOCK,
                        finalSubtitle,
                        Title.Times.times(
                                Duration.ZERO,
                                Duration.ofSeconds(3),
                                Duration.ofMillis(600)
                        )
                );

                viewer.showTitle(finalTitle);
            }
        }, totalAnimationTicks);
    }

    private void finalizarRuleta(Player player, Reto retoGanador, String castigoGanador,
                                 int indiceReto, int indiceCastigo) {
        retoManager.resetearTodos();

        actualizarConfig(indiceReto, indiceCastigo);

        PendulumSettings.getInstance().load();

        effectsManager.reproducirEfectosRuleta(player);

        notificationManager.anunciarResultadoRuleta(player,
                retoGanador.getTitulo(), castigoGanador);
    }

    private void actualizarConfig(int nuevoIndiceReto, int nuevoIndiceCastigo) {
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");
        File file = new File(plugin.getDataFolder(), "settings.yml");

        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            config.set("reto.retoActualIndex", nuevoIndiceReto);
            config.set("reto.castigoActualIndex", nuevoIndiceCastigo);
            config.save(file);

            Bukkit.getLogger().info("[Pendulum] Reto actualizado a índice nuevo: " + nuevoIndiceReto);
            Bukkit.getLogger().info("[Pendulum] Castigo actualizado a índice: " + nuevoIndiceCastigo);
        } catch (IOException e) {
            Bukkit.getLogger().severe("[Pendulum] Error al actualizar el config:");
            e.printStackTrace();
        }
    }

    private void sendRetoDetail(Player player, String icon, String label, String value) {
        player.sendMessage(MessageUtils.color("&8└ " + icon + " &7" + label + ": &d" + value));
    }

    private boolean checkPermission(Player player) {
        String[] ops = PendulumSettings.getInstance().getOp();
        if (ops == null) return false;
        return java.util.Arrays.asList(ops).contains(player.getName());
    }

    @Override
    public boolean requiresPermission() {
        return false;
    }

    @Override
    public void showUsage(Player player) {
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&8&l≫ &d&l&k|&r &6&lCOMANDOS DEL RETO&r &d&l&k|&r &8&l≪"));
        player.sendMessage("");
        player.sendMessage(MessageUtils.color("&d/pdl reto &7- Ver información del reto actual"));
        player.sendMessage(MessageUtils.color("&d/pdl reto entregar &7- Entregar el reto completado"));

        if (checkPermission(player)) {
            player.sendMessage(MessageUtils.color("&d/pdl reto reset [jugador] &7- Resetear retos &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl reto ruleta &7- Girar ruleta de retos &8(Admin)"));
            player.sendMessage(MessageUtils.color("&d/pdl reto lista &7- Ver jugadores que completaron &8(Admin)"));
        }

        player.sendMessage("");
        effectsManager.reproducirSonidoError(player);
    }
}
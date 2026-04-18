package org.delta.managers.chargebase;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.MessageUtils;
import org.delta.pendulum;

import java.time.LocalTime;
import java.util.Random;

public class ChargeBaseManager {

    private static final int MAP_BOUND = 4000;
    private static final double INITIAL_RADIUS = 150.0;
    private static final long DURATION_TICKS = 72000L;   // 1 hora
    private static final long SHRINK_INTERVAL = 1200L;   // cada minuto
    private long startTimeTicks;

    private final pendulum plugin;
    private ChargeBaseZone activeZone = null;
    private boolean active = false;

    public ChargeBaseManager(pendulum plugin) {
        this.plugin = plugin;
        scheduleTrigger();
    }

    private void scheduleTrigger() {
        new BukkitRunnable() {
            @Override
            public void run() {
                LocalTime now = LocalTime.now();
                boolean isEventHour = (now.getHour() == 10 || now.getHour() == 22);
                boolean isOnTime = now.getMinute() == 0 && now.getSecond() < 2;

                if (isEventHour && isOnTime && !active) {
                    startEvent();
                }
            }
        }.runTaskTimer(plugin, 0L, 20L); // checa cada segundo
    }

    private void startEvent() {
        active = true;
        startTimeTicks = plugin.getServer().getCurrentTick();

        World world = Bukkit.getWorlds().get(0);
        Random rng = new Random();
        int x = rng.nextInt(MAP_BOUND * 2) - MAP_BOUND;
        int z = rng.nextInt(MAP_BOUND * 2) - MAP_BOUND;
        int y = world.getHighestBlockYAt(x, z);

        activeZone = new ChargeBaseZone(new Location(world, x, y, z), INITIAL_RADIUS);

        Bukkit.broadcastMessage("§d§l[Pendulum] §rBase de Carga activa en §e" + x + ", " + z);

        startShrinking();
        startParticles();

        new BukkitRunnable() {
            @Override
            public void run() { endEvent(); }
        }.runTaskLater(plugin, DURATION_TICKS);

    }

    private void startShrinking() {
        long steps = DURATION_TICKS / SHRINK_INTERVAL; // 60 pasos
        double shrinkPerStep = INITIAL_RADIUS / steps;

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                activeZone.shrink(shrinkPerStep);
                plugin.getLogger().info("Base de Carga reducida — Radio actual: " + String.format("%.1f", activeZone.getCurrentRadius()) + " bloques");
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (activeZone.isInside(p.getLocation())) {
                        p.sendMessage(MessageUtils.color(
                                "&8[&d&l!&8] &7La zona se redujo — Radio actual: &e" +
                                        String.format("%.1f", activeZone.getCurrentRadius()) + " bloques"
                        ));
                    }
                }

            }
        }.runTaskTimer(plugin, SHRINK_INTERVAL, SHRINK_INTERVAL);
    }

    private void endEvent() {
        active = false;
        activeZone = null;
        Bukkit.broadcastMessage("§d§l[Pendulum] §rLa Base de Carga ha finalizado.");
    }

    private void startParticles() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!active) { cancel(); return; }
                activeZone.spawnParticles();
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void startEventAt(Location loc, double radius) {
        active = true;
        startTimeTicks = plugin.getServer().getCurrentTick();
        activeZone = new ChargeBaseZone(loc, radius);
        Bukkit.getServer().broadcast(MessageUtils.color("&d&l[Pendulum] &rBase de Carga activa en &e" + (int)loc.getX() + ", " + (int)loc.getZ()));        startShrinking();
        startParticles();
        new BukkitRunnable() {
            @Override public void run() { endEvent(); }
        }.runTaskLater(plugin, DURATION_TICKS);
    }

    public void forceEnd() { endEvent(); }

    public long getRemainingTicks() {
        long elapsed = plugin.getServer().getCurrentTick() - startTimeTicks;
        return Math.max(0, DURATION_TICKS - elapsed);
    }

    public String getTimeUntilNext() {
        java.time.LocalTime now = java.time.LocalTime.now();
        java.time.LocalTime next10am = java.time.LocalTime.of(10, 0);
        java.time.LocalTime next10pm = java.time.LocalTime.of(22, 0);

        long secsTo10am = now.until(next10am, java.time.temporal.ChronoUnit.SECONDS);
        long secsTo10pm = now.until(next10pm, java.time.temporal.ChronoUnit.SECONDS);

        if (secsTo10am <= 0) secsTo10am += 86400;
        if (secsTo10pm <= 0) secsTo10pm += 86400;

        long secs = Math.min(secsTo10am, secsTo10pm);
        long hours = secs / 3600;
        long minutes = (secs % 3600) / 60;
        long seconds = secs % 60;

        return hours + "h " + minutes + "m " + seconds + "s";
    }

    public ChargeBaseZone getActiveZone() { return activeZone; }

    public boolean isActive() { return active; }
}
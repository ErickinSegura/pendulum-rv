package org.delta.managers.chargebase;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.customs.mobs.chargebase.atacante.AtacanteBasico;
import org.delta.customs.mobs.chargebase.controlador.ControladorBasico;
import org.delta.customs.mobs.chargebase.defensor.DefensorBasico;
import org.delta.customs.mobs.chargebase.healer.HealerBasico;
import org.delta.customs.mobs.chargebase.hibrido.HibridoBasico;
import org.delta.pendulum;

import java.util.*;

public class ChargeBaseSpawnManager {

    // Cuántos mobs máx activos por clase
    private static final Map<MobClass, Integer> MAX_PER_CLASS = Map.of(
            MobClass.ATACANTE,    6,
            MobClass.DEFENSOR,    4,
            MobClass.HEALER,      3,
            MobClass.CONTROLADOR, 4,
            MobClass.HIBRIDO,     1
    );

    // Cada cuántos ticks spawnea cada clase (20t = 1s)
    private static final Map<MobClass, Long> SPAWN_INTERVAL = Map.of(
            MobClass.ATACANTE,    200L,  // 10s
            MobClass.DEFENSOR,    300L,  // 15s
            MobClass.HEALER,      400L,  // 20s
            MobClass.CONTROLADOR, 300L,  // 15s
            MobClass.HIBRIDO,     1200L  // 60s
    );

    private final pendulum plugin;
    private final ChargeBaseZone zone;

    // UUID -> clase del mob
    private final Map<UUID, MobClass> activeMobs = new HashMap<>();
    // Kills por clase (reduce spawns)
    private final Map<MobClass, Integer> killCount = new EnumMap<>(MobClass.class);
    // Dificultad actual (0.0 - 1.0, sube con el tiempo)
    private double difficulty = 0.0;

    private final List<BukkitRunnable> tasks = new ArrayList<>();

    public ChargeBaseSpawnManager(pendulum plugin, ChargeBaseZone zone) {
        this.plugin = plugin;
        this.zone = zone;
        for (MobClass c : MobClass.values()) killCount.put(c, 0);
    }

    public void start() {
        startDifficultyRamp();
        for (MobClass mobClass : MobClass.values()) {
            startSpawnLoop(mobClass);
        }
    }

    public void stop() {
        tasks.forEach(BukkitRunnable::cancel);
        tasks.clear();
        // Elimina mobs vivos de la zona
        activeMobs.keySet().forEach(uid -> {
            plugin.getServer().getWorlds().stream()
                    .flatMap(w -> w.getEntities().stream())
                    .filter(e -> e.getUniqueId().equals(uid))
                    .findFirst()
                    .ifPresent(e -> e.remove());
        });
        activeMobs.clear();
    }

    private void startSpawnLoop(MobClass mobClass) {
        long interval = SPAWN_INTERVAL.get(mobClass);

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                int activeCount = (int) activeMobs.values().stream()
                        .filter(c -> c == mobClass).count();

                int maxAllowed = getMaxAllowed(mobClass);
                if (activeCount >= maxAllowed) return;

                Location spawnLoc = randomLocationInZone();
                if (spawnLoc == null) return;

                LivingEntity entity = spawnMob(mobClass, spawnLoc);
                if (entity != null) {
                    activeMobs.put(entity.getUniqueId(), mobClass);
                }
            }
        };

        task.runTaskTimer(plugin, interval, interval);
        tasks.add(task);
    }

    private void startDifficultyRamp() {
        // Sube dificultad cada 5 minutos (6000 ticks)
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                difficulty = Math.min(1.0, difficulty + (1.0 / 12)); // 12 pasos en 1 hora
                plugin.getLogger().info("[ChargeBase] Dificultad: " + String.format("%.0f%%", difficulty * 100));
            }
        };
        task.runTaskTimer(plugin, 6000L, 6000L);
        tasks.add(task);
    }

    // Mientras más kills de una clase, menos spawnean (mínimo 1)
    private int getMaxAllowed(MobClass mobClass) {
        int kills = killCount.get(mobClass);
        int base = MAX_PER_CLASS.get(mobClass);
        int reduced = Math.max(1, base - (kills / 5));
        // Dificultad sube el máximo ligeramente
        return (int)(reduced * (1 + difficulty * 0.5));
    }

    private LivingEntity spawnMob(MobClass mobClass, Location loc) {
        return switch (mobClass) {
            case ATACANTE    -> new AtacanteBasico(plugin, loc).build();
            case DEFENSOR    -> new DefensorBasico(plugin, loc).build();
            case HEALER      -> new HealerBasico(plugin, loc).build();
            case CONTROLADOR -> new ControladorBasico(plugin, loc).build();
            case HIBRIDO     -> new HibridoBasico(plugin, loc).build();
        };
    }

    private Location randomLocationInZone() {
        Location center = zone.getCenter();
        double radius = zone.getCurrentRadius();
        Random rng = new Random();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double dist = rng.nextDouble() * radius;
            double x = center.getX() + dist * Math.cos(angle);
            double z = center.getZ() + dist * Math.sin(angle);
            int y = center.getWorld().getHighestBlockYAt((int) x, (int) z);
            Location loc = new Location(center.getWorld(), x, y, z);
            if (zone.isInside(loc)) return loc;
        }
        return null;
    }

    public void registerKill(UUID uid) {
        MobClass mobClass = activeMobs.remove(uid);
        if (mobClass != null) {
            killCount.merge(mobClass, 1, Integer::sum);
        }
    }

    public boolean isManagedMob(UUID uid) {
        return activeMobs.containsKey(uid);
    }

    public MobClass getMobClass(UUID uid) {
        return activeMobs.get(uid);
    }

    public double getDifficulty() { return difficulty; }
    public Map<MobClass, Integer> getKillCount() { return killCount; }
}
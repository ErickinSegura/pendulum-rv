package org.delta.managers.chargebase;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.customs.mobs.chargebase.MobClass;
import org.delta.customs.mobs.chargebase.atacante.AtacanteAvanzado;
import org.delta.customs.mobs.chargebase.atacante.AtacanteBasico;
import org.delta.customs.mobs.chargebase.controlador.ControladorAvanzado;
import org.delta.customs.mobs.chargebase.controlador.ControladorBasico;
import org.delta.customs.mobs.chargebase.defensor.DefensorAvanzado;
import org.delta.customs.mobs.chargebase.defensor.DefensorBasico;
import org.delta.customs.mobs.chargebase.healer.HealerAvanzado;
import org.delta.customs.mobs.chargebase.healer.HealerBasico;
import org.delta.customs.mobs.chargebase.hibrido.HibridoAvanzado;
import org.delta.customs.mobs.chargebase.hibrido.HibridoBasico;
import org.delta.pendulum;

import java.util.*;

public class ChargeBaseSpawnManager {

    private static final Map<MobClass, Integer> MAX_PER_CLASS = Map.of(
            MobClass.ATACANTE,    6,
            MobClass.DEFENSOR,    4,
            MobClass.HEALER,      3,
            MobClass.CONTROLADOR, 4,
            MobClass.HIBRIDO,     1
    );

    private static final Map<MobClass, Long> SPAWN_INTERVAL = Map.of(
            MobClass.ATACANTE,    200L,  // 10s
            MobClass.DEFENSOR,    300L,  // 15s
            MobClass.HEALER,      400L,  // 20s
            MobClass.CONTROLADOR, 300L,  // 15s
            MobClass.HIBRIDO,     1200L     // 60s
    );

    private final pendulum plugin;
    private final ChargeBaseZone zone;

    private final Map<UUID, MobClass> activeMobs = new HashMap<>();
    private final Set<UUID> allSpawned = new HashSet<>();
    private final Map<MobClass, Integer> killCount = new EnumMap<>(MobClass.class);
    private double difficulty = 0.0;
    Random rng = new Random();


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
        allSpawned.forEach(uid -> {
            plugin.getServer().getWorlds().stream()
                    .flatMap(w -> w.getEntities().stream())
                    .filter(e -> e.getUniqueId().equals(uid))
                    .findFirst()
                    .ifPresent(Entity::remove);
        });
        activeMobs.clear();
        allSpawned.clear();
    }

    private void startDifficultyRamp() {
        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                difficulty = Math.min(1.0, difficulty + (1.0 / 12));
                plugin.getLogger().info("[ChargeBase] Dificultad: " + String.format("%.0f%%", difficulty * 100));
                restartSpawnLoops();
            }
        };
        task.runTaskTimer(plugin, 6000L, 6000L);
        tasks.add(task);
    }

    private void restartSpawnLoops() {
        for (int i = 0; i < tasks.size() - 1; i++) {
            tasks.get(i).cancel();
        }
        tasks.subList(0, tasks.size() - 1).clear();

        for (MobClass mobClass : MobClass.values()) {
            startSpawnLoop(mobClass);
        }
    }

    private void startSpawnLoop(MobClass mobClass) {
        long base = SPAWN_INTERVAL.get(mobClass);
        long scaled = (long)(base * Math.max(0.6, 1 - difficulty * 0.4));

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                activeMobs.entrySet().removeIf(entry -> plugin.getServer().getWorlds().stream()
                        .flatMap(w -> w.getEntities().stream())
                        .noneMatch(e -> {
                            Location loc = e.getLocation();
                            if (!loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                                return false;
                            }
                            return e.getUniqueId().equals(entry.getKey());
                        }));

                int activeCount = (int) activeMobs.values().stream()
                        .filter(c -> c == mobClass).count();
                if (activeCount >= getMaxAllowed(mobClass)) return;

                Location spawnLoc = randomLocationInZone();
                if (spawnLoc == null) return;

                LivingEntity entity = spawnMob(mobClass, spawnLoc);
                if (entity != null) {
                    activeMobs.put(entity.getUniqueId(), mobClass);
                    allSpawned.add(entity.getUniqueId());
                }
            }
        };
        task.runTaskTimer(plugin, scaled, scaled);
        tasks.add(task);
    }

    private int getMaxAllowed(MobClass mobClass) {
        int kills = killCount.get(mobClass);
        int base = MAX_PER_CLASS.get(mobClass);
        int reduced = Math.max(1, base - (kills / 5));
        return (int)(reduced * (1 + difficulty * 0.5));
    }

    private LivingEntity spawnMob(MobClass mobClass, Location loc) {
        double advancedChance = 0.05 + (difficulty * 0.45);

        return switch (mobClass) {
            case ATACANTE    -> rng.nextDouble() < advancedChance
                    ? new AtacanteBasico(plugin, loc).build()
                    : new AtacanteAvanzado(plugin, loc).build();
            case DEFENSOR    -> rng.nextDouble() < advancedChance
                    ? new DefensorAvanzado(plugin, loc).build()
                    : new DefensorBasico(plugin, loc).build();
            case HEALER      -> rng.nextDouble() < advancedChance
                    ? new HealerAvanzado(plugin, loc).build()
                    : new HealerBasico(plugin, loc).build();
            case CONTROLADOR -> rng.nextDouble() < advancedChance
                    ? new ControladorAvanzado(plugin, loc).build()
                    : new ControladorBasico(plugin, loc).build();
            case HIBRIDO     -> rng.nextDouble() < advancedChance
                    ? new HibridoBasico(plugin, loc).build()
                    : new HibridoAvanzado(plugin, loc).build();
        };
    }

    private void applyDifficultyScaling(LivingEntity entity) {
        if (difficulty <= 0) return;

        double maxHp = entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        double newHp = maxHp * (1 + difficulty * 0.5);
        entity.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).setBaseValue(newHp);
        entity.setHealth(newHp);

        var speedAttr = entity.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.setBaseValue(speedAttr.getValue() * (1 + difficulty * 0.3));
        }

        var damageAttr = entity.getAttribute(org.bukkit.attribute.Attribute.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.setBaseValue(damageAttr.getValue() * (1 + difficulty * 0.4));
        }
    }

    public void despawnOutsideMobs() {
        activeMobs.entrySet().removeIf(entry -> {
            UUID uid = entry.getKey();
            return plugin.getServer().getWorlds().stream()
                    .flatMap(w -> w.getEntities().stream())
                    .filter(e -> e.getUniqueId().equals(uid))
                    .findFirst()
                    .map(e -> {
                        if (!zone.isInside(e.getLocation())) {
                            e.remove();
                            return true;
                        }
                        return false;
                    })
                    .orElse(true);
        });
    }

    private Location randomLocationInZone() {
        Location center = zone.getCenter();
        double radius = zone.getCurrentRadius();

        for (int attempt = 0; attempt < 10; attempt++) {
            double angle = rng.nextDouble() * 2 * Math.PI;
            double dist = rng.nextDouble() * radius;
            double x = center.getX() + dist * Math.cos(angle);
            double z = center.getZ() + dist * Math.sin(angle);
            int y = center.getWorld().getHighestBlockYAt((int) x, (int) z);
            Location loc = new Location(center.getWorld(), x, y + 1, z);

            if (!loc.getBlock().getType().isSolid() &&
                    !loc.clone().add(0, 1, 0).getBlock().getType().isSolid() &&
                    zone.isInside(loc)) {
                return loc;
            }
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

}
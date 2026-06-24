package org.delta.listeners.spawns;

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class DragonaVacioListener implements Listener {

    private static final int DIA_MINIMO = 10;
    private static final double HEALTH_MULTIPLIER = 1.5;

    private static final long ATTACK_INTERVAL =12L;
    private static final long ATTACK_INTERVAL_ENRAGE = 8L;
    private static final double ENRAGE_THRESHOLD = 0.5;
    private static final long TELEGRAPH_TICKS = 40L;
    private static final int NV_TICKS = 70;
    private static final double RANGE = 64.0;
    private static final Vector CENTER = new Vector(0, 0, 0);

    private static final double PULL_STRENGTH = 1.0;
    private static final int PULL_PULSES = 12;
    private static final long PULL_PULSE_INTERVAL = 4L;

    private static final int LEVITATION_TICKS = 30;
    private static final int LEVITATION_AMP = 20;

    private static final double IMPLOSION_RADIUS = 10.0;
    private static final double IMPLOSION_DAMAGE = 16.0;
    private static final double IMPLOSION_LAUNCH = 1.0;

    private static final int METEOR_COUNT = 6;
    private static final double METEOR_SCATTER = 9.0;
    private static final double METEOR_RADIUS = 3.5;
    private static final double METEOR_DAMAGE = 6.0;
    private static final double METEOR_LAUNCH = 0.4;

    private static final double TELEPORT_RADIUS_CENTRO = 100.0;
    private static final int TELEPORT_RETRIES = 16;

    private static final long CRYSTAL_RESPAWN_DELAY = 1200L;

    private static final String TAG_REFORZADA = "dragona_vacio_reforzada";

    private final pendulum plugin;
    private final PendulumSettings settings = PendulumSettings.getInstance();
    private final Random random = new Random();
    private final Set<UUID> activas = new HashSet<>();

    public DragonaVacioListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAdd(EntityAddToWorldEvent event) {
        if (settings.getDia() < DIA_MINIMO) return;
        if (!(event.getEntity() instanceof EnderDragon dragon)) return;
        if (dragon.getWorld().getEnvironment() != World.Environment.THE_END) return;

        if (!activas.add(dragon.getUniqueId())) return;

        if (!dragon.getScoreboardTags().contains(TAG_REFORZADA)) {
            reforzar(dragon);
            dragon.addScoreboardTag(TAG_REFORZADA);
        }
        iniciarComportamiento(dragon);
        plugin.getLogger().info("[DragonaVacio] Dragona del Vacío enganchada (vida "
                + (int) dragon.getHealth() + ").");
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrystalDamage(EntityDamageEvent event) {
        if (settings.getDia() < DIA_MINIMO) return;
        if (!(event.getEntity() instanceof EnderCrystal crystal)) return;

        World world = crystal.getWorld();
        if (world.getEnvironment() != World.Environment.THE_END) return;
        if (world.getEntitiesByClass(EnderDragon.class).isEmpty()) return;

        Location loc = crystal.getLocation();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (world.getEntitiesByClass(EnderDragon.class).isEmpty()) return;
            world.spawn(loc, EnderCrystal.class, c -> c.setShowingBottom(false));
            world.playSound(loc, Sound.BLOCK_END_PORTAL_SPAWN, 0.6f, 1.5f);
            world.spawnParticle(Particle.END_ROD, loc, 40, 0.3, 0.6, 0.3, 0.05);
        }, CRYSTAL_RESPAWN_DELAY);
    }

    private void reforzar(EnderDragon dragon) {
        AttributeInstance maxHealth = dragon.getAttribute(Attribute.MAX_HEALTH);
        if (maxHealth == null) return;
        maxHealth.setBaseValue(maxHealth.getBaseValue() * HEALTH_MULTIPLIER);
        dragon.setHealth(maxHealth.getValue());
    }

    private void iniciarComportamiento(EnderDragon dragon) {
        new BukkitRunnable() {
            int cooldown = (int) ATTACK_INTERVAL;

            @Override
            public void run() {
                if (dragon.isDead() || !dragon.isValid()) {
                    activas.remove(dragon.getUniqueId());
                    cancel();
                    return;
                }

                if (--cooldown > 0) return;
                cooldown = (int) intervaloActual(dragon);
                ejecutarAtaque(dragon);
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private long intervaloActual(EnderDragon dragon) {
        AttributeInstance max = dragon.getAttribute(Attribute.MAX_HEALTH);
        if (max != null && dragon.getHealth() / max.getValue() < ENRAGE_THRESHOLD) {
            return ATTACK_INTERVAL_ENRAGE;
        }
        return ATTACK_INTERVAL;
    }

    private void ejecutarAtaque(EnderDragon dragon) {
        switch (random.nextInt(5)) {
            case 0 -> tironDelVacio(dragon);
            case 1 -> alientoDelVacio(dragon);
            case 2 -> meteorosDelVacio(dragon);
            case 3 -> implosionDelVacio(dragon);
            default -> teletransporteDelVacio(dragon);
        }
    }

    private void avisar(EnderDragon dragon, float pitch) {
        for (Player player : jugadoresCerca(dragon)) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1f, pitch);
            player.addPotionEffect(new PotionEffect(
                    PotionEffectType.NIGHT_VISION, NV_TICKS, 0, false, false, false));
        }
    }

    private void telegrafiar(EnderDragon dragon, float pitch) {
        avisar(dragon, pitch);
        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (t >= TELEGRAPH_TICKS || dragon.isDead() || !dragon.isValid()) {
                    cancel();
                    return;
                }

                double progreso = (double) t / TELEGRAPH_TICKS;
                double radio = 2.5 * (1 - progreso) + 0.2;

                for (Player player : jugadoresCerca(dragon)) {
                    Location centro = player.getLocation();
                    for (int i = 0; i < 6; i++) {
                        double ang = (Math.PI * 2 / 6) * i + progreso * Math.PI * 2;
                        double x = Math.cos(ang) * radio;
                        double z = Math.sin(ang) * radio;
                        player.getWorld().spawnParticle(Particle.REVERSE_PORTAL,
                                centro.clone().add(x, 0.3 + progreso, z), 1, 0, 0, 0, 0);
                    }
                }
                t += 2;
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void tironDelVacio(EnderDragon dragon) {
        telegrafiar(dragon, 0.6f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> new BukkitRunnable() {
            int pulso = 0;

            @Override
            public void run() {
                if (pulso >= PULL_PULSES || dragon.isDead() || !dragon.isValid()) {
                    cancel();
                    return;
                }

                for (Player player : jugadoresCerca(dragon)) {
                    Vector out = player.getLocation().toVector().subtract(CENTER);
                    out.setY(0);
                    if (out.lengthSquared() < 0.01) out = new Vector(1, 0, 0);
                    out.normalize().multiply(PULL_STRENGTH);

                    Vector v = player.getVelocity();
                    player.setVelocity(new Vector(out.getX(), Math.max(v.getY(), 0.2), out.getZ()));
                    player.getWorld().spawnParticle(Particle.PORTAL, player.getLocation(), 12, 0.2, 0.2, 0.2, 0.05);
                    if (pulso == 0) player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 0.6f);
                }
                pulso++;
            }
        }.runTaskTimer(plugin, 0L, PULL_PULSE_INTERVAL), TELEGRAPH_TICKS);
    }

    private void alientoDelVacio(EnderDragon dragon) {
        telegrafiar(dragon, 0.8f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : jugadoresCerca(dragon)) {
                player.addPotionEffect(new PotionEffect(
                        PotionEffectType.LEVITATION, LEVITATION_TICKS, LEVITATION_AMP, false, true, true));
                player.getWorld().spawnParticle(Particle.PORTAL,
                        player.getLocation().add(0, 1, 0), 50, 0.5, 0.8, 0.5, 0.15);
                player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_SHOOT, 1f, 0.8f);
            }
        }, TELEGRAPH_TICKS);
    }

    private void meteorosDelVacio(EnderDragon dragon) {
        World world = dragon.getWorld();
        List<Player> objetivos = jugadoresCerca(dragon);
        if (objetivos.isEmpty()) return;

        avisar(dragon, 0.7f);

        List<Location> puntos = new ArrayList<>();
        for (int i = 0; i < METEOR_COUNT; i++) {
            Player ref = objetivos.get(random.nextInt(objetivos.size()));
            double dx = (random.nextDouble() * 2 - 1) * METEOR_SCATTER;
            double dz = (random.nextDouble() * 2 - 1) * METEOR_SCATTER;
            Block highest = world.getHighestBlockAt(ref.getLocation().add(dx, 0, dz));
            if (!highest.getType().isSolid()) continue;
            puntos.add(highest.getLocation().add(0.5, 1, 0.5));
        }

        zonaPeligro(dragon, puntos, METEOR_RADIUS, METEOR_DAMAGE, METEOR_LAUNCH);
    }

    private void implosionDelVacio(EnderDragon dragon) {
        List<Location> marcas = new ArrayList<>();
        for (Player player : jugadoresCerca(dragon)) {
            marcas.add(player.getLocation());
        }

        avisar(dragon, 0.7f);
        zonaPeligro(dragon, marcas, IMPLOSION_RADIUS, IMPLOSION_DAMAGE, IMPLOSION_LAUNCH);
    }

    private void zonaPeligro(EnderDragon dragon, List<Location> puntos, double radio, double dano, double launch) {
        World world = dragon.getWorld();
        Color rojo = Color.fromRGB(200, 25, 70);

        new BukkitRunnable() {
            int t = 0;

            @Override
            public void run() {
                if (dragon.isDead() || !dragon.isValid()) {
                    cancel();
                    return;
                }
                if (t >= TELEGRAPH_TICKS) {
                    cancel();
                    for (Location punto : puntos) {
                        impactar(world, punto, radio, dano, launch, dragon);
                    }
                    return;
                }

                for (Location punto : puntos) {
                    anilloPeligro(world, punto, radio, rojo);
                }
                t += 4;
            }
        }.runTaskTimer(plugin, 0L, 4L);
    }

    private void anilloPeligro(World world, Location centro, double radio, Color color) {
        Particle.DustOptions dust = new Particle.DustOptions(color, 1.7f);
        int puntos = (int) (radio * 8);
        for (int i = 0; i < puntos; i++) {
            double ang = Math.PI * 2 * i / puntos;
            double x = Math.cos(ang) * radio;
            double z = Math.sin(ang) * radio;
            world.spawnParticle(Particle.DUST, centro.clone().add(x, 0.2, z), 1, 0, 0, 0, 0, dust);
        }
        for (int h = 0; h < 4; h++) {
            world.spawnParticle(Particle.DUST, centro.clone().add(0, 0.5 + h * 0.7, 0), 1, 0, 0, 0, 0, dust);
        }
    }

    private void teletransporteDelVacio(EnderDragon dragon) {
        World world = dragon.getWorld();
        telegrafiar(dragon, 0.7f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Player player : jugadoresCerca(dragon)) {
                Location dest = puntoAleatorioCentro(world);
                if (dest == null) continue;
                dest.setYaw(player.getLocation().getYaw());
                dest.setPitch(player.getLocation().getPitch());

                player.teleport(dest);
                world.spawnParticle(Particle.PORTAL, dest, 60, 0.4, 0.8, 0.4, 0.1);
                world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.7f);
                player.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.9f);
            }
        }, TELEGRAPH_TICKS);
    }

    private Location puntoAleatorioCentro(World world) {
        for (int intento = 0; intento < TELEPORT_RETRIES; intento++) {
            int x = (int) ((random.nextDouble() * 2 - 1) * TELEPORT_RADIUS_CENTRO);
            int z = (int) ((random.nextDouble() * 2 - 1) * TELEPORT_RADIUS_CENTRO);
            Block highest = world.getHighestBlockAt(x, z);
            if (highest.getType().isSolid()) {
                return highest.getLocation().add(0.5, 1, 0.5);
            }
        }
        return null;
    }

    private void impactar(World world, Location centro, double radio, double dano, double launch, EnderDragon dragon) {
        world.spawnParticle(Particle.EXPLOSION, centro.clone().add(0, 0.5, 0), 1);
        world.spawnParticle(Particle.DUST, centro.clone().add(0, 0.5, 0), 40, 1.0, 0.5, 1.0, 0,
                new Particle.DustOptions(Color.fromRGB(200, 25, 70), 1.8f));
        world.playSound(centro, Sound.ENTITY_GENERIC_EXPLODE, 0.8f, 1.1f);

        double radioSq = radio * radio;
        for (Player player : world.getPlayers()) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
            if (player.getLocation().distanceSquared(centro) <= radioSq) {
                player.damage(dano, dragon);
                player.setVelocity(player.getVelocity().add(new Vector(0, launch, 0)));
            }
        }
    }

    private List<Player> jugadoresCerca(EnderDragon dragon) {
        List<Player> resultado = new ArrayList<>();
        for (Player player : dragon.getWorld().getPlayers()) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
            if (player.getLocation().distanceSquared(dragon.getLocation()) <= RANGE * RANGE) {
                resultado.add(player);
            }
        }
        return resultado;
    }
}

package org.delta.listeners.boss;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.AreaEffectCloud;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.mobs.boss.CustodioVacio;
import org.delta.managers.achievements.Achievement;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class CustodioVacioListener implements Listener {

    private static final String TAG = CustodioVacio.TAG;
    private static final long LOOP_INTERVAL = 20L;
    private static final long ATTACK_COOLDOWN = 70L;
    private static final double ACTIVATION_RANGE = 32.0;
    private static final double ENRAGE_THRESHOLD = 0.4;

    private final pendulum plugin;
    private final Set<UUID> attackCooldown = new HashSet<>();
    private final Set<UUID> enraged = new HashSet<>();
    private final Random rng = new Random();

    public CustodioVacioListener(pendulum plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!(entity instanceof LivingEntity boss)) continue;
                        if (!boss.getScoreboardTags().contains(TAG)) continue;

                        Player target = nearestPlayer(boss);
                        if (target == null) continue;

                        if (boss instanceof Mob mob) mob.setTarget(target);
                        if (isOverVoid(boss)) boss.teleport(target.getLocation());

                        if (attackCooldown.contains(boss.getUniqueId())) continue;
                        if (boss.getLocation().distanceSquared(target.getLocation())
                                > ACTIVATION_RANGE * ACTIVATION_RANGE) continue;

                        performRandomAttack(boss, target);
                        startCooldown(boss.getUniqueId());
                    }
                }
            }
        }.runTaskTimer(plugin, LOOP_INTERVAL, LOOP_INTERVAL);
    }

    private void performRandomAttack(LivingEntity boss, Player target) {
        double distance = boss.getLocation().distance(target.getLocation());

        List<Integer> pool = new ArrayList<>();
        if (distance > 4.0) pool.add(0);
        pool.add(1);
        if (distance <= 10.0) pool.add(2);
        pool.add(3);
        pool.add(4);

        switch (pool.get(rng.nextInt(pool.size()))) {
            case 0 -> parpadeo(boss, target);
            case 1 -> salvaDelVacio(boss, target);
            case 2 -> pulsoDeLevitacion(boss);
            case 3 -> alientoDelVacio(boss, target);
            default -> veloDelVacio(boss);
        }
    }

    private void parpadeo(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.spawnParticle(Particle.REVERSE_PORTAL, boss.getLocation().add(0, 1, 0), 12, 0.3, 0.6, 0.3, 0.05);
        world.playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.8f);

        double angle = rng.nextDouble() * Math.PI * 2;
        Location dest = target.getLocation().clone().add(Math.cos(angle) * 2.0, 0, Math.sin(angle) * 2.0);
        boss.teleport(dest);

        world.spawnParticle(Particle.PORTAL, dest.clone().add(0, 1, 0), 18, 0.3, 0.6, 0.3, 0.1);
        world.playSound(dest, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 0.6f);

        if (dest.distance(target.getLocation()) <= 3.5) {
            target.damage(7.0, boss);
            Vector knockback = target.getLocation().toVector().subtract(dest.toVector());
            if (knockback.lengthSquared() > 0) knockback.normalize();
            knockback.setY(0.45);
            target.setVelocity(target.getVelocity().add(knockback));
        }
    }

    private void salvaDelVacio(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_SHULKER_SHOOT, 1.3f, 0.7f);

        for (int i = 0; i < 5; i++) {
            ShulkerBullet bullet = world.spawn(boss.getEyeLocation(), ShulkerBullet.class);
            bullet.setShooter(boss);
            bullet.setTarget(target);
        }
    }

    private void pulsoDeLevitacion(LivingEntity boss) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.2f, 0.6f);
        world.spawnParticle(Particle.PORTAL, boss.getLocation().add(0, 1, 0), 20, 1.5, 0.6, 1.5, 0.1);

        for (Player player : nearbyPlayers(boss, 12.0)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, 70, 2, false, true));
        }
    }

    private void alientoDelVacio(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_AMBIENT, 1f, 0.5f);

        AreaEffectCloud cloud = world.spawn(target.getLocation(), AreaEffectCloud.class);
        cloud.setRadius(3.5f);
        cloud.setDuration(120);
        cloud.setRadiusPerTick(-0.01f);
        cloud.addCustomEffect(new PotionEffect(PotionEffectType.WITHER, 80, 1), true);
    }

    private void veloDelVacio(LivingEntity boss) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.0f, 0.5f);
        world.spawnParticle(Particle.PORTAL, boss.getLocation().add(0, 1, 0), 20, 1, 1, 1, 0.2);

        for (Player player : nearbyPlayers(boss, 14.0)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, 100, 0, false, true));
            player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1, false, true));
        }
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof LivingEntity boss)) return;
        if (!boss.getScoreboardTags().contains(TAG)) return;
        if (enraged.contains(boss.getUniqueId())) return;

        double healthAfter = boss.getHealth() - event.getFinalDamage();
        double max = boss.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH).getValue();
        if (healthAfter > max * ENRAGE_THRESHOLD) return;

        enraged.add(boss.getUniqueId());
        boss.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1, false, true));
        boss.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, Integer.MAX_VALUE, 1, false, true));
        boss.getWorld().spawnParticle(Particle.REVERSE_PORTAL, boss.getLocation().add(0, 1, 0), 20);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_ENDERMAN_SCREAM, 1.4f, 0.7f);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (!event.getEntity().getScoreboardTags().contains(TAG)) return;

        attackCooldown.remove(id);
        enraged.remove(id);

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getAchievementManager().unlock(killer, Achievement.VIGILIA_ROTA);
        }

        event.getDrops().clear();
        event.setDroppedExp(80);

        Location loc = event.getEntity().getLocation();

        String endItem = rng.nextBoolean() ? "ancla_vinculo" : "frasco_vacio";
        int amount = 1 + rng.nextInt(5);
        ItemRegistry.get(endItem).ifPresent(item -> {
            for (int i = 0; i < amount; i++) {
                loc.getWorld().dropItemNaturally(loc, item.build());
            }
        });
    }

    private void startCooldown(UUID id) {
        attackCooldown.add(id);
        Bukkit.getScheduler().runTaskLater(plugin, () -> attackCooldown.remove(id), ATTACK_COOLDOWN);
    }

    private boolean isOverVoid(LivingEntity boss) {
        Location base = boss.getLocation();
        for (int dy = 1; dy <= 6; dy++) {
            if (base.clone().subtract(0, dy, 0).getBlock().getType().isSolid()) return false;
        }
        return true;
    }

    private List<Player> nearbyPlayers(LivingEntity boss, double radius) {
        double squared = radius * radius;
        return boss.getWorld().getPlayers().stream()
                .filter(p -> p.getGameMode() == GameMode.SURVIVAL)
                .filter(p -> p.getLocation().distanceSquared(boss.getLocation()) <= squared)
                .toList();
    }

    private Player nearestPlayer(LivingEntity boss) {
        return boss.getWorld().getPlayers().stream()
                .filter(p -> !p.isDead() && p.getGameMode() == GameMode.SURVIVAL)
                .min(Comparator.comparingDouble(p -> p.getLocation().distanceSquared(boss.getLocation())))
                .orElse(null);
    }
}

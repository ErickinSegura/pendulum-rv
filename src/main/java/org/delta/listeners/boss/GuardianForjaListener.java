package org.delta.listeners.boss;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.mobs.boss.GuardianForja;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class GuardianForjaListener implements Listener {

    private static final String TAG = GuardianForja.TAG;
    private static final long LOOP_INTERVAL = 20L;
    private static final long ATTACK_COOLDOWN = 90L;
    private static final double ACTIVATION_RANGE = 28.0;
    private static final double ENRAGE_THRESHOLD = 0.4;
    private static final double MODIFIER_DROP_CHANCE = 0.20;

    private static final String[] MODIFIER_KEYS = {
            "unbreakable_modifier",
            "liviano_modifier",
            "temple_modifier"
    };

    private final pendulum plugin;
    private final Set<UUID> attackCooldown = new HashSet<>();
    private final Set<UUID> enraged = new HashSet<>();
    private final Random rng = new Random();

    public GuardianForjaListener(pendulum plugin) {
        this.plugin = plugin;
        startAttackLoop();
    }

    private void startAttackLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (World world : Bukkit.getWorlds()) {
                    for (Entity entity : world.getEntities()) {
                        if (!(entity instanceof LivingEntity boss)) continue;
                        if (!boss.getScoreboardTags().contains(TAG)) continue;
                        if (attackCooldown.contains(boss.getUniqueId())) continue;

                        Player target = nearestPlayer(boss);
                        if (target == null) continue;
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
        if (distance <= 7.0) pool.add(0);
        pool.add(1);
        pool.add(2);
        pool.add(3);
        pool.add(4);
        if (distance > 4.0) pool.add(5);

        switch (pool.get(rng.nextInt(pool.size()))) {
            case 0 -> golpeDeYunque(boss);
            case 1 -> llamarada(boss, target);
            case 2 -> convocarEscoria(boss, target);
            case 3 -> salvaDeFlechas(boss, target);
            case 4 -> lluviaDeFuego(boss, target);
            default -> tironGravitatorio(boss);
        }
    }

    private void golpeDeYunque(LivingEntity boss) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.2f, 0.7f);
        world.playSound(boss.getLocation(), Sound.BLOCK_ANVIL_LAND, 1.0f, 0.6f);
        world.spawnParticle(Particle.EXPLOSION, boss.getLocation(), 3);
        world.spawnParticle(Particle.BLOCK, boss.getLocation().add(0, 0.2, 0), 40,
                2, 0.3, 2, Material.ANVIL.createBlockData());

        for (Player player : nearbyPlayers(boss, 5.0)) {
            player.damage(5.0, boss);
            Vector knockback = player.getLocation().toVector()
                    .subtract(boss.getLocation().toVector());
            if (knockback.lengthSquared() > 0) knockback.normalize();
            knockback.setY(0.7);
            player.setVelocity(player.getVelocity().add(knockback));
        }
    }

    private void salvaDeFlechas(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_SKELETON_SHOOT, 1.2f, 0.7f);

        for (int i = 0; i < 5; i++) {
            Vector direction = target.getEyeLocation().toVector()
                    .subtract(boss.getEyeLocation().toVector())
                    .normalize()
                    .add(new Vector(
                            (rng.nextDouble() - 0.5) * 0.25,
                            (rng.nextDouble() - 0.5) * 0.1,
                            (rng.nextDouble() - 0.5) * 0.25));

            Arrow arrow = world.spawn(boss.getEyeLocation().add(direction), Arrow.class);
            arrow.setShooter(boss);
            arrow.setVelocity(direction.multiply(2.4));
            arrow.setDamage(3.0);
            arrow.setCritical(true);
        }
    }

    private void lluviaDeFuego(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ITEM_FIRECHARGE_USE, 1.2f, 0.8f);

        for (int i = 0; i < 6; i++) {
            var above = target.getLocation().add(
                    rng.nextDouble() * 6 - 3, 9, rng.nextDouble() * 6 - 3);
            SmallFireball fireball = world.spawn(above, SmallFireball.class);
            fireball.setShooter(boss);
            fireball.setDirection(new Vector(0, -1, 0));
            fireball.setIsIncendiary(false);
            fireball.setYield(0f);
        }
    }

    private void tironGravitatorio(LivingEntity boss) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_WITHER_SHOOT, 1.0f, 1.2f);
        world.spawnParticle(Particle.PORTAL, boss.getLocation().add(0, 1, 0), 60, 0.5, 1, 0.5, 0.4);

        for (Player player : nearbyPlayers(boss, 20.0)) {
            Vector pull = boss.getLocation().toVector()
                    .subtract(player.getLocation().toVector());
            if (pull.lengthSquared() > 0) pull.normalize();
            pull.multiply(1.2);
            pull.setY(0.35);
            player.setVelocity(pull);
        }
    }

    private void llamarada(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1.2f, 0.8f);

        for (int i = 0; i < 3; i++) {
            Vector direction = target.getEyeLocation().toVector()
                    .subtract(boss.getEyeLocation().toVector())
                    .normalize()
                    .add(new Vector(
                            (rng.nextDouble() - 0.5) * 0.2,
                            (rng.nextDouble() - 0.5) * 0.1,
                            (rng.nextDouble() - 0.5) * 0.2));

            SmallFireball fireball = world.spawn(
                    boss.getEyeLocation().add(direction), SmallFireball.class);
            fireball.setShooter(boss);
            fireball.setDirection(direction);
            fireball.setIsIncendiary(false);
            fireball.setYield(0f);
        }
    }

    private void convocarEscoria(LivingEntity boss, Player target) {
        World world = boss.getWorld();
        world.playSound(boss.getLocation(), Sound.ENTITY_EVOKER_CAST_SPELL, 1.2f, 0.8f);
        world.spawnParticle(Particle.FLAME, boss.getLocation().add(0, 1, 0), 30, 1, 1, 1, 0.02);

        int count = 2 + rng.nextInt(2);
        for (int i = 0; i < count; i++) {
            var spawnLocation = boss.getLocation().add(
                    rng.nextDouble() * 4 - 2, 0, rng.nextDouble() * 4 - 2);
            MagmaCube cube = (MagmaCube) world.spawnEntity(spawnLocation, EntityType.MAGMA_CUBE);
            cube.setSize(2);
            cube.addScoreboardTag("guardian_minion");
            cube.setTarget(target);
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
        boss.getWorld().spawnParticle(Particle.LAVA, boss.getLocation().add(0, 1, 0), 30);
        boss.getWorld().playSound(boss.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.2f, 0.8f);
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        UUID id = event.getEntity().getUniqueId();
        if (!event.getEntity().getScoreboardTags().contains(TAG)) return;

        attackCooldown.remove(id);
        enraged.remove(id);

        event.getDrops().clear();
        event.setDroppedExp(60);

        if (rng.nextDouble() <= MODIFIER_DROP_CHANCE) {
            String key = MODIFIER_KEYS[rng.nextInt(MODIFIER_KEYS.length)];
            ItemRegistry.get(key).ifPresent(item ->
                    event.getEntity().getWorld().dropItemNaturally(
                            event.getEntity().getLocation(), item.build()));
        }
    }

    private void startCooldown(UUID id) {
        attackCooldown.add(id);
        Bukkit.getScheduler().runTaskLater(plugin, () -> attackCooldown.remove(id), ATTACK_COOLDOWN);
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

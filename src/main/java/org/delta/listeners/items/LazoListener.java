package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.delta.customs.items.CustomItem;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Convierte el "Lazo" (una caña de pescar custom) en un gancho de agarre:
 * cuando el anzuelo se clava en un bloque, impulsa al jugador hacia ese punto
 * en arco para balancearse. El balanceo se suelta al agacharse, soltar la caña,
 * acercarse demasiado al ancla o tras un tiempo máximo.
 */
public class LazoListener implements Listener {

    private static final double PULL_STRENGTH = 1.2;   // fuerza del impulso hacia el ancla
    private static final double MAX_SPEED = 2.2;       // velocidad máxima resultante
    private static final double UPWARD_BOOST = 0.35;   // componente vertical para el arco
    private static final double STOP_DISTANCE = 1.5;   // distancia mínima al ancla
    private static final int MAX_TICKS = 60;           // duración máxima del balanceo

    private final Set<UUID> swinging = new HashSet<>();
    private final Set<UUID> noFall = new HashSet<>();

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!isLazo(player.getInventory().getItemInMainHand())
                && !isLazo(player.getInventory().getItemInOffHand())) {
            return;
        }

        switch (event.getState()) {
            // Al lanzar la caña rastreamos el anzuelo hasta que toque un bloque.
            case FISHING -> trackHook(player, event.getHook());
            case REEL_IN, FAILED_ATTEMPT, CAUGHT_ENTITY, CAUGHT_FISH ->
                    swinging.remove(player.getUniqueId());
            default -> {
            }
        }
    }

    /**
     * Sigue al anzuelo tras lanzarlo y arranca el balanceo en cuanto se clava
     * en un bloque (o aterriza en el suelo). No dependemos del evento IN_GROUND
     * porque no se dispara de forma fiable en todas las superficies.
     */
    private void trackHook(Player player, FishHook hook) {
        new BukkitRunnable() {
            int waited = 0;

            @Override
            public void run() {
                if (hook == null || hook.isDead() || !player.isOnline()
                        || waited++ > 100) {
                    cancel();
                    return;
                }
                if (hasLanded(hook)) {
                    startSwing(player, hook);
                    cancel();
                }
            }
        }.runTaskTimer(pendulum.getInstance(), 1L, 1L);
    }

    private boolean hasLanded(FishHook hook) {
        if (hook.isOnGround()) return true;
        Location loc = hook.getLocation();
        return !loc.getBlock().isPassable()
                || !loc.clone().add(0, -0.3, 0).getBlock().isPassable();
    }

    private void startSwing(Player player, FishHook hook) {
        UUID id = player.getUniqueId();
        Location anchor = hook.getLocation().clone();
        swinging.add(id);
        player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_ATTACH, 1f, 1.2f);
        pendulum.getInstance().getAchievementManager().unlock(player, org.delta.managers.achievements.Achievement.COLUMPIO_URBANO);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !swinging.contains(id) || hook.isDead()
                        || player.isSneaking() || ticks++ >= MAX_TICKS
                        || (!isLazo(player.getInventory().getItemInMainHand())
                        && !isLazo(player.getInventory().getItemInOffHand()))) {
                    stop(id, hook);
                    cancel();
                    return;
                }

                Vector dir = anchor.toVector().subtract(player.getLocation().toVector());
                if (dir.length() < STOP_DISTANCE) {
                    stop(id, hook);
                    cancel();
                    return;
                }

                Vector pull = dir.normalize().multiply(PULL_STRENGTH);
                pull.setY(pull.getY() + UPWARD_BOOST);
                if (pull.length() > MAX_SPEED) {
                    pull = pull.normalize().multiply(MAX_SPEED);
                }

                player.setVelocity(pull);
                player.setFallDistance(0f);
                noFall.add(id);
            }
        }.runTaskTimer(pendulum.getInstance(), 0L, 2L);
    }

    private void stop(UUID id, FishHook hook) {
        swinging.remove(id);
        if (hook != null && !hook.isDead()) {
            hook.remove();
        }
        // Mantiene la inmunidad a caída un momento tras soltarse y luego la limpia.
        Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), () -> noFall.remove(id), 120L);
    }

    @EventHandler
    public void onFall(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (noFall.remove(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    private boolean isLazo(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "lazo".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

package org.delta.listeners.chargebase;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;
import org.delta.libs.MessageUtils;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.pendulum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ChargeBaseZoneListener implements Listener {

    private final ChargeBaseManager manager;
    private final Set<UUID> insideZone = new HashSet<>();
    private final Map<UUID, BukkitTask> glowTasks = new HashMap<>();

    public ChargeBaseZoneListener(ChargeBaseManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!manager.isActive()) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) return;

        Player player  = event.getPlayer();
        UUID   uid     = player.getUniqueId();
        boolean inside    = manager.getActiveZone().isInside(event.getTo());
        boolean wasInside = insideZone.contains(uid);

        if (inside && !wasInside) {
            insideZone.add(uid);
            player.sendMessage(MessageUtils.color("&8[&d&l!&8] &7Entraste a la &d&lBase de Carga&7."));
            applyZoneEffects(player);
            pendulum.getInstance().getAchievementManager()
                    .unlock(player, org.delta.managers.achievements.Achievement.EN_EL_OJO_DEL_HURACAN);
        } else if (!inside && wasInside) {
            insideZone.remove(uid);
            player.sendMessage(MessageUtils.color("&8[&d&l!&8] &7Saliste de la &d&lBase de Carga&7."));
            removeZoneEffects(player);
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uid = player.getUniqueId();

        if (insideZone.remove(uid)) {
            removeZoneEffects(player);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.GLOWING);
        cancelGlowTask(player.getUniqueId());
    }

    @EventHandler
    public void onPotionRemoved(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!insideZone.contains(player.getUniqueId())) return;
        if (event.getModifiedType() != PotionEffectType.DARKNESS) return;
        if (event.getCause() == EntityPotionEffectEvent.Cause.EXPIRATION) return;
        if (event.getCause() == EntityPotionEffectEvent.Cause.PLUGIN) return;

        event.setCancelled(true);
    }

    public void cleanupAll() {
        for (UUID uid : new HashSet<>(insideZone)) {
            Player player = Bukkit.getPlayer(uid);
            if (player != null) {
                removeZoneEffects(player);
            }
            cancelGlowTask(uid);
        }
        insideZone.clear();
    }

    private void applyZoneEffects(Player player) {
        UUID uid = player.getUniqueId();

        player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, PotionEffect.INFINITE_DURATION, 0, false, false));

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), () -> {
            if (!player.isOnline()) {
                cancelGlowTask(uid);
                return;
            }
            player.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, 45, 0, false, true));
            player.playSound(
                    player.getLocation(),
                    "minecraft:entity.warden.heartbeat",
                    org.bukkit.SoundCategory.MASTER,
                    1.0f,
                    1.0f
            );
        }, 200L, 200L);

        glowTasks.put(uid, task);
    }

    private void removeZoneEffects(Player player) {
        player.removePotionEffect(PotionEffectType.DARKNESS);
        player.removePotionEffect(PotionEffectType.GLOWING);
        cancelGlowTask(player.getUniqueId());
    }

    private void cancelGlowTask(UUID uid) {
        BukkitTask task = glowTasks.remove(uid);
        if (task != null) {
            task.cancel();
        }
    }
}
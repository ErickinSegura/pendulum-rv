package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ArmorStand;
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
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.delta.customs.items.CustomItem;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Convierte el "Lazo" (una caña de pescar custom) en un gancho de agarre:
 * el anzuelo se lanza más lejos y, en cuanto toca un bloque, se queda clavado
 * sin caer. El jalón hacia el ancla ocurre cuando el jugador vuelve a accionar
 * la caña (recoger), no al tocar el bloque. El balanceo se suelta al agacharse,
 * soltar la caña, acercarse demasiado al ancla o tras un tiempo máximo.
 */
public class LazoListener implements Listener {

    private static final double CAST_POWER = 2.4;      // velocidad que le reimponemos al anzuelo para que llegue más lejos
    private static final double PULL_STRENGTH = 1.2;   // fuerza del impulso hacia el ancla
    private static final double MAX_SPEED = 2.2;       // velocidad máxima resultante
    private static final double UPWARD_BOOST = 0.35;   // componente vertical para el arco
    private static final double STOP_DISTANCE = 1.5;   // distancia mínima al ancla
    private static final int MAX_TICKS = 60;           // duración máxima del balanceo
    private static final int MAX_PIN_TICKS = 200;      // cuánto aguanta el anzuelo clavado esperando el jalón

    private final Set<UUID> swinging = new HashSet<>();
    private final Set<UUID> noFall = new HashSet<>();
    private final Map<UUID, FishHook> pinnedHooks = new HashMap<>();
    private final Map<UUID, Location> pinnedAnchors = new HashMap<>();
    private final Map<UUID, ArmorStand> pinnedMarkers = new HashMap<>();

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        if (!isLazo(player.getInventory().getItemInMainHand())
                && !isLazo(player.getInventory().getItemInOffHand())) {
            return;
        }

        switch (event.getState()) {
            // Al lanzar la caña impulsamos el anzuelo y lo rastreamos hasta que toque un bloque.
            case FISHING -> {
                FishHook hook = event.getHook();
                // El vanilla fija la velocidad del anzuelo justo después de este evento,
                // así que la reimponemos un tick más tarde para que sí llegue lejos.
                Bukkit.getScheduler().runTask(pendulum.getInstance(), () -> {
                    if (!hook.isDead()) {
                        hook.setVelocity(player.getEyeLocation().getDirection().multiply(CAST_POWER));
                    }
                });
                trackHook(player, hook);
            }
            // Volver a accionar la caña: si el anzuelo está clavado, ahora sí jala al jugador.
            // Como el anzuelo quedó enganchado al marcador, el vanilla dispara CAUGHT_ENTITY;
            // cancelamos para que no intente "jalar" el marcador y limpiamos a mano.
            case REEL_IN, FAILED_ATTEMPT, CAUGHT_ENTITY, CAUGHT_FISH -> {
                UUID id = player.getUniqueId();
                Location anchor = pinnedAnchors.get(id);
                if (anchor != null) {
                    event.setCancelled(true);
                    unpin(id);
                    startSwing(player, anchor);
                }
            }
            default -> {
            }
        }
    }

    /**
     * Sigue al anzuelo tras lanzarlo y lo clava en cuanto golpea un bloque.
     * Trazamos un rayo entre la posición anterior y la actual cada tick, así
     * detectamos también las paredes (donde el anzuelo queda "en el aire" pegado
     * a la cara del bloque) en vez de esperar a que resbale hasta el suelo.
     */
    private void trackHook(Player player, FishHook hook) {
        new BukkitRunnable() {
            Location last = hook.getLocation().clone();
            boolean moved = false;
            int waited = 0;

            @Override
            public void run() {
                if (hook == null || hook.isDead() || !player.isOnline()
                        || waited > 100) {
                    cancel();
                    return;
                }
                // En el primer tick solo fijamos el origen del trazo; a partir del
                // siguiente ya buscamos el primer bloque que cruce el anzuelo.
                if (waited++ == 0) {
                    last = hook.getLocation().clone();
                    return;
                }

                Location now = hook.getLocation();
                Vector delta = now.toVector().subtract(last.toVector());
                double step = delta.length();
                if (step > 0.15) moved = true;

                // 1) Trazamos desde la posición previa, con algo de alcance extra,
                //    para clavarlo en la cara del primer bloque que cruce o roce.
                if (step > 1.0e-4) {
                    RayTraceResult hit = player.getWorld().rayTraceBlocks(
                            last, delta.clone().normalize(), step + 0.6,
                            FluidCollisionMode.NEVER, true);
                    if (hit != null && hit.getHitBlock() != null) {
                        pinHook(player, hook, surfaceAnchor(hit, player.getWorld()));
                        cancel();
                        return;
                    }
                }

                // 2) Si ya voló y se frenó junto a un bloque, es que topó con algo
                //    (pared o suelo): el vanilla pone su velocidad casi a cero al
                //    chocar. Exigimos un bloque sólido adyacente para no clavarlo en
                //    el aire cuando solo se frena un instante en el ápice de un tiro.
                if (moved && hook.getVelocity().lengthSquared() < 0.02
                        && !isWater(now) && nearSolid(hook)) {
                    pinHook(player, hook, now.clone());
                    cancel();
                    return;
                }

                // 3) Respaldo: apoyado en el suelo o metido dentro de un bloque.
                if (hook.isOnGround() || isInsideBlock(hook)) {
                    pinHook(player, hook, now.clone());
                    cancel();
                    return;
                }
                last = now.clone();
            }
        }.runTaskTimer(pendulum.getInstance(), 1L, 1L);
    }

    private boolean isInsideBlock(FishHook hook) {
        Location loc = hook.getLocation();
        return !loc.getBlock().isPassable()
                || !loc.clone().add(0, -0.3, 0).getBlock().isPassable();
    }

    /**
     * ¿El anzuelo está pegado a un bloque sólido? Revisa su propio bloque y los
     * seis vecinos a corta distancia. Sirve para confirmar un choque real y no
     * clavarlo flotando en pleno aire.
     */
    private boolean nearSolid(FishHook hook) {
        Location loc = hook.getLocation();
        if (!loc.getBlock().isPassable()) return true;
        double r = 0.4;
        int[][] dirs = {{1, 0, 0}, {-1, 0, 0}, {0, 0, 1}, {0, 0, -1}, {0, -1, 0}, {0, 1, 0}};
        for (int[] d : dirs) {
            if (!loc.clone().add(d[0] * r, d[1] * r, d[2] * r).getBlock().isPassable()) {
                return true;
            }
        }
        return false;
    }

    private boolean isWater(Location loc) {
        org.bukkit.Material type = loc.getBlock().getType();
        return type == org.bukkit.Material.WATER
                || type == org.bukkit.Material.BUBBLE_COLUMN;
    }

    /**
     * Punto exacto del impacto, retirado un poco sobre la cara golpeada para que
     * el anzuelo quede apoyado en la superficie y no medio incrustado.
     */
    private Location surfaceAnchor(RayTraceResult hit, org.bukkit.World world) {
        Location anchor = hit.getHitPosition().toLocation(world);
        BlockFace face = hit.getHitBlockFace();
        if (face != null) {
            anchor.add(face.getDirection().multiply(0.08));
        }
        return anchor;
    }

    /**
     * Clava el anzuelo en el punto de contacto enganchándolo a un armor stand
     * invisible (marcador) puesto ahí. Al quedar "hooked in entity", cliente y
     * servidor fijan el anzuelo en el marcador y le quitan la gravedad, así que
     * se ve pegado de forma estable. Permanece clavado hasta que el jugador
     * acciona la caña, cambia de objeto o se agota el tiempo.
     */
    private void pinHook(Player player, FishHook hook, Location anchor) {
        UUID id = player.getUniqueId();

        ArmorStand marker = player.getWorld().spawn(anchor, ArmorStand.class, stand -> {
            stand.setVisible(false);
            stand.setMarker(true);
            stand.setGravity(false);
            stand.setInvulnerable(true);
            stand.setSilent(true);
            stand.setPersistent(false);
            stand.setCollidable(false);
        });

        pinnedHooks.put(id, hook);
        pinnedAnchors.put(id, anchor);
        pinnedMarkers.put(id, marker);
        NMSEntityUtils.attachHook(hook, marker);
        player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_ATTACH, 1f, 1.2f);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (pinnedHooks.get(id) != hook) {
                    cancel();
                    return;
                }
                if (!player.isOnline() || hook.isDead() || ticks++ > MAX_PIN_TICKS
                        || (!isLazo(player.getInventory().getItemInMainHand())
                        && !isLazo(player.getInventory().getItemInOffHand()))) {
                    unpin(id);
                    cancel();
                }
            }
        }.runTaskTimer(pendulum.getInstance(), 1L, 1L);
    }

    private void unpin(UUID id) {
        FishHook hook = pinnedHooks.remove(id);
        pinnedAnchors.remove(id);
        ArmorStand marker = pinnedMarkers.remove(id);
        if (marker != null && !marker.isDead()) {
            marker.remove();
        }
        if (hook != null && !hook.isDead()) {
            hook.remove();
        }
    }

    private void startSwing(Player player, Location anchor) {
        UUID id = player.getUniqueId();
        swinging.add(id);
        player.playSound(player.getLocation(), Sound.BLOCK_TRIPWIRE_ATTACH, 1f, 1.2f);
        pendulum.getInstance().getAchievementManager().unlock(player, org.delta.managers.achievements.Achievement.COLUMPIO_URBANO);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (!player.isOnline() || !swinging.contains(id)
                        || player.isSneaking() || ticks++ >= MAX_TICKS
                        || (!isLazo(player.getInventory().getItemInMainHand())
                        && !isLazo(player.getInventory().getItemInOffHand()))) {
                    stop(id);
                    cancel();
                    return;
                }

                Vector dir = anchor.toVector().subtract(player.getLocation().toVector());
                if (dir.length() < STOP_DISTANCE) {
                    stop(id);
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

    private void stop(UUID id) {
        swinging.remove(id);
        // La inmunidad a caída cubre SOLO la caída provocada por el balanceo: la
        // mantenemos mientras el jugador sigue en el aire por el impulso y la
        // quitamos en cuanto toca suelo (o agua). Así una caída posterior y ajena
        // al lazo sí recibe daño normal. Un tope de seguridad la limpia igual.
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                Player player = Bukkit.getPlayer(id);
                if (player == null || !player.isOnline() || !noFall.contains(id)
                        || player.isOnGround() || player.isInWater()
                        || ticks++ > 100) {
                    noFall.remove(id);
                    cancel();
                }
            }
        }.runTaskTimer(pendulum.getInstance(), 4L, 2L);
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

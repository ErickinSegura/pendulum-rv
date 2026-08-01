package org.delta.listeners.event;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.delta.libs.MessageUtils;
import org.delta.managers.event.EventManager;
import org.delta.managers.event.ServerEvent;

public class EventRestrictionListener implements Listener {

    private final EventManager manager;

    public EventRestrictionListener(EventManager manager) {
        this.manager = manager;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        ServerEvent active = manager.getActiveEvent();
        if (active == null || !active.restrictsMovement()) return;

        Location anchor = manager.getAnchor();
        if (anchor == null) return;

        Location to = event.getTo();
        if (event.getFrom().getBlockX() == to.getBlockX()
                && event.getFrom().getBlockZ() == to.getBlockZ()) return;

        if (fuera(to, anchor, active.getMovementRadius())) {
            event.setTo(event.getFrom());
            event.getPlayer().sendActionBar(MessageUtils.color("&c✘ No puedes alejarte de la ceremonia."));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        ServerEvent active = manager.getActiveEvent();
        if (active == null || !active.cancelsDamage()) return;

        if (event.getEntity() instanceof Player) {
            event.setCancelled(true);
            return;
        }

        if (event instanceof EntityDamageByEntityEvent porEntidad && esOrigenJugador(porEntidad.getDamager())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ServerEvent active = manager.getActiveEvent();
        if (active == null) return;

        Location anchor = manager.getAnchor();
        if (anchor == null) return;

        active.onPlayerJoin(event.getPlayer(), anchor);
    }

    private boolean esOrigenJugador(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player) return true;
        if (damager instanceof Projectile projectile) {
            ProjectileSource source = projectile.getShooter();
            return source instanceof Player;
        }
        return false;
    }

    private boolean fuera(Location loc, Location anchor, double radius) {
        if (loc.getWorld() == null || !loc.getWorld().equals(anchor.getWorld())) return true;
        double dx = loc.getX() - anchor.getX();
        double dz = loc.getZ() - anchor.getZ();
        return Math.sqrt(dx * dx + dz * dz) > radius;
    }
}

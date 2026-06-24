package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

public class SaltoDobleListener extends BasePerkListener {

    private static final double JUMP_POWER = 0.8;
    private static final double FORWARD_BOOST = 0.35;

    public SaltoDobleListener() {
        Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                GameMode gm = player.getGameMode();
                if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;

                boolean has = hasTeamPerk(player, Perk.SALTO_DOBLE);
                if (has && player.isOnGround()) {
                    player.setAllowFlight(true);
                } else if (!has && player.getAllowFlight()) {
                    player.setAllowFlight(false);
                }
            }
        }, 0L, 5L);
    }

    @EventHandler
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (!hasTeamPerk(player, Perk.SALTO_DOBLE)) return;
        if (player.isFlying()) return;

        event.setCancelled(true);
        player.setAllowFlight(false);
        player.setFlying(false);

        Vector direction = player.getLocation().getDirection();
        Vector velocity = player.getVelocity();
        velocity.setY(JUMP_POWER);
        velocity.add(new Vector(direction.getX() * FORWARD_BOOST, 0, direction.getZ() * FORWARD_BOOST));
        player.setVelocity(velocity);

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.2f);
    }
}

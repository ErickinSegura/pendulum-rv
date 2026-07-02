package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SaltoDobleListener extends BasePerkListener {

    private static final double JUMP_POWER = 0.8;
    private static final double FORWARD_BOOST = 0.35;

    private final Set<UUID> jumpHeld = ConcurrentHashMap.newKeySet();
    private final Set<UUID> armed = ConcurrentHashMap.newKeySet();
    private final Set<UUID> doubleJumped = ConcurrentHashMap.newKeySet();

    public SaltoDobleListener() {
        Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), this::tick, 0L, 1L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID id = player.getUniqueId();
            GameMode gm = player.getGameMode();

            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR
                    || !hasTeamPerk(player, Perk.SALTO_DOBLE)) {
                jumpHeld.remove(id);
                armed.remove(id);
                doubleJumped.remove(id);
                continue;
            }

            boolean jumping = player.getCurrentInput().isJump();
            boolean rising = jumping && !jumpHeld.contains(id);

            if (player.isOnGround()) {
                doubleJumped.remove(id);
                armed.remove(id);
            } else if (!jumping) {
                armed.add(id);
            }

            if (rising
                    && !player.isOnGround()
                    && !player.isFlying()
                    && !player.isGliding()
                    && armed.contains(id)
                    && doubleJumped.add(id)) {
                armed.remove(id);
                doubleJump(player);
            }

            if (jumping) {
                jumpHeld.add(id);
            } else {
                jumpHeld.remove(id);
            }
        }
    }

    private void doubleJump(Player player) {
        Vector direction = player.getLocation().getDirection();
        Vector velocity = player.getVelocity();
        velocity.setY(JUMP_POWER);
        velocity.add(new Vector(direction.getX() * FORWARD_BOOST, 0, direction.getZ() * FORWARD_BOOST));
        player.setVelocity(velocity);

        player.getWorld().spawnParticle(Particle.CLOUD, player.getLocation(), 15, 0.3, 0.1, 0.3, 0.05);
        player.playSound(player.getLocation(), Sound.ENTITY_BREEZE_JUMP, 1.0f, 1.2f);
    }
}

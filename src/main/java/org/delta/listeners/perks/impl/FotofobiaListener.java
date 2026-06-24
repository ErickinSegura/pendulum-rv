package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

public class FotofobiaListener extends BasePerkListener {

    private static final int FIRE_TICKS = 100;

    public FotofobiaListener() {
        Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), this::tick, 0L, 20L);
    }

    private void tick() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            GameMode gm = player.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) continue;
            if (!hasTeamPerk(player, Perk.FOTOFOBIA)) continue;
            if (!isExposedToSun(player)) continue;

            player.setFireTicks(FIRE_TICKS);
            player.getWorld().spawnParticle(Particle.SMOKE,
                    player.getLocation().add(0, 1, 0), 5, 0.3, 0.5, 0.3, 0.01);
        }
    }

    private boolean isExposedToSun(Player player) {
        World world = player.getWorld();
        if (world.getEnvironment() != World.Environment.NORMAL) return false;
        if (!world.isDayTime()) return false;
        if (world.hasStorm()) return false;
        if (player.isInWater()) return false;

        Block block = player.getEyeLocation().getBlock();
        return block.getLightFromSky() >= 15;
    }
}

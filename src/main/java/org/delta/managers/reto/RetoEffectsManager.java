package org.delta.managers.reto;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class RetoEffectsManager {
    private static RetoEffectsManager instance;

    private RetoEffectsManager() {}

    public static RetoEffectsManager getInstance() {
        if (instance == null) {
            instance = new RetoEffectsManager();
        }
        return instance;
    }

    public void reproducirEfectosCompletado(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();
        Plugin plugin = Bukkit.getPluginManager().getPlugin("Pendulum");

        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        world.spawnParticle(Particle.TOTEM_OF_UNDYING,
                loc.clone().add(0, 1, 0), 100, 0.5, 1, 0.5, 0.1);

        world.spawnParticle(Particle.HAPPY_VILLAGER,
                loc.clone().add(0, 1, 0), 50, 0.5, 0.5, 0.5, 0.1);

        new BukkitRunnable() {
            int ticks = 0;
            double angle = 0;

            @Override
            public void run() {
                if (ticks >= 60) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 2; i++) {
                    double offsetAngle = angle + (i * Math.PI);
                    double x = Math.cos(offsetAngle) * 0.8;
                    double z = Math.sin(offsetAngle) * 0.8;
                    double y = ticks * 0.08;

                    Location particleLoc = loc.clone().add(x, y, z);
                    world.spawnParticle(Particle.DUST, particleLoc, 1,
                            new Particle.DustOptions(Color.ORANGE, 1.0f));
                    world.spawnParticle(Particle.HAPPY_VILLAGER, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.ENCHANT, particleLoc, 2, 0, 0, 0, 0.5);
                }

                angle += Math.PI / 10;
                ticks++;
            }
        }.runTaskTimer(plugin, 0L, 1L);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 40) {
                    cancel();
                    return;
                }

                double radius = 2.0 * (ticks / 40.0);
                for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                    double x = Math.cos(angle) * radius;
                    double z = Math.sin(angle) * radius;

                    Location particleLoc = loc.clone().add(x, 0.1, z);
                    world.spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);
                    world.spawnParticle(Particle.END_ROD, particleLoc, 1, 0, 0, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 5L, 1L);

        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks >= 30) {
                    cancel();
                    return;
                }

                for (int i = 0; i < 5; i++) {
                    double x = (Math.random() - 0.5) * 2;
                    double z = (Math.random() - 0.5) * 2;
                    double y = 3 + Math.random() * 2;

                    Location particleLoc = loc.clone().add(x, y, z);
                    world.spawnParticle(Particle.FIREWORK, particleLoc, 1, 0, -0.5, 0, 0.1);
                    world.spawnParticle(Particle.GLOW, particleLoc, 1, 0, -0.3, 0, 0);
                }

                ticks++;
            }
        }.runTaskTimer(plugin, 10L, 2L);
    }

    public void reproducirEfectosRuleta(Player player) {
        Location loc = player.getLocation();
        World world = player.getWorld();

        player.playSound(loc, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
        player.playSound(loc, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1.2f);

        world.spawnParticle(Particle.FIREWORK, loc.clone().add(0, 2, 0), 50, 0.5, 0.5, 0.5, 0.2);
        world.spawnParticle(Particle.TOTEM_OF_UNDYING, loc.clone().add(0, 1, 0), 30, 0.3, 0.5, 0.3, 0.1);
    }

    public void reproducirSonidoError(Player player) {
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
    }

    public void reproducirSonidoExito(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1.5f);
    }
}
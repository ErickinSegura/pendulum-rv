package org.delta.listeners.spawns;

import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.libs.builders.ItemBuilder;
import org.delta.libs.builders.MobBuilder;
import org.delta.pendulum;

public class ZombieSpawner implements Listener {

    private final pendulum plugin;

    public ZombieSpawner(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

        new MobBuilder(EntityType.ZOMBIE)
                .setCustomName("&c⚠ Zombie Vengador")
                .setCustomNameVisible(true)
                .setMaxHealth(80)
                .setHealth(80)
                .setGlowing(true)
                .setRemovable(false)
                .setHelmet(new ItemBuilder(Material.IRON_HELMET).build())
                .setMainHand(new ItemBuilder(Material.IRON_SWORD).build())
                .addPotionEffect(new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, 1))
                .setBossBar(plugin, "&c⚠ Zombie Vengador &7| &c❤ Vida", BarColor.RED, BarStyle.SEGMENTED_10)
                .setBossBarRange(40.0)
                .build(event.getEntity().getLocation());
    }
}
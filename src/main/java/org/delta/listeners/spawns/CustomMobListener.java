package org.delta.listeners.spawns;

import net.minecraft.world.entity.PathfinderMob;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.delta.customs.mobs.CustomMob;
import org.delta.customs.mobs.MobRegistry;
import org.delta.libs.nms.NMSEntityUtils;
import org.delta.pendulum;

public class CustomMobListener implements Listener {

    private final pendulum plugin;

    public CustomMobListener(pendulum plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof LivingEntity attacker)) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        String key = attacker.getScoreboardTags().stream()
                .filter(tag -> MobRegistry.getKeys().contains(tag))
                .findFirst().orElse(null);

        if (key == null) return;

        Location dummy = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
        CustomMob mob = MobRegistry.get(key, plugin, dummy).orElse(null);

        if (mob == null || mob.getKnockbackStrength() <= 0) return;

        double dx = attacker.getLocation().getX() - victim.getLocation().getX();
        double dz = attacker.getLocation().getZ() - victim.getLocation().getZ();


        net.minecraft.world.entity.player.Player nmsPlayer =
                (net.minecraft.world.entity.player.Player) ((CraftLivingEntity) victim).getHandle();

        nmsPlayer.knockback(mob.getKnockbackStrength(), dx, dz);

        if (mob.getKnockbackVertical() > 0) {
            final Player finalVictim = victim;
            final double verticalStrength = mob.getKnockbackVertical();
            Bukkit.getScheduler().runTask(plugin, () -> {
                org.bukkit.util.Vector vel = finalVictim.getVelocity();
                vel.setY(verticalStrength);
                finalVictim.setVelocity(vel);
            });
        }
    }
}
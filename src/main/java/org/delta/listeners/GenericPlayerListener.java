package org.delta.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.delta.libs.MessageUtils;
import org.delta.libs.PendulumSettings;
import org.delta.pendulum;

import java.util.Random;

import static org.bukkit.Bukkit.getServer;

public class GenericPlayerListener implements Listener {
    pendulum plugin = pendulum.getInstance();
    private final PendulumSettings settings = PendulumSettings.getInstance();
    private final Random random = new Random();


    // Constantes
    private static final double TOTEM_FAIL_CHANCE = 0.99;

    @EventHandler(priority = EventPriority.HIGH)
    public void onUseTotem(EntityResurrectEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }


        if (!event.isCancelled() && event.getEntity().getLastDamageCause() != null) {

            if (hasTotem(player)) {
                if (settings.getDia() < 20) {
                    getServer().broadcast(MessageUtils.color("&d&l" + player.getName() + "&r&d ha usado un tótem de la inmortalidad!"));
                    return;
                }

                double roll = random.nextDouble();
                int rollPercentage = (int)(roll * 100);

                if (roll >= TOTEM_FAIL_CHANCE) {
                    event.setCancelled(true);

                    player.getWorld().spawnParticle(Particle.LARGE_SMOKE, player.getLocation(), 100, 0.5, 1, 0.5, 0.1);
                    player.getWorld().playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 0.5f);

                    getServer().broadcast(MessageUtils.color("&4&l¡El tótem de " + player.getName() + " ha fallado! " +
                            "\n&c[" + rollPercentage + "% >= 99%]"));

                    Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), () -> {
                        player.setHealth(0);
                    }, 1L);

                    return;
                }

                getServer().broadcast(MessageUtils.color("&d&l" + player.getName() + "&r&d ha usado un tótem de la inmortalidad! " +
                        "\n&7[" + rollPercentage + "% < 99%]"));
            }
        }
    }

    private boolean hasTotem(Player player) {
        Material totemMaterial = Material.TOTEM_OF_UNDYING;
        return player.getInventory().getItemInMainHand().getType() == totemMaterial ||
                player.getInventory().getItemInOffHand().getType() == totemMaterial;
    }
}

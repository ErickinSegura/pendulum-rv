package org.delta.listeners.items;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.managers.achievements.Achievement;
import org.delta.pendulum;

public class PapaExplosivaListener implements Listener {

    private static final NamespacedKey COMIO_PAPA = new NamespacedKey("delta", "comio_papa");
    private Player currentPapaEater = null;

    @EventHandler
    public void onEat(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (!isCustomItem(item, "papa_explosiva")) return;

        player.getPersistentDataContainer().set(COMIO_PAPA, PersistentDataType.BYTE, (byte) 1);
        currentPapaEater = player;

        try {
            net.minecraft.world.level.Level nmsWorld =
                    ((org.bukkit.craftbukkit.CraftWorld) player.getWorld()).getHandle();

            net.minecraft.world.entity.player.Player nmsPlayer =
                    ((org.bukkit.craftbukkit.entity.CraftPlayer) player).getHandle();

            nmsWorld.explode(
                    nmsPlayer,
                    player.getLocation().getX(),
                    player.getLocation().getY() + 1,
                    player.getLocation().getZ(),
                    6f,
                    false,
                    net.minecraft.world.level.Level.ExplosionInteraction.BLOCK
            );

            if (!player.isDead()) {
                player.setHealth(0);
            }
        } finally {
            currentPapaEater = null;
        }

        pendulum.getInstance().getServer().getScheduler().runTaskLater(pendulum.getInstance(), () -> {
            if (player.isOnline() && !player.isDead()
                    && player.getPersistentDataContainer().has(COMIO_PAPA, PersistentDataType.BYTE)) {
                player.getPersistentDataContainer().remove(COMIO_PAPA);
                pendulum.getInstance().getAchievementManager().unlock(player, Achievement.ESTOMAGO_DE_HIERRO);
            }
        }, 1L);
    }

    @EventHandler
    public void onDeath(org.bukkit.event.entity.PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (player.getPersistentDataContainer().has(COMIO_PAPA, PersistentDataType.BYTE)) {
            player.getPersistentDataContainer().remove(COMIO_PAPA);
            event.setDeathMessage(
                    player.getName() +
                            org.bukkit.ChatColor.GRAY + " se suicidó comiendo una Papa Explosiva"
            );
            return;
        }

        if (currentPapaEater != null && player != currentPapaEater) {
            event.setDeathMessage(
                    player.getName() +
                            org.bukkit.ChatColor.GRAY + " fue volado por la Papa Explosiva de " +
                            currentPapaEater.getName()
            );
        }
    }

    private boolean isCustomItem(ItemStack item, String key) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return key.equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING)
        );
    }
}
package org.delta.listeners.items;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.List;


public class VaritaBarreraListener implements Listener {

    private static final Material BARRIER_BLOCK = Material.CYAN_STAINED_GLASS;
    private static final int RADIUS = 3;
    private static final int DURATION_TICKS = 200;   // 10 segundos
    private static final int COOLDOWN_TICKS = 600;   // 30 segundos

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isWand(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasCooldown(item.getType())) return;
        player.setCooldown(item.getType(), COOLDOWN_TICKS);

        List<Block> placed = buildDome(player);

        player.getWorld().playSound(player.getLocation(), Sound.BLOCK_GLASS_PLACE, 0.8f, 1.2f);

        // Revierte los bloques a aire tras la duración, sólo si siguen siendo
        // nuestro cristal (no toca lo que el mundo o los jugadores hayan cambiado).
        pendulum.getInstance().getServer().getScheduler().runTaskLater(pendulum.getInstance(), () -> {
            for (Block block : placed) {
                if (block.getType() == BARRIER_BLOCK) {
                    block.setType(Material.AIR, false);
                }
            }
            if (!placed.isEmpty()) {
                Block first = placed.get(0);
                first.getWorld().playSound(first.getLocation(), Sound.BLOCK_GLASS_BREAK, 0.8f, 1.0f);
            }
        }, DURATION_TICKS);
    }

    /**
     * Coloca una cáscara esférica de cristal alrededor del jugador, ocupando
     * únicamente bloques de aire. Devuelve los bloques realmente colocados.
     */
    private List<Block> buildDome(Player player) {
        List<Block> placed = new ArrayList<>();
        World world = player.getWorld();
        Block feet = player.getLocation().getBlock();
        int cx = feet.getX();
        int cy = feet.getY() + 1;   // centrado a la altura del torso
        int cz = feet.getZ();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                    if (dist < RADIUS - 0.5 || dist > RADIUS + 0.5) continue;

                    Block block = world.getBlockAt(cx + dx, cy + dy, cz + dz);
                    if (!block.getType().isAir()) continue;

                    block.setType(BARRIER_BLOCK, false);
                    placed.add(block);
                }
            }
        }
        return placed;
    }

    private boolean isWand(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "varita_barrera".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

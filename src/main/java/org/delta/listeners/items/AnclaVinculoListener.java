package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scoreboard.Team;
import org.delta.commands.menu.TeamTeleportHolder;
import org.delta.customs.items.CustomItem;
import org.delta.libs.MessageUtils;

import java.util.ArrayList;
import java.util.List;

public class AnclaVinculoListener implements Listener {

    private static final int COOLDOWN_TICKS = 6000;

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (!isAncla(event.getItem())) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasCooldown(Material.RECOVERY_COMPASS)) {
            denied(player, "&cEl Ancla aún se está recargando.");
            return;
        }

        List<Player> mates = teammates(player);
        if (mates.isEmpty()) {
            denied(player, "&cNo tienes aliados conectados.");
            return;
        }

        player.openInventory(new TeamTeleportHolder(mates).getInventory());
        player.playSound(player.getLocation(), Sound.BLOCK_ENDER_CHEST_OPEN, 0.7f, 1.4f);
    }

    @EventHandler
    public void onSelect(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof TeamTeleportHolder)) return;
        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || !clicked.hasItemMeta()) return;

        String name = clicked.getItemMeta().getPersistentDataContainer()
                .get(TeamTeleportHolder.TARGET_KEY, PersistentDataType.STRING);
        if (name == null) return;

        if (player.hasCooldown(Material.RECOVERY_COMPASS)) {
            player.closeInventory();
            return;
        }

        Player target = Bukkit.getPlayerExact(name);
        if (target == null || !target.isOnline()) {
            denied(player, "&cEse aliado ya no está disponible.");
            player.closeInventory();
            return;
        }

        player.closeInventory();

        Location from = player.getLocation();
        from.getWorld().spawnParticle(Particle.PORTAL, from.clone().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.2);
        from.getWorld().playSound(from, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);

        player.teleport(target.getLocation());
        player.setCooldown(Material.RECOVERY_COMPASS, COOLDOWN_TICKS);

        target.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0, 1, 0), 30, 0.4, 0.8, 0.4, 0.2);
        target.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f);
        player.sendMessage(MessageUtils.color("&aViajaste junto a &f" + target.getName()));
    }

    private List<Player> teammates(Player player) {
        List<Player> mates = new ArrayList<>();
        Team team = player.getScoreboard().getEntryTeam(player.getName());
        if (team == null) return mates;
        for (String entry : team.getEntries()) {
            Player mate = Bukkit.getPlayerExact(entry);
            if (mate != null && !mate.equals(player)) mates.add(mate);
        }
        return mates;
    }

    private void denied(Player player, String message) {
        player.sendMessage(MessageUtils.color(message));
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 0.6f, 1.2f);
    }

    private boolean isAncla(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "ancla_vinculo".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

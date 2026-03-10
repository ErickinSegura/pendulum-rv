package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.PlayerLeashEntityEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Team;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

import java.util.HashSet;

public class SharedSpaceListener extends BasePerkListener {

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent e) {
        if (!(e.getWhoClicked() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickupItem(EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDropItem(PlayerDropItemEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onSwapHand(PlayerSwapHandItemsEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemDamage(PlayerItemDamageEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemConsume(PlayerItemConsumeEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPickupArrow(PlayerPickupArrowEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBucketFill(PlayerBucketFillEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDispenseArmor(BlockDispenseArmorEvent e) {
        if (!(e.getTargetEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileLaunch(ProjectileLaunchEvent e) {
        if (!(e.getEntity().getShooter() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.SHARED_SPACE)) return;
        scheduleSync(player);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onArmorStandManipulate(PlayerArmorStandManipulateEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onShear(PlayerShearEntityEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLeash(PlayerLeashEntityEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDeath(PlayerDeathEvent e) {
        if (!hasTeamPerk(e.getEntity(), Perk.SHARED_SPACE)) return;
        scheduleSync(e.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        Bukkit.getScheduler().runTaskLater(pendulum.getInstance(), () -> syncTeamInventory(e.getPlayer()), 1L);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent e) {
        if (!hasTeamPerk(e.getPlayer(), Perk.SHARED_SPACE)) return;
        Bukkit.getScheduler().runTask(pendulum.getInstance(), () -> {
            Team team = getTeam(e.getPlayer());
            if (team == null) return;
            for (String memberName : team.getEntries()) {
                Player member = Bukkit.getPlayerExact(memberName);
                if (member == null || member.equals(e.getPlayer())) continue;
                e.getPlayer().getInventory().setContents(cloneContents(member.getInventory().getContents()));
                e.getPlayer().updateInventory();
                return;
            }
        });
    }

    private void scheduleSync(Player source) {
        Bukkit.getScheduler().runTask(pendulum.getInstance(), () -> syncTeamInventory(source));
    }

    private void syncTeamInventory(Player source) {
        Team team = getTeam(source);
        if (team == null) return;

        ItemStack[] contents = source.getInventory().getContents();

        for (String memberName : new HashSet<>(team.getEntries())) {
            Player member = Bukkit.getPlayerExact(memberName);
            if (member == null || member.equals(source)) continue;

            member.getInventory().setContents(cloneContents(contents));
            member.updateInventory();
        }
    }

    private ItemStack[] cloneContents(ItemStack[] contents) {
        ItemStack[] cloned = new ItemStack[contents.length];
        for (int i = 0; i < contents.length; i++) {
            cloned[i] = contents[i] != null ? contents[i].clone() : null;
        }
        return cloned;
    }
}
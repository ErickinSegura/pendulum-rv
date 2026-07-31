package org.delta.listeners.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.base.PapaExplosiva;
import org.delta.pendulum;

import java.util.*;

public class LanzapapasListener implements Listener {

    private static final long LOAD_TICKS = 25L;
    private final Map<UUID, BukkitRunnable> loading = new HashMap<>();
    private final Map<UUID, Player> papasEnVuelo = new HashMap<>();


    @EventHandler
    public void onCrossbowLoad(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack held = player.getInventory().getItemInMainHand();

        if (!isCustomItem(held, "lanzapapas")) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR &&
                event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        CrossbowMeta meta = (CrossbowMeta) held.getItemMeta();
        if (meta == null || meta.hasChargedProjectiles()) return;

        event.setCancelled(true);

        UUID id = player.getUniqueId();
        if (loading.containsKey(id)) return;
        if (!hasItem(player, "papa_explosiva")) return;

        consumeItem(player, "papa_explosiva");

        int slot = player.getInventory().getHeldItemSlot();
        player.setCooldown(Material.CROSSBOW, (int) LOAD_TICKS);
        player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_START, 1f, 1f);

        BukkitRunnable task = new BukkitRunnable() {
            int elapsed = 0;
            boolean middlePlayed = false;

            @Override
            public void run() {
                elapsed++;

                if (!player.isOnline()) {
                    cancelLoad(player, this, true);
                    return;
                }

                ItemStack current = player.getInventory().getItem(slot);
                if (player.getInventory().getHeldItemSlot() != slot
                        || !isCustomItem(current, "lanzapapas")
                        || !(current.getItemMeta() instanceof CrossbowMeta cm)
                        || cm.hasChargedProjectiles()) {
                    cancelLoad(player, this, true);
                    return;
                }

                if (!middlePlayed && elapsed >= LOAD_TICKS / 2) {
                    middlePlayed = true;
                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_MIDDLE, 1f, 1f);
                }

                if (elapsed >= LOAD_TICKS) {
                    chargeCrossbow(player, slot, current);
                    player.playSound(player.getLocation(), Sound.ITEM_CROSSBOW_LOADING_END, 1f, 1f);
                    loading.remove(player.getUniqueId());
                    cancel();
                }
            }
        };

        loading.put(id, task);
        task.runTaskTimer(pendulum.getInstance(), 1L, 1L);
    }

    @EventHandler
    public void onShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isCustomItem(event.getBow(), "lanzapapas")) return;

        event.setCancelled(true);

        unloadCrossbow(event.getBow());

        Snowball projectile = player.launchProjectile(Snowball.class);
        projectile.setVelocity(event.getProjectile().getVelocity());
        projectile.setItem(new PapaExplosiva().build());
        projectile.getPersistentDataContainer().set(
                new NamespacedKey("delta", "papa_explosiva"),
                PersistentDataType.BYTE,
                (byte) 1
        );

        papasEnVuelo.put(projectile.getUniqueId(), player);

        player.playSound(player.getLocation(), Sound.ENTITY_TNT_PRIMED, 1f, 1f);
        pendulum.getInstance().getAchievementManager().unlock(player, org.delta.managers.achievements.Achievement.FUEGO_DE_PAPAS);
    }

    @EventHandler
    public void onPapaHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Snowball snowball)) return;

        if (!snowball.getPersistentDataContainer().has(
                new NamespacedKey("delta", "papa_explosiva"),
                PersistentDataType.BYTE
        )) return;

        Player shooter = papasEnVuelo.remove(snowball.getUniqueId());
        org.bukkit.Location loc = snowball.getLocation();
        snowball.remove();

        net.minecraft.world.level.Level nmsWorld =
                ((org.bukkit.craftbukkit.CraftWorld) loc.getWorld()).getHandle();

        net.minecraft.world.entity.player.Player nmsPlayer = shooter != null
                ? ((org.bukkit.craftbukkit.entity.CraftPlayer) shooter).getHandle()
                : null;

        nmsWorld.explode(
                nmsPlayer,
                loc.getX(), loc.getY(), loc.getZ(),
                3f,
                false,
                net.minecraft.world.level.Level.ExplosionInteraction.BLOCK
        );

        if (shooter != null) {
            double distance = shooter.getLocation().distance(loc);
            if (distance < 5.0) {
                double damage = Math.max(1.0, 10.0 - (distance * 2));
                shooter.damage(damage);
            }
        }
    }


    private void chargeCrossbow(Player player, int slot, ItemStack current) {
        net.minecraft.world.item.ItemStack nmsItem =
                org.bukkit.craftbukkit.inventory.CraftItemStack.asNMSCopy(current);

        net.minecraft.world.item.ItemStack nmsArrow =
                new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.ARROW);

        net.minecraft.world.item.component.ChargedProjectiles charged =
                net.minecraft.world.item.component.ChargedProjectiles.of(nmsArrow);

        nmsItem.set(
                net.minecraft.core.component.DataComponents.CHARGED_PROJECTILES,
                charged
        );

        ItemStack loadedItem = org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(nmsItem);
        player.getInventory().setItem(slot, loadedItem);
    }

    private void cancelLoad(Player player, BukkitRunnable task, boolean refund) {
        loading.remove(player.getUniqueId());
        task.cancel();
        if (player.isOnline()) {
            player.setCooldown(Material.CROSSBOW, 0);
        }
        if (refund) giveBack(player);
    }

    private void giveBack(Player player) {
        player.getInventory().addItem(new org.delta.customs.items.base.PapaExplosiva().build());
    }

    private boolean isCustomItem(ItemStack item, String key) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return key.equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING)
        );
    }

    private boolean hasItem(Player player, String key) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isCustomItem(item, key)) return true;
        }
        return false;
    }

    private boolean consumeItem(Player player, String key) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (isCustomItem(item, key)) {
                item.setAmount(item.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    private void unloadCrossbow(ItemStack crossbow) {
        if (crossbow == null) return;
        CrossbowMeta meta = (CrossbowMeta) crossbow.getItemMeta();
        if (meta == null) return;
        meta.setChargedProjectiles(null);
        crossbow.setItemMeta(meta);
    }
}

package org.delta.listeners.castigo;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.MessageUtils;
import org.delta.libs.castigo.Castigo;
import org.delta.libs.castigo.TipoCastigo;
import org.delta.managers.castigo.CastigoManager;
import org.delta.pendulum;

public class CastigoListener implements Listener {

    private final CastigoManager castigoManager;

    public CastigoListener(CastigoManager castigoManager) {
        this.castigoManager = castigoManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        castigoManager.reaplicar(event.getPlayer());
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        org.bukkit.Bukkit.getScheduler().runTaskLater(
                pendulum.getInstance(), () -> castigoManager.reaplicar(player), 1L);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        castigoManager.limpiarBarrerasDeDrops(event.getDrops());
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (castigoManager.esBarrera(event.getCurrentItem())
                || castigoManager.esBarrera(event.getCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrag(InventoryDragEvent event) {
        if (castigoManager.esBarrera(event.getOldCursor())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (castigoManager.esBarrera(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwap(PlayerSwapHandItemsEvent event) {
        if (castigoManager.esBarrera(event.getMainHandItem())
                || castigoManager.esBarrera(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && estaProhibido(event.getPlayer(), event.getItem().getType())) {
            event.setCancelled(true);
            avisar(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent event) {
        if (estaProhibido(event.getPlayer(), event.getBlock().getType())) {
            event.setCancelled(true);
            avisar(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onConsume(PlayerItemConsumeEvent event) {
        if (estaProhibido(event.getPlayer(), event.getItem().getType())) {
            event.setCancelled(true);
            avisar(event.getPlayer());
        }
    }

    private boolean estaProhibido(Player player, Material material) {
        Castigo castigo = castigoManager.getCastigo(player.getUniqueId());
        return castigo != null
                && castigo.getTipo() == TipoCastigo.PROHIBIR_ITEM
                && castigo.getMaterial() == material;
    }

    private void avisar(Player player) {
        player.sendActionBar(MessageUtils.color("&cNo puedes usar ese ítem por tu castigo."));
    }
}

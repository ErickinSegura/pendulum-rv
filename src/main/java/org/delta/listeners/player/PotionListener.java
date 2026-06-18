package org.delta.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerAttemptPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.delta.libs.PotionStackUtil;
import org.delta.pendulum;

/**
 * Garantiza que toda poción del servidor lleve el componente {@code max_stack_size}
 * para que se apilen de forma consistente sin importar su origen (destilado, recogido,
 * creativo, comercio, etc.). Beber y lanzar no requieren manejo extra: el sistema de
 * consumibles de 1.21 ya resta 1 del stack y devuelve la botella correctamente.
 */
public class PotionListener implements Listener {

    // Al entrar: normaliza lo que el jugador ya traiga en su inventario.
    @EventHandler(priority = EventPriority.LOW)
    public void onJoin(PlayerJoinEvent event) {
        PotionStackUtil.normalize(event.getPlayer().getInventory());
    }

    // Al aparecer un ítem en el mundo (drop de muerte, destilado tirado, etc.):
    // así las pociones en el suelo ya se apilan entre sí y al recogerlas.
    @EventHandler(priority = EventPriority.LOW)
    public void onItemSpawn(ItemSpawnEvent event) {
        Item entity = event.getEntity();
        ItemStack stack = entity.getItemStack();
        if (PotionStackUtil.normalize(stack)) {
            entity.setItemStack(stack);
        }
    }

    // Red de seguridad por si la poción apareció antes de que el plugin estuviera activo.
    @EventHandler(priority = EventPriority.LOW)
    public void onPickup(PlayerAttemptPickupItemEvent event) {
        Item entity = event.getItem();
        ItemStack stack = entity.getItemStack();
        if (PotionStackUtil.normalize(stack)) {
            entity.setItemStack(stack);
        }
    }

    // Al abrir cualquier inventario: normaliza tanto el contenedor como el del jugador.
    // Cubre cofres, hornos, comercios de aldeano y cualquier poción preexistente.
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryOpen(InventoryOpenEvent event) {
        PotionStackUtil.normalize(event.getInventory());
        PotionStackUtil.normalize(event.getPlayer().getInventory());
    }

    // Cubre la creación de pociones en modo creativo (InventoryCreativeEvent extiende
    // de este) y cualquier ítem que pase por el cursor.
    @EventHandler(priority = EventPriority.LOW)
    public void onInventoryClick(InventoryClickEvent event) {
        ItemStack current = event.getCurrentItem();
        if (PotionStackUtil.normalize(current)) {
            event.setCurrentItem(current);
        }

        ItemStack cursor = event.getCursor();
        if (PotionStackUtil.normalize(cursor)) {
            event.getWhoClicked().setItemOnCursor(cursor);
        }
    }

    // El alambique coloca los resultados DESPUÉS del evento, así que normalizamos
    // el contenido un tick más tarde.
    @EventHandler(priority = EventPriority.LOW)
    public void onBrew(BrewEvent event) {
        Bukkit.getScheduler().runTask(
                pendulum.getInstance(),
                () -> PotionStackUtil.normalize(event.getContents())
        );
    }
}

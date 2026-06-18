package org.delta.listeners.items;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.consumables.ZanahoriaEncantada;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Permite rellenar la Zanahoria Rellenable en la mesa de crafteo: se combina la
 * zanahoria custom con zanahorias normales y el resultado es la misma zanahoria
 * con sus cargas aumentadas (cada zanahoria puesta = 1 carga, hasta el máximo).
 * Como el resultado depende de las cargas actuales y de cuántas zanahorias se
 * pongan, se calcula a mano en lugar de usar una receta de resultado fijo.
 */
public class ZanahoriaRellenableCraftListener implements Listener {

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        CraftingInventory inv = event.getInventory();
        RefillScenario scenario = detect(inv.getMatrix());
        if (scenario == null) return;

        inv.setResult(scenario.preview());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getInventory() instanceof CraftingInventory inv)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack[] matrix = inv.getMatrix();
        RefillScenario scenario = detect(matrix);
        if (scenario == null) return;

        event.setCancelled(true);

        // Consume una zanahoria rellenable del grid.
        ItemStack refill = matrix[scenario.refillSlot];
        if (refill.getAmount() <= 1) {
            matrix[scenario.refillSlot] = new ItemStack(Material.AIR);
        } else {
            ItemStack leftover = refill.clone();
            leftover.setAmount(refill.getAmount() - 1);
            matrix[scenario.refillSlot] = leftover;
        }

        // Consume sólo las zanahorias que realmente caben.
        int toRemove = scenario.used;
        for (int slot : scenario.carrotSlots) {
            if (toRemove <= 0) break;
            ItemStack carrots = matrix[slot];
            if (carrots == null || carrots.getType() != Material.CARROT) continue;
            int take = Math.min(carrots.getAmount(), toRemove);
            toRemove -= take;
            if (take >= carrots.getAmount()) {
                matrix[slot] = new ItemStack(Material.AIR);
            } else {
                ItemStack leftover = carrots.clone();
                leftover.setAmount(carrots.getAmount() - take);
                matrix[slot] = leftover;
            }
        }

        inv.setMatrix(matrix);

        ItemStack result = scenario.preview();
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(result);
        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        // Recalcula el resultado por si quedan ingredientes para otro relleno.
        RefillScenario next = detect(matrix);
        inv.setResult(next != null ? next.preview() : new ItemStack(Material.AIR));

        player.playSound(player.getLocation(), Sound.ENTITY_ITEM_PICKUP, 0.6f, 1.4f);
        player.updateInventory();
    }

    /**
     * Reconoce el patrón de relleno: exactamente una zanahoria rellenable, al
     * menos una zanahoria normal y ningún otro item en el grid.
     */
    private RefillScenario detect(ItemStack[] matrix) {
        int refillSlot = -1;
        ItemStack refillItem = null;
        List<Integer> carrotSlots = new ArrayList<>();
        int totalCarrots = 0;

        for (int slot = 0; slot < matrix.length; slot++) {
            ItemStack item = matrix[slot];
            if (item == null || item.getType() == Material.AIR) continue;

            if (isRefillCarrot(item)) {
                if (refillSlot != -1) return null; // más de una rellenable: inválido
                refillSlot = slot;
                refillItem = item;
            } else if (item.getType() == Material.CARROT && customKey(item) == null) {
                carrotSlots.add(slot);
                totalCarrots += item.getAmount();
            } else {
                return null; // cualquier otro item invalida la combinación
            }
        }

        if (refillSlot == -1 || totalCarrots <= 0) return null;

        int charges = ZanahoriaEncantada.getCharges(refillItem);
        int space = ZanahoriaEncantada.MAX_CHARGES - charges;
        if (space <= 0) return null;

        int used = Math.min(space, totalCarrots);
        return new RefillScenario(refillSlot, refillItem, carrotSlots, charges + used, used);
    }

    private boolean isRefillCarrot(ItemStack item) {
        return "zanahoria_rellenable".equals(customKey(item));
    }

    private String customKey(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING);
    }

    private record RefillScenario(int refillSlot, ItemStack refillItem,
                                  List<Integer> carrotSlots, int resultCharges, int used) {
        ItemStack preview() {
            ItemStack result = refillItem.clone();
            result.setAmount(1);
            ZanahoriaEncantada.setCharges(result, resultCharges);
            return result;
        }
    }
}

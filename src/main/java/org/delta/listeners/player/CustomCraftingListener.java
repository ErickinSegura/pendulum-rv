package org.delta.listeners.player;

import org.bukkit.Bukkit;
import org.bukkit.Material;
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
import org.bukkit.plugin.Plugin;
import org.delta.customs.items.CustomItem;
import org.delta.libs.PendulumSettings;
import org.delta.libs.builders.CustomRecipeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomCraftingListener implements Listener {

    private final List<CustomRecipeBuilder.CustomRecipe> recipes = new ArrayList<>();
    private final Plugin plugin;

    public CustomCraftingListener(Plugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    public void registerRecipe(CustomRecipeBuilder.CustomRecipe recipe) {
        recipes.add(recipe);
    }

    private CustomRecipeBuilder.CustomRecipe matchRecipe(ItemStack[] matrix) {
        int diaActual = PendulumSettings.getInstance().getDia();
        outer:
        for (CustomRecipeBuilder.CustomRecipe recipe : recipes) {
            if (diaActual < recipe.getMinDay()) continue;
            if (diaActual > recipe.getMaxDay()) continue;
            Map<Integer, ItemStack> ingredients = recipe.getIngredients();

            for (int slot = 0; slot < 9; slot++) {
                ItemStack required = ingredients.get(slot);
                ItemStack inSlot = matrix[slot];

                if (required == null || required.getType() == Material.AIR) {
                    // El slot debe estar vacío: ningún ingrediente extra permitido.
                    if (inSlot != null && inSlot.getType() != Material.AIR) continue outer;
                } else {
                    if (!ingredientMatches(required, inSlot)) continue outer;
                }
            }
            return recipe;
        }
        return null;
    }

    /**
     * Un slot coincide con el ingrediente requerido sólo si:
     * - es el mismo material,
     * - tiene al menos la cantidad pedida, y
     * - su identidad de item custom coincide exactamente: si la receta pide un
     *   item custom, el slot debe ser ese mismo custom; si la receta pide un item
     *   base (vanilla), el slot NO puede ser un item custom.
     */
    private boolean ingredientMatches(ItemStack required, ItemStack inSlot) {
        if (inSlot == null || inSlot.getType() == Material.AIR) return false;
        if (inSlot.getType() != required.getType()) return false;
        if (inSlot.getAmount() < required.getAmount()) return false;
        return Objects.equals(customKey(required), customKey(inSlot));
    }

    private String customKey(ItemStack item) {
        if (item == null) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING);
    }

    private int calculateMaxCrafts(ItemStack[] matrix, CustomRecipeBuilder.CustomRecipe match) {
        int max = Integer.MAX_VALUE;
        for (Map.Entry<Integer, ItemStack> entry : match.getIngredients().entrySet()) {
            ItemStack inSlot = matrix[entry.getKey()];
            if (inSlot == null || inSlot.getType() == Material.AIR) return 0;
            max = Math.min(max, inSlot.getAmount() / entry.getValue().getAmount());
        }
        return max == Integer.MAX_VALUE ? 0 : max;
    }

    private void consumeIngredients(CraftingInventory inv, CustomRecipeBuilder.CustomRecipe match, int times) {
        ItemStack[] matrix = inv.getMatrix();
        for (int slot = 0; slot < 9; slot++) {
            ItemStack required = match.getIngredients().get(slot);
            if (required == null) continue;
            ItemStack inSlot = matrix[slot];
            if (inSlot == null || inSlot.getType() == Material.AIR) continue;
            int remaining = inSlot.getAmount() - (required.getAmount() * times);
            if (remaining <= 0) {
                matrix[slot] = new ItemStack(Material.AIR);
            } else {
                // Conservamos el item original (incluido su PDC custom) y sólo
                // reducimos la cantidad sobrante.
                ItemStack leftover = inSlot.clone();
                leftover.setAmount(remaining);
                matrix[slot] = leftover;
            }
        }
        inv.setMatrix(matrix);

        CustomRecipeBuilder.CustomRecipe nextMatch = matchRecipe(matrix);
        inv.setResult(nextMatch != null ? nextMatch.getResult() : new ItemStack(Material.AIR));
    }

    @EventHandler
    public void onPrepare(PrepareItemCraftEvent event) {
        if (event.getInventory().getMatrix().length < 9) return;
        CustomRecipeBuilder.CustomRecipe match = matchRecipe(event.getInventory().getMatrix());
        if (match != null) {
            event.getInventory().setResult(match.getResult());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getSlotType() != InventoryType.SlotType.RESULT) return;
        if (!(event.getInventory() instanceof CraftingInventory craftingInv)) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack[] matrix = craftingInv.getMatrix();
        if (matrix.length < 9) return;
        CustomRecipeBuilder.CustomRecipe match = matchRecipe(matrix);
        if (match == null) return;

        event.setCancelled(true);

        boolean isShiftClick = event.isShiftClick();
        int times = isShiftClick ? calculateMaxCrafts(matrix, match) : 1;
        if (times <= 0) return;

        consumeIngredients(craftingInv, match, times);

        ItemStack result = match.getResult();
        result.setAmount(result.getAmount() * times);
        HashMap<Integer, ItemStack> overflow = player.getInventory().addItem(result);
        overflow.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));

        player.updateInventory();
    }
}
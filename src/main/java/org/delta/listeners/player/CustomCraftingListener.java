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
import org.bukkit.plugin.Plugin;
import org.delta.libs.builders.CustomRecipeBuilder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        outer:
        for (CustomRecipeBuilder.CustomRecipe recipe : recipes) {
            Map<Integer, ItemStack> ingredients = recipe.getIngredients();

            for (int slot = 0; slot < 9; slot++) {
                ItemStack required = ingredients.get(slot);
                ItemStack inSlot = matrix[slot];

                if (required == null || required.getType() == Material.AIR) {
                    if (inSlot != null && inSlot.getType() != Material.AIR) continue outer;
                } else {
                    if (inSlot == null || inSlot.getType() != required.getType()) continue outer;
                    if (inSlot.getAmount() < required.getAmount()) continue outer;
                }
            }
            return recipe;
        }
        return null;
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
            matrix[slot] = remaining <= 0
                    ? new ItemStack(Material.AIR)
                    : new ItemStack(inSlot.getType(), remaining);
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
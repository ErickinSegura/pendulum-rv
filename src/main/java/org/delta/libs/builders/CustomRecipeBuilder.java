package org.delta.libs.builders;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class CustomRecipeBuilder {

    private final String id;
    private ItemStack result;
    private int minDay = 0;
    private final Map<Integer, ItemStack> ingredients = new HashMap<>();

    private CustomRecipeBuilder(String id) {
        this.id = id;
    }

    public static CustomRecipeBuilder of(String id) {
        return new CustomRecipeBuilder(id);
    }

    public CustomRecipeBuilder result(ItemStack result) {
        this.result = result.clone();
        return this;
    }

    public CustomRecipeBuilder result(Material material) {
        this.result = new ItemStack(material);
        return this;
    }

    public CustomRecipeBuilder ingredient(int slot, Material material, int amount) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Slot debe ser entre 0 y 8");
        if (amount < 1) throw new IllegalArgumentException("La cantidad debe ser mayor a 0");
        this.ingredients.put(slot, new ItemStack(material, amount));
        return this;
    }

    public CustomRecipeBuilder ingredient(int slot, ItemStack item) {
        if (slot < 0 || slot > 8) throw new IllegalArgumentException("Slot debe ser entre 0 y 8");
        this.ingredients.put(slot, item.clone());
        return this;
    }

    public CustomRecipeBuilder minDay(int minDay) {
        this.minDay = Math.max(0, minDay);
        return this;
    }

    public CustomRecipe build() {
        if (result == null) throw new IllegalStateException("La receta '" + id + "' no tiene resultado definido");
        if (ingredients.isEmpty()) throw new IllegalStateException("La receta '" + id + "' no tiene ingredientes");
        return new CustomRecipe(id, result, ingredients, minDay);
    }

    public static class CustomRecipe {

        private final String id;
        private final ItemStack result;
        private final Map<Integer, ItemStack> ingredients;
        private final int minDay;

        private CustomRecipe(String id, ItemStack result, Map<Integer, ItemStack> ingredients, int minDay) {
            this.id = id;
            this.result = result;
            this.ingredients = Map.copyOf(ingredients);
            this.minDay = minDay;
        }

        public String getId() { return id; }
        public ItemStack getResult() { return result.clone(); }
        public Map<Integer, ItemStack> getIngredients() { return ingredients; }
        public int getMinDay() { return minDay; }
    }
}
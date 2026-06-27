package org.delta.listeners.items;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.PrepareSmithingEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingInventory;
import org.bukkit.inventory.SmithingTransformRecipe;
import org.delta.customs.items.ItemRegistry;
import org.delta.customs.items.modifier.Modifier;
import org.delta.customs.items.modifier.ModifierRegistry;
import org.delta.pendulum;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModifierSmithingListener implements Listener {

    private final pendulum plugin;
    private final Set<Material> modifierMaterials = new HashSet<>();

    public ModifierSmithingListener(pendulum plugin) {
        this.plugin = plugin;
        registerRecipes();
    }

    private void registerRecipes() {
        RecipeChoice baseChoice = new RecipeChoice.MaterialChoice(damageableMaterials());

        for (Modifier modifier : ModifierRegistry.values()) {
            Material material = ItemRegistry.get(modifier.getKey())
                    .map(item -> item.build().getType())
                    .orElse(null);
            if (material == null) continue;
            modifierMaterials.add(material);

            NamespacedKey key = new NamespacedKey(plugin, "modifier_" + modifier.getKey());
            Bukkit.removeRecipe(key);

            SmithingTransformRecipe recipe = new SmithingTransformRecipe(
                    key,
                    new ItemStack(Material.BARRIER),
                    null,
                    baseChoice,
                    new RecipeChoice.MaterialChoice(material)
            );
            Bukkit.addRecipe(recipe);
        }
    }

    private List<Material> damageableMaterials() {
        List<Material> materials = new ArrayList<>();
        for (Material material : Material.values()) {
            if (material.isLegacy()) continue;
            if (!material.isItem()) continue;
            if (material.getMaxDurability() > 0) materials.add(material);
        }
        return materials;
    }

    @EventHandler
    public void onPrepare(PrepareSmithingEvent event) {
        SmithingInventory inventory = event.getInventory();
        ItemStack addition = inventory.getItem(2);

        if (addition == null || !modifierMaterials.contains(addition.getType())) return;

        event.setResult(null);

        ItemStack template = inventory.getItem(0);
        if (template != null && !template.getType().isAir()) return;

        Modifier modifier = ModifierRegistry.fromItem(addition).orElse(null);
        if (modifier == null) return;

        ItemStack base = inventory.getItem(1);
        if (base == null || !modifier.canApply(base)) return;

        ItemStack result = base.clone();
        result.setAmount(1);
        modifier.apply(result);
        event.setResult(result);
    }
}

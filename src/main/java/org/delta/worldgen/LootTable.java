package org.delta.worldgen;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.delta.customs.items.CustomItem;
import org.delta.customs.items.ItemRegistry;

import java.util.*;

public class LootTable {


    public record Entry(Material material, String customKey, int minCount, int maxCount, int weight) {}

    private final String      id;
    private final int         minRolls;
    private final int         maxRolls;
    private final List<Entry> entries;
    private final int         totalWeight;

    private LootTable(String id, int minRolls, int maxRolls, List<Entry> entries) {
        this.id          = id;
        this.minRolls    = minRolls;
        this.maxRolls    = maxRolls;
        this.entries     = List.copyOf(entries);
        this.totalWeight = entries.stream().mapToInt(Entry::weight).sum();
    }

    public String getId() { return id; }


    public void fill(Inventory inventory, Random random) {
        int rolls = minRolls + (maxRolls > minRolls ? random.nextInt(maxRolls - minRolls + 1) : 0);

        List<Integer> slots = new ArrayList<>(inventory.getSize());
        for (int i = 0; i < inventory.getSize(); i++) slots.add(i);
        Collections.shuffle(slots, random);

        for (int i = 0; i < rolls && i < slots.size(); i++) {
            Entry entry = pickEntry(random);
            if (entry == null) continue;

            int count = entry.minCount()
                    + (entry.maxCount() > entry.minCount()
                    ? random.nextInt(entry.maxCount() - entry.minCount() + 1)
                    : 0);

            ItemStack stack;
            if (entry.customKey() != null) {
                stack = ItemRegistry.get(entry.customKey()).map(CustomItem::build).orElse(null);
                if (stack == null) continue;
                stack.setAmount(Math.min(count, stack.getMaxStackSize()));
            } else {
                stack = new ItemStack(entry.material(), count);
            }

            inventory.setItem(slots.get(i), stack);
        }
    }


    private Entry pickEntry(Random random) {
        if (totalWeight <= 0 || entries.isEmpty()) return null;
        int roll       = random.nextInt(totalWeight);
        int cumulative = 0;
        for (Entry e : entries) {
            cumulative += e.weight();
            if (roll < cumulative) return e;
        }
        return entries.get(entries.size() - 1);
    }


    public static class Builder {
        private final String       id;
        private       int          minRolls = 3;
        private       int          maxRolls = 6;
        private final List<Entry>  entries  = new ArrayList<>();

        public Builder(String id) { this.id = id; }

        public Builder rolls(int min, int max) {
            if (min < 0 || max < min) throw new IllegalArgumentException("rolls inválidos");
            this.minRolls = min;
            this.maxRolls = max;
            return this;
        }


        public Builder entry(Material material, int minCount, int maxCount, int weight) {
            validate(minCount, maxCount, weight);
            entries.add(new Entry(material, null, minCount, maxCount, weight));
            return this;
        }

        public Builder entryCustom(String customKey, int minCount, int maxCount, int weight) {
            validate(minCount, maxCount, weight);
            entries.add(new Entry(null, customKey, minCount, maxCount, weight));
            return this;
        }

        private void validate(int minCount, int maxCount, int weight) {
            if (weight <= 0)        throw new IllegalArgumentException("weight debe ser > 0");
            if (minCount < 1)       throw new IllegalArgumentException("minCount debe ser >= 1");
            if (maxCount < minCount) throw new IllegalArgumentException("maxCount < minCount");
        }

        public LootTable build() {
            if (entries.isEmpty()) throw new IllegalStateException("LootTable sin entradas: " + id);
            return new LootTable(id, minRolls, maxRolls, entries);
        }
    }
}
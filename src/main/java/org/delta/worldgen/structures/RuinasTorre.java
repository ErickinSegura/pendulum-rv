package org.delta.worldgen.structures;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Biome;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.delta.worldgen.LootTable;
import org.delta.worldgen.StructureDef;
import org.delta.worldgen.StructureTemplate;

public class RuinasTorre extends StructureTemplate {

    /** Loot table compartida para la torre (podría ser static final si quieres reutilizarla). */
    private static final LootTable LOOT_RUINAS = new LootTable.Builder("ruinas_torre_cofre")
            .rolls(3, 7)
            // Comida — muy común
            .entry(Material.BREAD,           1, 3, 60)
            .entry(Material.APPLE,           1, 2, 40)
            // Recursos — común
            .entry(Material.IRON_INGOT,      1, 3, 35)
            .entry(Material.GOLD_INGOT,      1, 2, 20)
            .entry(Material.COAL,            2, 6, 30)
            // Equipo — poco común
            .entry(Material.IRON_SWORD,      1, 1, 12)
            .entry(Material.IRON_HELMET,     1, 1, 10)
            .entry(Material.LEATHER_CHESTPLATE, 1, 1, 15)
            // Libros y rarezas
            .entry(Material.BOOK,            1, 2, 20)
            .entry(Material.COMPASS,         1, 1,  8)
            // Raro
            .entry(Material.DIAMOND,         1, 1,  4)
            .entry(Material.GOLDEN_APPLE,    1, 1,  3)
            .build();

    @Override
    public StructureDef build() {
        return new StructureDef.Builder("ruinas_torre")
                .spawnChance(0.9)
                .biomes(
                        Biome.PLAINS,
                        Biome.SUNFLOWER_PLAINS,
                        Biome.FOREST,
                        Biome.BIRCH_FOREST,
                        Biome.OLD_GROWTH_BIRCH_FOREST
                )

                // ── Estructura ─────────────────────────────────────────────────
                .layer(0, 0, 4, 0, 4, Material.COBBLESTONE)
                .layer(0, 1, 3, 1, 3, Material.STONE_BRICKS)
                .column(0, 1, 5, 0, Material.STONE_BRICKS)
                .column(4, 1, 3, 0, Material.CRACKED_STONE_BRICKS)
                .column(0, 1, 3, 4, Material.CRACKED_STONE_BRICKS)
                .column(4, 1, 4, 4, Material.STONE_BRICKS)
                .block(1, 1, 0, Material.STONE_BRICK_WALL)
                .block(2, 1, 0, Material.STONE_BRICK_WALL)
                .block(3, 1, 0, Material.STONE_BRICK_WALL)
                .block(1, 2, 0, Material.STONE_BRICK_WALL)
                .block(2, 2, 0, Material.STONE_BRICK_WALL)
                .block(0, 1, 1, Material.STONE_BRICK_WALL)
                .block(0, 1, 2, Material.STONE_BRICK_WALL)
                .block(0, 1, 3, Material.STONE_BRICK_WALL)
                .block(5, 0, 2, Material.COBBLESTONE)
                .block(-1, 0, 1, Material.MOSSY_COBBLESTONE)
                .block(2, 0, 5, Material.MOSSY_COBBLESTONE)

                // ── Cofre con loot (dentro de la torre, planta baja) ───────────
                .chest(2, 1, 2, LOOT_RUINAS)

                // ── Guardián zombie con nombre y equipo ────────────────────────
                .entity(2, 2, 2, EntityType.ZOMBIE, entity -> {
                    Zombie zombie = (Zombie) entity;
                    zombie.customName(Component.text("§cGuardián de las Ruinas"));
                    zombie.setCustomNameVisible(true);
                    zombie.setShouldBurnInDay(false);         // no muere de día
                    zombie.getEquipment().setHelmet(new ItemStack(Material.IRON_HELMET));
                    zombie.getEquipment().setItemInMainHand(new ItemStack(Material.IRON_SWORD));
                    // Más vida: +10 corazones extra
                    var maxHealth = zombie.getAttribute(Attribute.MAX_HEALTH);
                    if (maxHealth != null) maxHealth.setBaseValue(40.0);
                    zombie.setHealth(40.0);
                })

                .build();
    }
}
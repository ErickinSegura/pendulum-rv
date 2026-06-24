package org.delta.listeners.spawns;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.managers.ArmorTrimManager;

import java.util.EnumSet;
import java.util.Set;

public class MobEquipmentListener extends BaseMobSpawnListener {

    private static final int DIA_MINIMO = 10;
    private static final double TOTEM_CHANCE = 0.20;

    private static final Set<EntityType> ARMOR_WEARERS = EnumSet.of(
            EntityType.ZOMBIE,
            EntityType.ZOMBIE_VILLAGER,
            EntityType.HUSK,
            EntityType.DROWNED,
            EntityType.ZOMBIFIED_PIGLIN,
            EntityType.SKELETON,
            EntityType.STRAY,
            EntityType.BOGGED,
            EntityType.WITHER_SKELETON,
            EntityType.PIGLIN,
            EntityType.PIGLIN_BRUTE
    );

    private static final Material[][] ARMOR_TIERS = {
            { Material.LEATHER_HELMET, Material.LEATHER_CHESTPLATE, Material.LEATHER_LEGGINGS, Material.LEATHER_BOOTS },
            { Material.GOLDEN_HELMET, Material.GOLDEN_CHESTPLATE, Material.GOLDEN_LEGGINGS, Material.GOLDEN_BOOTS },
            { Material.CHAINMAIL_HELMET, Material.CHAINMAIL_CHESTPLATE, Material.CHAINMAIL_LEGGINGS, Material.CHAINMAIL_BOOTS },
            { Material.IRON_HELMET, Material.IRON_CHESTPLATE, Material.IRON_LEGGINGS, Material.IRON_BOOTS },
            { Material.DIAMOND_HELMET, Material.DIAMOND_CHESTPLATE, Material.DIAMOND_LEGGINGS, Material.DIAMOND_BOOTS },
            { Material.NETHERITE_HELMET, Material.NETHERITE_CHESTPLATE, Material.NETHERITE_LEGGINGS, Material.NETHERITE_BOOTS }
    };

    @EventHandler(ignoreCancelled = true)
    public void onSpawn(CreatureSpawnEvent event) {
        if (!canModify(event, DIA_MINIMO)) return;

        LivingEntity entity = event.getEntity();
        if (!ARMOR_WEARERS.contains(entity.getType())) return;

        EntityEquipment eq = entity.getEquipment();
        if (eq == null) return;

        Material[] tier = ARMOR_TIERS[random.nextInt(ARMOR_TIERS.length)];
        TrimPattern pattern = ArmorTrimManager.patronAleatorio(random);
        TrimMaterial trimMaterial = ArmorTrimManager.materialAleatorio(random);

        eq.setHelmet(conTrim(tier[0], pattern, trimMaterial));
        eq.setChestplate(conTrim(tier[1], pattern, trimMaterial));
        eq.setLeggings(conTrim(tier[2], pattern, trimMaterial));
        eq.setBoots(conTrim(tier[3], pattern, trimMaterial));

        eq.setHelmetDropChance(0f);
        eq.setChestplateDropChance(0f);
        eq.setLeggingsDropChance(0f);
        eq.setBootsDropChance(0f);

        aplicarEfectoTrim(entity, trimMaterial);

        if (random.nextDouble() < TOTEM_CHANCE) {
            eq.setItemInOffHand(new ItemStack(Material.TOTEM_OF_UNDYING));
            eq.setItemInOffHandDropChance(0f);
        }
    }

    private ItemStack conTrim(Material material, TrimPattern pattern, TrimMaterial trimMaterial) {
        ItemStack item = new ItemStack(material);
        if (item.getItemMeta() instanceof ArmorMeta meta) {
            meta.setTrim(new ArmorTrim(trimMaterial, pattern));
            item.setItemMeta(meta);
        }
        return item;
    }

    private void aplicarEfectoTrim(LivingEntity entity, TrimMaterial trimMaterial) {
        PotionEffectType tipo = ArmorTrimManager.efectoDe(trimMaterial);
        if (tipo == null) return;

        entity.addPotionEffect(new PotionEffect(
                tipo, PotionEffect.INFINITE_DURATION, 0, true, false, false));
    }
}

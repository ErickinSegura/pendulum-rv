package org.delta.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.delta.libs.PendulumSettings;
import org.delta.managers.achievements.Achievement;
import org.delta.pendulum;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ArmorTrimManager {

    private static final int DIA_MINIMO = 5;
    private static final int DURATION = 60;
    private static final long INTERVAL = 20L;

    private static final Map<TrimMaterial, PotionEffectType> EFECTOS = new HashMap<>();
    private static final TrimMaterial[] MATERIALES;
    private static final TrimPattern[] PATRONES = {
            TrimPattern.SENTRY,
            TrimPattern.DUNE,
            TrimPattern.COAST,
            TrimPattern.WILD,
            TrimPattern.WARD,
            TrimPattern.EYE,
            TrimPattern.VEX,
            TrimPattern.TIDE,
            TrimPattern.SNOUT,
            TrimPattern.RIB,
            TrimPattern.SPIRE,
            TrimPattern.WAYFINDER,
            TrimPattern.SHAPER,
            TrimPattern.SILENCE,
            TrimPattern.RAISER,
            TrimPattern.HOST,
            TrimPattern.FLOW,
            TrimPattern.BOLT
    };

    static {
        EFECTOS.put(TrimMaterial.QUARTZ, PotionEffectType.JUMP_BOOST);
        EFECTOS.put(TrimMaterial.IRON, PotionEffectType.FIRE_RESISTANCE);
        EFECTOS.put(TrimMaterial.NETHERITE, PotionEffectType.RESISTANCE);
        EFECTOS.put(TrimMaterial.REDSTONE, PotionEffectType.SPEED);
        EFECTOS.put(TrimMaterial.COPPER, PotionEffectType.SLOW_FALLING);
        EFECTOS.put(TrimMaterial.GOLD, PotionEffectType.HASTE);
        EFECTOS.put(TrimMaterial.EMERALD, PotionEffectType.INVISIBILITY);
        EFECTOS.put(TrimMaterial.DIAMOND, PotionEffectType.STRENGTH);
        EFECTOS.put(TrimMaterial.LAPIS, PotionEffectType.WATER_BREATHING);
        EFECTOS.put(TrimMaterial.AMETHYST, PotionEffectType.NIGHT_VISION);
        MATERIALES = EFECTOS.keySet().toArray(new TrimMaterial[0]);
    }

    public static PotionEffectType efectoDe(TrimMaterial material) {
        return EFECTOS.get(material);
    }

    public static TrimMaterial materialAleatorio(Random random) {
        return MATERIALES[random.nextInt(MATERIALES.length)];
    }

    public static TrimPattern patronAleatorio(Random random) {
        return PATRONES[random.nextInt(PATRONES.length)];
    }

    private final pendulum plugin;

    public ArmorTrimManager(pendulum plugin) {
        this.plugin = plugin;
        iniciar();
    }

    private void iniciar() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (PendulumSettings.getInstance().getDia() < DIA_MINIMO) return;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    aplicarEfectos(player);
                }
            }
        }.runTaskTimer(plugin, INTERVAL, INTERVAL);
    }

    private void aplicarEfectos(Player player) {
        TrimMaterial material = null;

        for (ItemStack pieza : player.getInventory().getArmorContents()) {
            if (pieza == null) return;
            if (!(pieza.getItemMeta() instanceof ArmorMeta meta) || !meta.hasTrim()) return;
            ArmorTrim trim = meta.getTrim();
            if (material == null) material = trim.getMaterial();
            else if (!material.equals(trim.getMaterial())) return;
        }

        if (material == null) return;
        PotionEffectType tipo = EFECTOS.get(material);
        if (tipo == null) return;

        player.addPotionEffect(new PotionEffect(tipo, DURATION, 0, true, false, true));
        plugin.getAchievementManager().unlock(player, Achievement.ALTA_COSTURA);
    }
}

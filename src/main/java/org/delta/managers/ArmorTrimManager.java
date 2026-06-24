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
import java.util.Set;

public class ArmorTrimManager {

    private static final int DIA_MINIMO = 5;
    private static final int DURATION = 60;
    private static final long INTERVAL = 20L;

    private static final Map<TrimPattern, PotionEffectType> EFECTOS = new HashMap<>();
    private static final Set<TrimMaterial> MATERIALES_VALIOSOS = Set.of(
            TrimMaterial.DIAMOND,
            TrimMaterial.NETHERITE
    );
    private static final TrimPattern[] PATRONES;
    private static final TrimMaterial[] MATERIALES = {
            TrimMaterial.QUARTZ,
            TrimMaterial.IRON,
            TrimMaterial.NETHERITE,
            TrimMaterial.REDSTONE,
            TrimMaterial.COPPER,
            TrimMaterial.GOLD,
            TrimMaterial.EMERALD,
            TrimMaterial.DIAMOND,
            TrimMaterial.LAPIS,
            TrimMaterial.AMETHYST
    };

    static {
        EFECTOS.put(TrimPattern.SENTRY, PotionEffectType.RESISTANCE);
        EFECTOS.put(TrimPattern.DUNE, PotionEffectType.SPEED);
        EFECTOS.put(TrimPattern.COAST, PotionEffectType.DOLPHINS_GRACE);
        EFECTOS.put(TrimPattern.WILD, PotionEffectType.REGENERATION);
        EFECTOS.put(TrimPattern.WARD, PotionEffectType.ABSORPTION);
        EFECTOS.put(TrimPattern.EYE, PotionEffectType.NIGHT_VISION);
        EFECTOS.put(TrimPattern.VEX, PotionEffectType.INVISIBILITY);
        EFECTOS.put(TrimPattern.TIDE, PotionEffectType.WATER_BREATHING);
        EFECTOS.put(TrimPattern.SNOUT, PotionEffectType.FIRE_RESISTANCE);
        EFECTOS.put(TrimPattern.RIB, PotionEffectType.STRENGTH);
        EFECTOS.put(TrimPattern.SPIRE, PotionEffectType.JUMP_BOOST);
        EFECTOS.put(TrimPattern.WAYFINDER, PotionEffectType.SLOW_FALLING);
        EFECTOS.put(TrimPattern.SHAPER, PotionEffectType.HASTE);
        EFECTOS.put(TrimPattern.SILENCE, PotionEffectType.LUCK);
        EFECTOS.put(TrimPattern.RAISER, PotionEffectType.HEALTH_BOOST);
        EFECTOS.put(TrimPattern.HOST, PotionEffectType.HERO_OF_THE_VILLAGE);
        EFECTOS.put(TrimPattern.FLOW, PotionEffectType.CONDUIT_POWER);
        EFECTOS.put(TrimPattern.BOLT, PotionEffectType.SATURATION);
        PATRONES = EFECTOS.keySet().toArray(new TrimPattern[0]);
    }

    public static PotionEffectType efectoDe(TrimPattern patron) {
        return EFECTOS.get(patron);
    }

    public static boolean esValioso(TrimMaterial material) {
        return MATERIALES_VALIOSOS.contains(material);
    }

    public static TrimPattern patronAleatorio(Random random) {
        return PATRONES[random.nextInt(PATRONES.length)];
    }

    public static TrimMaterial materialAleatorio(Random random) {
        return MATERIALES[random.nextInt(MATERIALES.length)];
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
        TrimPattern patron = null;
        boolean todoValioso = true;

        for (ItemStack pieza : player.getInventory().getArmorContents()) {
            if (pieza == null) return;
            if (!(pieza.getItemMeta() instanceof ArmorMeta meta) || !meta.hasTrim()) return;
            ArmorTrim trim = meta.getTrim();
            if (patron == null) patron = trim.getPattern();
            else if (!patron.equals(trim.getPattern())) return;
            if (!MATERIALES_VALIOSOS.contains(trim.getMaterial())) todoValioso = false;
        }

        if (patron == null) return;
        PotionEffectType tipo = EFECTOS.get(patron);
        if (tipo == null) return;

        int amplificador = todoValioso ? 1 : 0;
        player.addPotionEffect(new PotionEffect(tipo, DURATION, amplificador, true, false, true));
        plugin.getAchievementManager().unlock(player, Achievement.ALTA_COSTURA);
        if (todoValioso) {
            plugin.getAchievementManager().unlock(player, Achievement.COSTURA_DE_LUJO);
        }
    }
}

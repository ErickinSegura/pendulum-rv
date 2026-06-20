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
import org.delta.pendulum;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ArmorTrimManager {

    private static final int DIA_MINIMO = 5;
    private static final int DURATION = 60;
    private static final long INTERVAL = 20L;

    private final pendulum plugin;
    private final Map<TrimPattern, PotionEffectType> efectos = new HashMap<>();
    private final Set<TrimMaterial> materialesValiosos = Set.of(
            TrimMaterial.DIAMOND,
            TrimMaterial.NETHERITE
    );

    public ArmorTrimManager(pendulum plugin) {
        this.plugin = plugin;
        registrarEfectos();
        iniciar();
    }

    private void registrarEfectos() {
        efectos.put(TrimPattern.SENTRY, PotionEffectType.RESISTANCE);
        efectos.put(TrimPattern.DUNE, PotionEffectType.SPEED);
        efectos.put(TrimPattern.COAST, PotionEffectType.DOLPHINS_GRACE);
        efectos.put(TrimPattern.WILD, PotionEffectType.REGENERATION);
        efectos.put(TrimPattern.WARD, PotionEffectType.ABSORPTION);
        efectos.put(TrimPattern.EYE, PotionEffectType.NIGHT_VISION);
        efectos.put(TrimPattern.VEX, PotionEffectType.INVISIBILITY);
        efectos.put(TrimPattern.TIDE, PotionEffectType.WATER_BREATHING);
        efectos.put(TrimPattern.SNOUT, PotionEffectType.FIRE_RESISTANCE);
        efectos.put(TrimPattern.RIB, PotionEffectType.STRENGTH);
        efectos.put(TrimPattern.SPIRE, PotionEffectType.JUMP_BOOST);
        efectos.put(TrimPattern.WAYFINDER, PotionEffectType.SLOW_FALLING);
        efectos.put(TrimPattern.SHAPER, PotionEffectType.HASTE);
        efectos.put(TrimPattern.SILENCE, PotionEffectType.LUCK);
        efectos.put(TrimPattern.RAISER, PotionEffectType.HEALTH_BOOST);
        efectos.put(TrimPattern.HOST, PotionEffectType.HERO_OF_THE_VILLAGE);
        efectos.put(TrimPattern.FLOW, PotionEffectType.CONDUIT_POWER);
        efectos.put(TrimPattern.BOLT, PotionEffectType.SATURATION);
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
            if (!materialesValiosos.contains(trim.getMaterial())) todoValioso = false;
        }

        if (patron == null) return;
        PotionEffectType tipo = efectos.get(patron);
        if (tipo == null) return;

        int amplificador = todoValioso ? 1 : 0;
        player.addPotionEffect(new PotionEffect(tipo, DURATION, amplificador, true, false, true));
    }
}

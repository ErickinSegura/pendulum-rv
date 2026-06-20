package org.delta.listeners.items;

import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.delta.customs.items.CustomItem;
import org.delta.pendulum;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


public class FrenesiListener implements Listener {

    private static final int DURATION_TICKS = 160;   // 8 segundos
    private static final int COOLDOWN_TICKS = 400;    // 20 segundos
    private static final int SPEED_AMP = 1;           // Velocidad II
    private static final int STRENGTH_AMP = 1;        // Fuerza II
    private static final double DAMAGE_TAKEN_MULTIPLIER = 1.5;   // +50% de daño recibido

    private final Set<UUID> berserk = new HashSet<>();

    @EventHandler
    public void onUse(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isFrenesi(item)) return;

        Player player = event.getPlayer();
        event.setCancelled(true);

        if (player.hasCooldown(item.getType())) return;
        player.setCooldown(item.getType(), COOLDOWN_TICKS);

        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, DURATION_TICKS, SPEED_AMP, false, true));
        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, DURATION_TICKS, STRENGTH_AMP, false, true));

        UUID id = player.getUniqueId();
        berserk.add(id);
        pendulum.getInstance().getServer().getScheduler().runTaskLater(
                pendulum.getInstance(), () -> berserk.remove(id), DURATION_TICKS);

        player.getWorld().spawnParticle(Particle.LAVA, player.getLocation().add(0, 1, 0), 20);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_RAVAGER_ROAR, 1.0f, 1.2f);
        pendulum.getInstance().getAchievementManager().unlock(player, org.delta.managers.achievements.Achievement.MODO_FRENESI);
    }

    @EventHandler
    public void onBerserkDamaged(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!berserk.contains(player.getUniqueId())) return;
        event.setDamage(event.getDamage() * DAMAGE_TAKEN_MULTIPLIER);
    }

    @EventHandler
    public void onBerserkKill(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null || !berserk.contains(killer.getUniqueId())) return;
        pendulum.getInstance().getAchievementManager()
                .unlock(killer, org.delta.managers.achievements.Achievement.FURIA_DESATADA);
    }

    private boolean isFrenesi(ItemStack item) {
        if (item == null) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        return "frenesi".equals(
                meta.getPersistentDataContainer().get(CustomItem.ITEM_KEY, PersistentDataType.STRING));
    }
}

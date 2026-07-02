package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;
import org.delta.pendulum;

import java.util.ArrayList;

public class PiesLigerosListener extends BasePerkListener {

    private static final NamespacedKey SPEED_KEY = new NamespacedKey("delta", "perk_pies_ligeros");
    private static final double SPEED_BONUS = 0.2;

    public PiesLigerosListener() {
        Bukkit.getScheduler().runTaskTimer(pendulum.getInstance(), () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                updateSpeed(player);
            }
        }, 0L, 40L);
    }

    private void updateSpeed(Player player) {
        AttributeInstance instance = player.getAttribute(Attribute.MOVEMENT_SPEED);
        if (instance == null) return;

        AttributeModifier current = null;
        for (AttributeModifier modifier : new ArrayList<>(instance.getModifiers())) {
            if (modifier.getKey().equals(SPEED_KEY)) {
                current = modifier;
                break;
            }
        }

        boolean has = hasTeamPerk(player, Perk.PIES_LIGEROS);
        if (has && current == null) {
            instance.addModifier(new AttributeModifier(
                    SPEED_KEY, SPEED_BONUS, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.ANY));
        } else if (!has && current != null) {
            instance.removeModifier(current);
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!hasTeamPerk(player, Perk.PIES_LIGEROS)) return;
        event.setCancelled(true);
    }
}

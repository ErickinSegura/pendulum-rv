package org.delta.listeners.perks.impl;

import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.scoreboard.Team;
import org.delta.listeners.perks.BasePerkListener;
import org.delta.managers.perks.Perk;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VinculoDolorosoListener extends BasePerkListener {

    private static final double SHARE_PERCENT = 0.35;

    private final Set<UUID> procesando = new HashSet<>();

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (procesando.contains(victim.getUniqueId())) return;
        if (!hasTeamPerk(victim, Perk.VINCULO_DOLOROSO)) return;

        double shared = event.getFinalDamage() * SHARE_PERCENT;
        if (shared <= 0) return;

        Team team = getTeam(victim);
        if (team == null) return;

        for (String memberName : team.getEntries()) {
            Player member = Bukkit.getPlayerExact(memberName);
            if (member == null || member.equals(victim) || member.isDead()) continue;

            procesando.add(member.getUniqueId());
            try {
                member.damage(shared);
                member.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                        member.getLocation().add(0, 1, 0), 5, 0.3, 0.3, 0.3, 0);
            } finally {
                procesando.remove(member.getUniqueId());
            }
        }
    }
}

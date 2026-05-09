package org.delta;

import org.bukkit.plugin.PluginManager;
import org.delta.listeners.bingo.*;
import org.delta.listeners.chargebase.AtacanteBehaviorListener;
import org.delta.listeners.chargebase.ChargeBaseDeathListener;
import org.delta.listeners.chargebase.ChargeBaseZoneListener;
import org.delta.listeners.chargebase.mobs.ControladorArrowListener;
import org.delta.listeners.chargebase.mobs.ControladorAvanzadoListener;
import org.delta.listeners.chargebase.mobs.DefensorBehaviorListener;
import org.delta.listeners.chargebase.mobs.HealerBehaviorListener;
import org.delta.listeners.death.DeathListener;
import org.delta.listeners.perks.*;
import org.delta.listeners.perks.impl.*;
import org.delta.listeners.player.*;
import org.delta.listeners.spawns.CustomMobListener;
import org.delta.listeners.spawns.PolarBear;
import org.delta.listeners.spawns.ZombieSpawner;
import org.delta.listeners.teamChest.TeamChestListener;
import org.delta.managers.death.LifeManager;

public class EventRegistry {

    private final pendulum plugin;
    private final LifeManager lifeManager;

    public EventRegistry(pendulum plugin, LifeManager lifeManager) {
        this.plugin = plugin;
        this.lifeManager = lifeManager;
    }

    public void registerAll() {
        PluginManager pm = plugin.getServer().getPluginManager();
        ChargeBaseZoneListener zoneListener = new ChargeBaseZoneListener(plugin.getChargeBaseManager());
        plugin.getChargeBaseManager().setZoneListener(zoneListener);

        // Player
        pm.registerEvents(new RetoListener(), plugin);
        pm.registerEvents(new LifeListener(lifeManager), plugin);
        pm.registerEvents(new DeathListener(lifeManager), plugin);
        pm.registerEvents(new TotemListener(), plugin);
        pm.registerEvents(new PotionListener(), plugin);
        pm.registerEvents(new BedListener(), plugin);
        pm.registerEvents(new JoinLeaveListener(lifeManager), plugin);

        // ChargeBase
        pm.registerEvents(new ChargeBaseDeathListener(plugin.getChargeBaseManager()), plugin);
        pm.registerEvents(zoneListener, plugin);
        pm.registerEvents(new DefensorBehaviorListener(), plugin);
        pm.registerEvents(new ControladorArrowListener(), plugin);
        pm.registerEvents(new ControladorAvanzadoListener(plugin), plugin);
        pm.registerEvents(new HealerBehaviorListener(plugin, plugin.getChargeBaseManager()), plugin);
        pm.registerEvents(new AtacanteBehaviorListener(plugin), plugin);

        // Perks
        pm.registerEvents(new PerkListener(), plugin);
        pm.registerEvents(new LastStandListener(), plugin);
        pm.registerEvents(new FumbleListener(), plugin);
        pm.registerEvents(new SharedSpaceListener(), plugin);
        pm.registerEvents(new LifestealListener(), plugin);

        // TeamChest
        pm.registerEvents(new TeamChestListener(), plugin);

        // Bingo
        pm.registerEvents(new BingoInventoryListener(), plugin);
        pm.registerEvents(new BingoCollectListener(), plugin);
        pm.registerEvents(new BingoKillListener(), plugin);
        pm.registerEvents(new BingoMineListener(), plugin);

        // Spawns
        pm.registerEvents(new ZombieSpawner(plugin), plugin);
        pm.registerEvents(new PolarBear(plugin), plugin);
        pm.registerEvents(new CustomMobListener(plugin), plugin);
    }
}
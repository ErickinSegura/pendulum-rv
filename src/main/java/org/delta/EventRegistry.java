package org.delta;

import org.bukkit.plugin.PluginManager;
import org.delta.commands.subcommand.StructureDevCommand;
import org.delta.listeners.achievements.AchievementListener;
import org.delta.listeners.bingo.*;
import org.delta.listeners.boss.GuardianForjaListener;
import org.delta.listeners.boss.CustodioVacioListener;
import org.delta.listeners.castigo.CastigoListener;
import org.delta.listeners.chargebase.AtacanteBehaviorListener;
import org.delta.listeners.chargebase.ChargeBaseDeathListener;
import org.delta.listeners.chargebase.ChargeBaseZoneListener;
import org.delta.listeners.chargebase.mobs.*;
import org.delta.listeners.death.DeathListener;
import org.delta.listeners.items.ClawnListener;
import org.delta.listeners.items.GiveMenuListener;
import org.delta.listeners.items.LanzapapasListener;
import org.delta.listeners.items.LazoListener;
import org.delta.listeners.items.VaritaCurativaListener;
import org.delta.listeners.items.VaritaBarreraListener;
import org.delta.listeners.items.VaritaBarreraMejoradaListener;
import org.delta.listeners.items.FrenesiListener;
import org.delta.listeners.items.ZanahoriaRellenableListener;
import org.delta.listeners.items.ZanahoriaRellenableCraftListener;
import org.delta.listeners.items.PapaExplosivaListener;
import org.delta.listeners.items.ModifierSmithingListener;
import org.delta.listeners.items.AnclaVinculoListener;
import org.delta.listeners.items.FrascoVacioListener;
import org.delta.listeners.perks.*;
import org.delta.listeners.perks.impl.*;
import org.delta.listeners.player.*;
import org.delta.listeners.spawns.CreeperVariantListener;
import org.delta.listeners.spawns.CustomMobListener;
import org.delta.listeners.spawns.DragonaVacioListener;
import org.delta.listeners.spawns.EndCreeperListener;
import org.delta.listeners.spawns.EndermanHostileListener;
import org.delta.listeners.spawns.MobEquipmentListener;
import org.delta.listeners.spawns.PolarBearListener;
import org.delta.listeners.spawns.UpgradedMobListener;
import org.delta.listeners.spawns.ZombieSpawner;
import org.delta.listeners.teamChest.TeamChestListener;
import org.delta.listeners.worldgen.WorldGenListener;
import org.delta.managers.death.CombatTagManager;
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
        pm.registerEvents(new CombatLogListener(lifeManager, new CombatTagManager()), plugin);
        pm.registerEvents(new BanProtectionListener(), plugin);
        pm.registerEvents(new EndAccessListener(), plugin);

        // Castigos
        pm.registerEvents(new CastigoListener(plugin.getCastigoManager()), plugin);

        // Achievements
        pm.registerEvents(new AchievementListener(plugin.getAchievementManager()), plugin);

        // ChargeBase
        pm.registerEvents(new ChargeBaseDeathListener(plugin.getChargeBaseManager()), plugin);
        pm.registerEvents(zoneListener, plugin);
        pm.registerEvents(new DefensorBehaviorListener(), plugin);
        pm.registerEvents(new ControladorArrowListener(), plugin);
        pm.registerEvents(new ControladorAvanzadoListener(plugin), plugin);
        pm.registerEvents(new HealerBehaviorListener(plugin, plugin.getChargeBaseManager()), plugin);
        pm.registerEvents(new AtacanteBehaviorListener(plugin), plugin);
        pm.registerEvents(new HibridoBasicoListener(plugin, plugin.getChargeBaseManager()), plugin);
        pm.registerEvents(new HibridoAvanzadoListener(plugin, plugin.getChargeBaseManager()), plugin);

        // Perks
        pm.registerEvents(new PerkListener(), plugin);
        pm.registerEvents(new LastStandListener(), plugin);
        pm.registerEvents(new FumbleListener(), plugin);
        pm.registerEvents(new SharedSpaceListener(), plugin);
        pm.registerEvents(new LifestealListener(), plugin);
        pm.registerEvents(new PiesLigerosListener(), plugin);
        pm.registerEvents(new SanacionCompartidaListener(), plugin);
        pm.registerEvents(new ForjaEficienteListener(), plugin);
        pm.registerEvents(new HambreVorazListener(), plugin);
        pm.registerEvents(new EcoVacioListener(), plugin);
        pm.registerEvents(new VinculoDolorosoListener(), plugin);
        pm.registerEvents(new SaltoDobleListener(), plugin);
        pm.registerEvents(new PosturaFirmeListener(), plugin);
        pm.registerEvents(new ImanGolpesListener(), plugin);
        pm.registerEvents(new FotofobiaListener(), plugin);

        // TeamChest
        pm.registerEvents(new TeamChestListener(), plugin);

        // Bingo
        pm.registerEvents(new BingoInventoryListener(), plugin);
        pm.registerEvents(new BingoCollectListener(), plugin);
        pm.registerEvents(new BingoKillListener(), plugin);
        pm.registerEvents(new BingoMineListener(), plugin);

        // Spawns
        pm.registerEvents(new ZombieSpawner(plugin), plugin);
        pm.registerEvents(new PolarBearListener(plugin), plugin);
        pm.registerEvents(new CustomMobListener(plugin), plugin);
        pm.registerEvents(new MobEquipmentListener(), plugin);
        pm.registerEvents(new CreeperVariantListener(), plugin);
        pm.registerEvents(new UpgradedMobListener(), plugin);
        pm.registerEvents(new EndCreeperListener(plugin), plugin);
        pm.registerEvents(new EndermanHostileListener(plugin), plugin);
        pm.registerEvents(new DragonaVacioListener(plugin), plugin);

        // Boss
        pm.registerEvents(new GuardianForjaListener(plugin), plugin);
        pm.registerEvents(new CustodioVacioListener(plugin), plugin);

        // Items
        pm.registerEvents(new LanzapapasListener(), plugin);
        pm.registerEvents(new PapaExplosivaListener(), plugin);
        pm.registerEvents(new ClawnListener(), plugin);
        pm.registerEvents(new GiveMenuListener(), plugin);
        pm.registerEvents(new LazoListener(), plugin);
        pm.registerEvents(new VaritaCurativaListener(), plugin);
        pm.registerEvents(new VaritaBarreraListener(), plugin);
        pm.registerEvents(new VaritaBarreraMejoradaListener(), plugin);
        pm.registerEvents(new FrenesiListener(), plugin);
        pm.registerEvents(new ZanahoriaRellenableListener(), plugin);
        pm.registerEvents(new ZanahoriaRellenableCraftListener(), plugin);
        pm.registerEvents(new ModifierSmithingListener(plugin), plugin);
        pm.registerEvents(new AnclaVinculoListener(), plugin);
        pm.registerEvents(new FrascoVacioListener(), plugin);

        // World Generation
        WorldGenListener worldGenListener = new WorldGenListener(plugin.getStructurePopulator());
        pm.registerEvents(worldGenListener, plugin);
        pm.registerEvents(plugin.getPendingEntitySpawner(), plugin);
        StructureDevCommand structureDev = new StructureDevCommand(plugin.getStructurePopulator(), plugin.getDataFolder());
        pm.registerEvents(structureDev, plugin);
    }
}
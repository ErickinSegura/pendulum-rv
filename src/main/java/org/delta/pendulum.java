package org.delta;

import org.bukkit.plugin.java.JavaPlugin;
import org.delta.commands.CommandCompletion;
import org.delta.commands.PendulumCommand;
import org.delta.customs.craftings.CustomCraftingRegistry;
import org.delta.database.BingoSyncManager;
import org.delta.database.DatabaseManager;
import org.delta.listeners.bingo.BingoCollectListener;
import org.delta.listeners.bingo.BingoInventoryListener;
import org.delta.listeners.bingo.BingoKillListener;
import org.delta.listeners.bingo.BingoMineListener;
import org.delta.listeners.perks.PerkListener;
import org.delta.listeners.perks.impl.FumbleListener;
import org.delta.listeners.perks.impl.LastStandListener;
import org.delta.listeners.perks.impl.LifestealListener;
import org.delta.listeners.perks.impl.SharedSpaceListener;
import org.delta.listeners.player.*;
import org.delta.listeners.spawns.ZombieSpawner;
import org.delta.listeners.teamChest.TeamChestListener;
import org.delta.listeners.worldgen.PendingEntitySpawner;
import org.delta.managers.ArmorTrimManager;
import org.delta.managers.achievements.AchievementManager;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.chargebase.ChargeBaseManager;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.DeathEvents;
import org.delta.managers.death.LifeManager;
import org.delta.libs.PendulumSettings;
import org.delta.listeners.death.DeathListener;
import org.delta.managers.perks.PerkManager;
import org.delta.managers.teamChest.TeamChestManager;
import org.delta.worldgen.StructurePopulator;

import java.util.Objects;

import static org.delta.libs.MessageUtils.sendConsole;

public final class pendulum extends JavaPlugin {

    public static String prefix = "&d&lPendulum&r";
    private LifeManager lifeManager;
    private AchievementManager achievementManager;
    private DatabaseManager databaseManager;
    private ChargeBaseManager chargeBaseManager;
    private CustomCraftingListener customCraftingListener;
    private StructurePopulator structurePopulator;
    private PendingEntitySpawner pendingEntitySpawner;


    @Override
    public void onEnable() {
        String version = getPluginMeta().getVersion();
        if (!getDataFolder().exists()) getDataFolder().mkdirs();
        reloadConfig();
        PendulumSettings.getInstance().load();

        databaseManager = new DatabaseManager(this);
        try {
            databaseManager.connect();
        } catch (Exception e) {
            getLogger().warning("No se pudo conectar a la base de datos: " + e.getMessage());
            getLogger().warning("El plugin funcionará sin persistencia de datos.");
        }

        // Inicializar managers
        lifeManager = new LifeManager(this);
        achievementManager = new AchievementManager(this);
        DeathEvents deathEvents = new DeathEvents();
        BingoDataManager bingoDataManager = BingoDataManager.getInstance(this);
        BingoProgressManager bingoProgressManager = BingoProgressManager.getInstance();
        BingoSyncManager bingoSyncManager = BingoSyncManager.getInstance(this, databaseManager);
        chargeBaseManager = new ChargeBaseManager(this);
        new ArmorTrimManager(this);
        TeamChestManager.initialize(getDataFolder());
        PerkManager.initialize(getDataFolder());
        PerkManager.getInstance();
        customCraftingListener = new CustomCraftingListener(this);
        CustomCraftingRegistry.register(customCraftingListener);
        pendingEntitySpawner = new PendingEntitySpawner(this);
        structurePopulator = new StructurePopulator(getLogger(), pendingEntitySpawner, this);

        for (org.bukkit.World world : getServer().getWorlds()) {
            if (world.getEnvironment() == org.bukkit.World.Environment.NORMAL) {
                world.getPopulators().add(structurePopulator);
                getLogger().info("[StructurePopulator] Añadido a mundo ya cargado: " + world.getName());
            }
        }

        new EventRegistry(this, lifeManager).registerAll();
        registerCommands();

        sendConsole("&d&m                                          ");
        sendConsole("       &l[" + prefix + "&l]");
        sendConsole("       &l&dPlugin enabled!");
        sendConsole("       &l&dVersion: &r" + version);
        sendConsole("&d&m                                          ");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) databaseManager.disconnect();

        sendConsole("&d&m                                          ");
        sendConsole("       &l[" + prefix + "&l]");
        sendConsole("       &l&dPlugin disabled!");
        sendConsole("&d&m                                          ");
    }

    private void registerCommands() {
        Objects.requireNonNull(getServer().getPluginCommand("pendulum")).setExecutor(new PendulumCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("pendulum")).setTabCompleter(new CommandCompletion(structurePopulator));
    }

    public static pendulum getInstance(){
        return JavaPlugin.getPlugin(pendulum.class);
    }

    public LifeManager getLifeManager() {
        return lifeManager;
    }

    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public ChargeBaseManager getChargeBaseManager() { return chargeBaseManager; }

    public StructurePopulator getStructurePopulator() { return structurePopulator; }

    public PendingEntitySpawner getPendingEntitySpawner() { return pendingEntitySpawner; }
}
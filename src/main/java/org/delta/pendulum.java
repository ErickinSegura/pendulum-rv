package org.delta;

import org.bukkit.plugin.java.JavaPlugin;
import org.delta.commands.CommandCompletion;
import org.delta.commands.PendulumCommand;
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
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.DeathEvents;
import org.delta.managers.death.LifeManager;
import org.delta.libs.PendulumSettings;
import org.delta.listeners.death.DeathListener;
import org.delta.managers.perks.PerkManager;
import org.delta.managers.teamChest.TeamChestManager;

import java.util.Objects;

import static org.delta.libs.MessageUtils.sendConsole;

public final class pendulum extends JavaPlugin {

    public static String prefix = "&d&lPendulum&r";
    private LifeManager lifeManager;
    private DeathEvents deathEvents;
    private BingoDataManager bingoDataManager;
    private BingoProgressManager bingoProgressManager;
    private BingoSyncManager bingoSyncManager;
    private DatabaseManager databaseManager;

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
        deathEvents = new DeathEvents();
        bingoDataManager = BingoDataManager.getInstance(this);
        bingoProgressManager = BingoProgressManager.getInstance();
        bingoSyncManager = BingoSyncManager.getInstance(this, databaseManager);
        TeamChestManager.initialize(getDataFolder());
        PerkManager.initialize(getDataFolder());
        PerkManager.getInstance();

        registerEvents();
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

    public void registerEvents() {
        ClockEvents.setPlugin(this);
        getServer().getPluginManager().registerEvents(new RetoListener(), this);
        getServer().getPluginManager().registerEvents(new LifeListener(lifeManager), this);
        getServer().getPluginManager().registerEvents(new DeathListener(lifeManager), this);
        getServer().getPluginManager().registerEvents(new TotemListener(), this);
        getServer().getPluginManager().registerEvents(new PotionListener(), this);
        getServer().getPluginManager().registerEvents(new BedListener(), this);
        getServer().getPluginManager().registerEvents(new PerkListener(), this);

        // Listeners de perks
        getServer().getPluginManager().registerEvents(new LastStandListener(), this);
        getServer().getPluginManager().registerEvents(new FumbleListener(), this);
        getServer().getPluginManager().registerEvents(new SharedSpaceListener(), this);
        getServer().getPluginManager().registerEvents(new LifestealListener(), this);

        // Listeners de TeamChest
        getServer().getPluginManager().registerEvents(new TeamChestListener(), this);

        // Listeners del bingo
        getServer().getPluginManager().registerEvents(new BingoInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BingoCollectListener(), this);
        getServer().getPluginManager().registerEvents(new BingoKillListener(), this);
        getServer().getPluginManager().registerEvents(new BingoMineListener(), this);
        

        // Spawn Listeners
        getServer().getPluginManager().registerEvents(new ZombieSpawner(this), this);

        // Database Listeners
        getServer().getPluginManager().registerEvents(new JoinLeaveListener(lifeManager), this);
    }

    private void registerCommands() {
        Objects.requireNonNull(getServer().getPluginCommand("pendulum")).setExecutor(new PendulumCommand(this));
        Objects.requireNonNull(getServer().getPluginCommand("pendulum")).setTabCompleter(new CommandCompletion());
    }

    public static pendulum getInstance(){
        return JavaPlugin.getPlugin(pendulum.class);
    }

    public LifeManager getLifeManager() {
        return lifeManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
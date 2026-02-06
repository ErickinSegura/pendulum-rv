package org.delta;

import org.bukkit.plugin.java.JavaPlugin;
import org.delta.commands.CommandCompletion;
import org.delta.commands.PendulumCommand;
import org.delta.listeners.bingo.BingoCollectListener;
import org.delta.listeners.bingo.BingoInventoryListener;
import org.delta.listeners.bingo.BingoKillListener;
import org.delta.listeners.bingo.BingoMineListener;
import org.delta.listeners.player.*;
import org.delta.listeners.teamChest.TeamChestListener;
import org.delta.managers.bingo.BingoDataManager;
import org.delta.managers.bingo.BingoProgressManager;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.DeathEvents;
import org.delta.managers.death.LifeManager;
import org.delta.libs.PendulumSettings;
import org.delta.listeners.death.DeathListener;
import org.delta.managers.teamChest.TeamChestManager;

import java.util.Objects;

import static org.delta.libs.MessageUtils.sendConsole;

public final class pendulum extends JavaPlugin {

    public static String prefix = "&d&lPendulum&r";
    private LifeManager lifeManager;
    private DeathEvents deathEvents;
    private BingoDataManager bingoDataManager;
    private BingoProgressManager bingoProgressManager;

    @Override
    public void onEnable() {
        String version = getPluginMeta().getVersion();
        PendulumSettings.getInstance().load();

        // Inicializar managers
        lifeManager = new LifeManager(this);
        deathEvents = new DeathEvents();
        bingoDataManager = BingoDataManager.getInstance(this);
        bingoProgressManager = BingoProgressManager.getInstance();
        TeamChestManager.initialize(getDataFolder());


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

        // Listeners de TeamChest
        getServer().getPluginManager().registerEvents(new TeamChestListener(), this);

        // Listeners del bingo
        getServer().getPluginManager().registerEvents(new BingoInventoryListener(), this);
        getServer().getPluginManager().registerEvents(new BingoCollectListener(), this);
        getServer().getPluginManager().registerEvents(new BingoKillListener(), this);
        getServer().getPluginManager().registerEvents(new BingoMineListener(), this);
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

    public DeathEvents getDeathEvents() {
        return deathEvents;
    }

    public BingoDataManager getBingoDataManager() {
        return bingoDataManager;
    }

    public BingoProgressManager getBingoProgressManager() {
        return bingoProgressManager;
    }
}
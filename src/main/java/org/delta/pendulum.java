package org.delta;

import org.bukkit.plugin.java.JavaPlugin;
import org.delta.commands.CommandCompletion;
import org.delta.commands.PendulumCommand;
import org.delta.managers.death.ClockEvents;
import org.delta.managers.death.DeathEvents;
import org.delta.managers.death.LifeManager;
import org.delta.libs.PendulumSettings;
import org.delta.listeners.player.DeathListener;
import org.delta.listeners.player.TotemListener;
import org.delta.listeners.player.LifeListener;
import org.delta.listeners.player.RetoListener;

import static org.delta.libs.MessageUtils.sendConsole;

public final class pendulum extends JavaPlugin {

    public static String prefix = "&d&lPendulum&r";
    private LifeManager lifeManager;
    private DeathEvents deathEvents;

    @Override
    public void onEnable() {
        String version = getPluginMeta().getVersion();
        PendulumSettings.getInstance().load();
        lifeManager = new LifeManager(this);
        deathEvents = new DeathEvents();

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
    }

    private void registerCommands() {
        getServer().getPluginCommand("pendulum").setExecutor(new PendulumCommand());
        getServer().getPluginCommand("pendulum").setTabCompleter(new CommandCompletion());
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
}
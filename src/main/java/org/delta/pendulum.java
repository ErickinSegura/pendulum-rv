package org.delta;

import org.bukkit.plugin.java.JavaPlugin;
import org.delta.commands.CommandCompletion;
import org.delta.commands.PendulumCommand;
import org.delta.libs.ClockEvents;
import org.delta.libs.LifeManager;
import org.delta.libs.PendulumSettings;
import org.delta.listeners.DeathListener;
import org.delta.listeners.GenericPlayerListener;
import org.delta.listeners.LifeListener;
import org.delta.listeners.RetoListener;

import static org.delta.libs.MessageUtils.sendConsole;

public final class pendulum extends JavaPlugin {

    public static String prefix = "&d&lPendulum&r";
    private LifeManager lifeManager;

    @Override
    public void onEnable() {
        String version = getPluginMeta().getVersion();
        PendulumSettings.getInstance().load();
        lifeManager = new LifeManager(this);

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
        getServer().getPluginManager().registerEvents(new GenericPlayerListener(), this);
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
}
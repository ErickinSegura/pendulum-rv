package org.delta.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.delta.database.repositories.PlayerRepository;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseManager {

    private final JavaPlugin plugin;
    private HikariDataSource dataSource;

    private PlayerRepository playerRepository;

    public DatabaseManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        File configFile = new File(plugin.getDataFolder(), "settings.yml");
        FileConfiguration fileConfig = YamlConfiguration.loadConfiguration(configFile);

        String host     = fileConfig.getString("database.host");
        String port     = fileConfig.getString("database.port", "5432");
        String dbName   = fileConfig.getString("database.name", "postgres");
        String user     = fileConfig.getString("database.user", "postgres");
        String password = fileConfig.getString("database.password");

        if (host == null || host.isBlank()) {
            plugin.getLogger().warning("'database.host' no configurado — DB deshabilitada.");
            return;
        }

        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.pendulum.libs.postgresql.Driver");
            config.setJdbcUrl("jdbc:postgresql://" + host + ":" + port + "/" + dbName + "?sslmode=require");
            config.setUsername(user);
            config.setPassword(password);
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(10_000);
            config.setIdleTimeout(300_000);
            config.setMaxLifetime(600_000);
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");

            dataSource = new HikariDataSource(config);
            playerRepository = new PlayerRepository(this);
            plugin.getLogger().info("Conexión a la base de datos establecida.");
        } catch (Exception e) {
            plugin.getLogger().warning("Error al conectar con la DB: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        return dataSource != null && !dataSource.isClosed();
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public PlayerRepository players() {
        return playerRepository;
    }
}
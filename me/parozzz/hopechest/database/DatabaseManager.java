/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import me.parozzz.reflex.database.IDatabase;
import me.parozzz.reflex.database.PlayerTable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class DatabaseManager implements IDatabase
{
    private final JavaPlugin plugin;
    
    private final HikariDataSource source;
    private final ChestTable chestTable;
    private final PlayerTable playerTable;
    public DatabaseManager(final JavaPlugin plugin)
    {
        this.plugin = plugin;
        
        /*
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1;");
        config.setPoolName("HopeChestSQlitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        config.setMaximumPoolSize(20);*/
        
        source = new HikariDataSource();
        source.setConnectionTestQuery("SELECT 1;");
        source.setPoolName("HopeChestSQlitePool");
        source.setDriverClassName("org.sqlite.JDBC");
        source.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        source.setMaximumPoolSize(20);
        
        chestTable = new ChestTable(this);
        playerTable = new PlayerTable(this, plugin);
    }
    
    protected JavaPlugin getPlugin()
    {
        return plugin;
    }
    
    @Override
    public synchronized Connection getConnection() throws SQLException
    {
        return source.getConnection();
    }
    
    public PlayerTable getPlayerTable()
    {
        return playerTable;
    }
    
    public ChestTable getChestTable()
    {
        return chestTable;
    }
    
}

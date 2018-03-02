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
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class DatabaseManager 
{
    private final HikariDataSource source;
    private final ChestTable chestTable;
    public DatabaseManager(final JavaPlugin plugin)
    {
        HikariConfig config = new HikariConfig();
        config.setConnectionTestQuery("SELECT 1;");
        config.setPoolName("HopeChestSQlitePool");
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + plugin.getDataFolder().getAbsolutePath() + File.separator + "database.db");
        config.setMaximumPoolSize(20);
        
        source = new HikariDataSource(config);
        
        chestTable = new ChestTable(this);
    }
    
    protected synchronized Connection getConnection() throws SQLException
    {
        return source.getConnection();
    }
    
    public ChestTable getChestTable()
    {
        return chestTable;
    }
    
}

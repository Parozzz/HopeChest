/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import me.parozzz.hopechestv2.chests.ChestHandler;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.chests.crop.CropHandler;
import me.parozzz.hopechestv2.chests.crop.CropManager;
import me.parozzz.hopechestv2.chests.mob.MobHandler;
import me.parozzz.hopechestv2.chests.mob.MobManager;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.reflection.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeChest extends JavaPlugin
{

    @Override
    public void onEnable()
    {
        try
        {
            load(false);
        }
        catch(Exception ex)
        {
            Bukkit.getLogger().log(Level.SEVERE, null, ex);
        }
        
    }
    
    @Override
    public void onDisable()
    {
        ChunkManager.saveData(this);
        unregisterAll();
    }
    
    public void load(final boolean reload) throws FileNotFoundException, UnsupportedEncodingException
    {
        if(reload)
        {
            ChunkManager.saveData(this);
            unregisterAll();
        }
        FileConfiguration c=Utils.fileStartup(this, new File(this.getDataFolder(), "config.yml"));
        initializeStatics(c);
        
        Bukkit.getServer().getPluginManager().registerEvents(new ChestHandler(), this);
        
        if(c.getBoolean("mobChestEnabled"))
        {
            FileConfiguration mob=Utils.fileStartup(this, new File(this.getDataFolder(), "mob.yml"));
            MobManager.init(mob);
            
            Bukkit.getServer().getPluginManager().registerEvents(new MobHandler(), this);
        }
        
        if(c.getBoolean("cropChestEnabled"))
        {
            FileConfiguration crop=Utils.fileStartup(this, new File(this.getDataFolder(), "crop.yml"));
            CropManager.init(crop);
             
            Bukkit.getServer().getPluginManager().registerEvents(new CropHandler(), this);
        }
        
        this.getCommand("chest").setExecutor(new MainCommand());
        
        ChunkManager.loadData(this);
    }
    
    private void initializeStatics(final FileConfiguration c)
    {
        if(Dependency.setupEconomy())
        {
            Bukkit.getLogger().log(Level.INFO, "[HopeChest] Vault enabled!");
        }
        Configs.init(c);
        Utils.init();
        ReflectionUtils.init();
    }
    
    private void clearStatics()
    {
        Configs.clear();
        ChunkManager.clear();
        MobManager.clear();
        CropManager.clear();
    }
    
    private void unregisterAll()
    {
        clearStatics();
        Bukkit.getScheduler().cancelTasks(this);
        HandlerList.unregisterAll(this);
    }
   
}

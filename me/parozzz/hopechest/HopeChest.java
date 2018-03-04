/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopechest.api.HopeChestAPI;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestListener;
import me.parozzz.hopechest.chest.SubTypeTokenItem;
import me.parozzz.hopechest.world.ChestRegistry;
import me.parozzz.hopechest.chest.crop.CropListener;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.chest.gui.ChestGui;
import me.parozzz.hopechest.chest.gui.ChestGuiListener;
import me.parozzz.hopechest.chest.mob.MobListener;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.utilities.PlayerUtil;
import me.parozzz.hopechest.world.ChestFactory;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.CommandMap;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class HopeChest extends JavaPlugin
{
    private final static Logger logger = Logger.getLogger(HopeChest.class.getSimpleName());
    
    private HopeChestConfiguration configuration;
    
    private ChestRegistry chestRegistry;
    private ChestFactory chestFactory;
    private WorldRegistry worldRegistry;
    
    @Override
    public void onEnable()  
    {
        configuration = new HopeChestConfiguration(this);
        ChestGui.setConfiguration(configuration);
        PlayerUtil.setConfig(configuration);
        
        DatabaseManager databaseManager = new DatabaseManager(this);
        worldRegistry = new WorldRegistry(this, databaseManager);
        
        chestRegistry = new ChestRegistry(databaseManager);
        chestFactory = new ChestFactory(worldRegistry, configuration);
        
        Bukkit.getPluginManager().registerEvents(chestRegistry, this);
        Bukkit.getPluginManager().registerEvents(new ChestListener(chestFactory, chestRegistry, worldRegistry, configuration), this);
        Bukkit.getPluginManager().registerEvents(new MobListener(worldRegistry, configuration), this);
        Bukkit.getPluginManager().registerEvents(new CropListener(worldRegistry), this);
        Bukkit.getPluginManager().registerEvents(new ChestGuiListener(chestFactory, configuration), this);
        
        CommandMap commandMap;
        try {
            Field commandMapField = Bukkit.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (CommandMap)commandMapField.get(Bukkit.getServer());
            commandMapField.setAccessible(false);
        } catch(final IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
            return;
        }
        
        commandMap.register("hopechest", new HChestCommand(configuration, chestFactory, databaseManager));
    }
    
    public HopeChestConfiguration getConfiguration()
    {
        return configuration;
    }
    
    public WorldRegistry getWorldRegistry()
    {
        return worldRegistry;
    }
    
    public ChestFactory getChestFactory()
    {
        return chestFactory;
    }
    
    public ChestRegistry getChestRegistry()
    {
        return chestRegistry;
    }
    
    @Override
    public void onDisable()
    {
        HandlerList.unregisterAll(this);
    }
    
    
    private static HopeChestAPI api;
    public static HopeChestAPI getAPI()
    {
        HopeChest hopeChest;
        if(!Bukkit.getPluginManager().isPluginEnabled("HopeChest") || (hopeChest = JavaPlugin.getPlugin(HopeChest.class)).getWorldRegistry() == null)
        {
            logger.warning("Another plugin is trying to access before loading.");
            logger.warning("Have you added it to the Depend/SoftDepend list in the plugin.yml?");
            return null;
        }
        
        return api != null ? api : (api = new HopeChestAPI()
        {
            @Override
            public boolean hasChestAt(final Location loc) 
            {
                return hopeChest.getWorldRegistry().getWorldManager(loc.getWorld()).hasChestAt(loc);
            }

            @Override
            public AbstractChest getChestAt(final Location loc) 
            {
                return hopeChest.getWorldRegistry().getWorldManager(loc.getWorld()).getChestAt(loc);
            }

            @Override
            public SubTypeTokenItem getToken(CreatureType ct)
            {
                return new SubTypeTokenItem(ct, hopeChest.getConfiguration());
            }

            @Override
            public SubTypeTokenItem getToken(CropType ct) 
            {
                return new SubTypeTokenItem(ct, hopeChest.getConfiguration());
            }
        });        
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.autosell;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;
import me.parozzz.reflex.task.SplittedRunnable;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class AutoSellRunnable extends SplittedRunnable<IAutoSeller>
{
    private static final Logger logger = Logger.getLogger(AutoSellRunnable.class.getName());
    
    private static AutoSellRunnable instance;
    public static AutoSellRunnable getInstance()
    {
        if(instance == null)
        {
            logger.warning("Trying to access the instance before the plugin HopeChest has been enabled.");
            logger.warning("Have you added it to the plugin.yml?");
            throw new RuntimeException();
        }
        return instance;
    }
    
    public static void setInstance(final JavaPlugin plugin)
    {
        instance = new AutoSellRunnable(plugin);
    }
    
    private final List<IAutoSeller> autoSellChests;
    private AutoSellRunnable(final JavaPlugin plugin)
    {
        super(20);
        autoSellChests = new LinkedList<>();
        runTaskTimer(plugin, 1L, 1L);
    }
    
    protected void addAutoSeller(final IAutoSeller autoSeller)
    {
        autoSellChests.add(autoSeller);
    }
    
    protected void removeAutoSeller(final IAutoSeller autoSeller)
    {
        autoSellChests.remove(autoSeller);
    }
    
    public boolean contains(final IAutoSeller autoSeller)
    {
        return autoSellChests.contains(autoSeller);
    }
    
    @Override
    public Collection<IAutoSeller> getCollection() 
    {
        return autoSellChests;
    }

    private final Consumer<IAutoSeller> consumer = IAutoSeller::tickAutoSell;
    @Override
    public Consumer<IAutoSeller> getConsumer() 
    {
        return consumer;
    }
    
}

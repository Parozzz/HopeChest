/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.dependency;

import com.earth2me.essentials.Essentials;
import java.math.BigDecimal;
import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class DependencyManager 
{
    private static final Logger logger = Logger.getLogger(DependencyManager.class.getName());
    
    private static Economy economy;
    private static IWorth worth;
    public static void initialize(final JavaPlugin plugin)
    {
        if(Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
            if (economyProvider != null) {
                economy = economyProvider.getProvider();
                logger.info("Hooked into Vault");
            }
            
            if(Bukkit.getPluginManager().isPluginEnabled("Essentials"))
            {
                logger.info("Using Essentials Worth file");
                
                Essentials essentials = JavaPlugin.getPlugin(Essentials.class);
                worth = new IWorth()
                {
                    @Override
                    public double getCost(ItemStack item) 
                    {
                        BigDecimal bigDecimal = essentials.getWorth().getPrice(item);
                        if(bigDecimal == null)
                        {
                            return 0D;
                        }
                        
                        return bigDecimal.doubleValue();
                    }
                };
                
            }
        }
    }
    
    public static boolean isVaultHooked()
    {
        return economy != null;
    }
    
    public static Economy getEconomy()
    {
        return economy;
    }
}

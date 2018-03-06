/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.Dependency;

import java.util.logging.Logger;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

/**
 *
 * @author Paros
 */
public class DependencyManager 
{
    private static final Logger logger = Logger.getLogger(DependencyManager.class.getName());
    
    private static Economy economy;
    public static void initialize()
    {
        RegisteredServiceProvider<Economy> economyProvider = Bukkit.getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
        if (economyProvider != null) {
            economy = economyProvider.getProvider();
            logger.info("[HopeChest] Hooked into Vault");
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.autosell;

import me.parozzz.hopechest.dependency.DependencyManager;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.configuration.AutoSellConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author Paros
 */
public class AutoSellGui
{
    private static AutoSellConfig config;
    public static void setConfiguration(final HopeChestConfiguration configuration)
    {
        config = configuration.getAutoSellConfig();
    }
    
    
    
    private final IAutoSeller autoSeller;
    private final Inventory inventory;
    public AutoSellGui(final IAutoSeller autoSeller)
    {
        this.autoSeller = autoSeller;
        
        inventory = Bukkit.createInventory(new AutoSellHolder(), 9, config.getGuiConfig().getTitle());
    }
    
    public IAutoSeller getAutoSeller()
    {
        return autoSeller;
    }
    
    public void open(final HumanEntity he)
    {
        he.openInventory(inventory);
    }
    
    public class AutoSellHolder implements InventoryHolder
    {
        public AutoSellGui getGui()
        {
            return AutoSellGui.this;
        }
        
        @Override
        public Inventory getInventory() 
        {
            return inventory;
        }
    }
}

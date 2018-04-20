/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.autosell;

import me.parozzz.hopechest.chest.autosell.AutoSellGui.AutoSellHolder;
import me.parozzz.hopechest.configuration.AutoSellConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 *
 * @author Paros
 */
public class AutoSellGuiListener implements Listener
{
    private final AutoSellConfig config;
    public AutoSellGuiListener(final HopeChestConfiguration configuration)
    {
        this.config = configuration.getAutoSellConfig();
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getHolder() instanceof AutoSellHolder)
        {
            AutoSellGui gui = ((AutoSellHolder)e.getInventory().getHolder()).getGui();
        }
    }
}

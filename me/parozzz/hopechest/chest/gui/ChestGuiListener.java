/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.gui;

import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.SubTypeTokenItem;
import me.parozzz.hopechest.chest.gui.ChestGui.ChestGuiHolder;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.utilities.PlayerUtil;
import me.parozzz.hopechest.world.ChestFactory;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ChestGuiListener implements Listener
{
    private final ChestFactory chestFactory;
    private final HopeChestConfiguration config;
    public ChestGuiListener(final ChestFactory chestFactory, final HopeChestConfiguration config)
    {
        this.chestFactory = chestFactory;
        this.config = config;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onInventoryClick(final InventoryClickEvent e)
    {
        if(e.getInventory().getHolder() instanceof ChestGuiHolder)
        {
            e.setCancelled(true);
            if(e.getInventory().equals(e.getClickedInventory()))
            {
                AbstractChest chest = ((ChestGuiHolder)e.getInventory().getHolder()).getGui().getChest();
                
                ChestGuiItem guiItem = ChestGui.getChestGuiItem(new NMSStackCompound(e.getCurrentItem()));
                if(guiItem == null || guiItem.getChestType() != chest.getType())
                {
                    return;
                }
                
                ChestType type = guiItem.getChestType();
                Object subType = guiItem.getSubType();
                
                chest.removeSpecificType(subType);
                SubTypeTokenItem tokenItem = chestFactory.getToken(type, subType);
                PlayerUtil.sendItemStack(tokenItem.getItemStack(), e.getWhoClicked());
                
                e.setCurrentItem(new ItemStack(Material.AIR));
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onGuiClose(final InventoryCloseEvent e)
    {
        if(e.getInventory().getHolder() instanceof ChestGuiHolder && e.getInventory().getViewers().size() == 1)
        {
            ((ChestGuiHolder)e.getInventory().getHolder()).getGui().getChest().resetChestGuiInstance();
        }
    }
}

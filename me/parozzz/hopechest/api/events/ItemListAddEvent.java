/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.api.events;

import java.util.List;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Called when a list of items is being handled for being added to custom chests in the chunk.
 * @author Paros
 */
public class ItemListAddEvent extends Event implements Cancellable
{
    private final ChestType chestType;
    private final Object subType;
    private final List<ItemStack> items;
    public ItemListAddEvent(final ChestType chestType, final Object subType, final List<ItemStack> items)
    {
        this.chestType = chestType;
        this.subType = subType;
        this.items = items;
    }
    
    public ChestType getChestType()
    {
        return chestType;
    }
    
    /**
     * Get the subType of the chest. Return null if the ChestType is not CROP.
     * @return The CropType or null if ChestType != CROP.
     */
    public CropType getCropType()
    {
        return subType instanceof CropType ? (CropType)subType : null;
    }
    
    /**
     * Get the subType of the chest. Return null if the ChestType is not MOB.
     * @return The CreatureType or null if ChestType != MOB.
     */
    public CreatureType getCreatureType()
    {
        return subType instanceof CreatureType ? (CreatureType)subType : null;
    }
    
    /**
     * Get the list of items to be handled. This is not a copy, so changed here will be applied.
     * @return 
     */
    public List<ItemStack> getAddedItems()
    {
        return items;
    }
    
    private static final HandlerList handler=new HandlerList();
    @Override
    public HandlerList getHandlers() 
    {
        return handler;
    }
    
    public static HandlerList getHandlerList()
    {
        return handler;
    }
    
    private boolean cancelled=false;
    @Override
    public boolean isCancelled() 
    {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean bln) 
    {
        cancelled=bln;
    }
}

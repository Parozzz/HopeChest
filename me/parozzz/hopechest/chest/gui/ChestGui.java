/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.gui;

import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author Paros
 */
public class ChestGui 
{
    private static final String TAG = "HopeChest.ChestGui";
    public static void setStackData(final NMSStackCompound stack, final ChestType chestType, final String subType)
    {
        NBTCompound compound = new NBTCompound();
        compound.setString("Type", chestType.name());
        compound.setString("SubType", subType);
        stack.setTag(TAG, compound);
    }
    
    private static HopeChestConfiguration config;
    public static void setConfiguration(final HopeChestConfiguration localConfig)
    {
        config = localConfig;
    }
    
    private final AbstractChest chest;
    private final Inventory inventory;
    public ChestGui(final AbstractChest chest)
    {
        this.chest = chest;
        
        int size = ((chest.getSpecificTypesSize() / 9) + 1) *9;
        size = size == 54 ? size : size + 9;
        inventory = Bukkit.createInventory(new ChestGuiHolder(), size, config.getGuiConfig().getTitle());
        ((AbstractChest<Object>)chest).getGuiItems(config.getGuiConfig()).forEach(inventory::addItem);
    }
    
    public AbstractChest getChest()
    {
        return chest;
    }
    
    public void open(final HumanEntity he)
    {
        he.openInventory(inventory);
    }
    
    protected class ChestGuiHolder implements InventoryHolder
    {
        private ChestGuiHolder() { }
        
        public ChestGui getGui()
        {
            return ChestGui.this;
        }
        
        @Override
        public Inventory getInventory() 
        {
            return inventory;
        }
    }
    
        
    public static @Nullable ChestGuiItem getChestGuiItem(final NMSStackCompound stack)
    {
        if(!stack.hasKey(TAG))
        {
            return null;
        }
        
        NBTCompound compound = stack.getCompound(TAG);
        
        ChestType chestType = ChestType.valueOf(compound.getString("Type"));
        return new ChestGuiItem(chestType, chestType.convertString(compound.getString("SubType")));
    }
}

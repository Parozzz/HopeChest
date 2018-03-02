/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.gui;

import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.configuration.GuiConfig;
import me.parozzz.hopechest.world.ChestFactory;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ChestGuiItem 
{
    private final ChestType chestType;
    private final Object subType;
    protected ChestGuiItem(final ChestType chestType, final Object subType)
    {
        this.chestType = chestType;
        
        if(!chestType.getSubTypeClass().isInstance(subType))
        {
            throw new IllegalArgumentException("The subType is not of a valid instance.");
        }
        this.subType = subType;
    }
    
    public ChestGuiItem(final CreatureType type)
    {
        this(ChestType.MOB, type);
    }
    
    public ChestGuiItem(final CropType type)
    {
        this(ChestType.CROP, type);
    }
    
    public ChestType getChestType()
    {
        return chestType;
    }

    public Object getSubType()
    {
        return subType;
    }
    
    protected @Nullable ItemStack getItemStack(final GuiConfig config)
    {
        switch(chestType)
        {
            case MOB:
                return config.getMobHead((CreatureType)subType);
            case CROP:
                return config.getCropItem((CropType)subType);
            default:
                return null;
        }
    }
}

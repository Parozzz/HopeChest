/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.crop;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.autosell.AutoSellGui;
import me.parozzz.hopechest.chest.autosell.IAutoSeller;
import me.parozzz.hopechest.configuration.GuiConfig;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class CropChest extends AbstractChest<CropType> implements IAutoSeller
{
    private final Set<CropType> types;
    public CropChest(final UUID owner, final WorldManager worldManager, final Location loc, final DatabaseManager databaseManager)
    {
        super(owner, worldManager, loc, databaseManager);
        types = EnumSet.noneOf(CropType.class);
    }
    
    @Override
    public ChestType getType() 
    {
        return ChestType.CROP;
    }

    @Override
    public boolean canStoreItems(final CropType t) 
    {
        return t!= null && (types.isEmpty() || types.contains(t));
    }

    @Override
    public Set<CropType> getSpecificTypes() 
    {
        return Collections.unmodifiableSet(types);
    }

    @Override
    public boolean addSpecificType(final CropType t) 
    {
        boolean added = t == null ? false : types.add(t);
        if(added)
        {
            super.getDatabaseManager().getChestTable().updateSubTypes(this);
        }
        return added;
    }

    @Override
    public void addRawSpecificType(CropType t) 
    {
        types.add(t);
    }
    
    @Override
    public boolean removeSpecificType(CropType t) 
    {
        boolean removed = t == null ? false : types.remove(t);
        if(removed)
        {
            super.getDatabaseManager().getChestTable().updateSubTypes(this);
        }
        return removed;
    }
    
    @Override
    public int getSpecificTypesSize() 
    {
        return types.size();
    }
    
    @Override
    public Stream<ItemStack> getGuiItems(GuiConfig guiConfig) 
    {
        return types.stream().map(guiConfig::getCropItem);
    }

    @Override
    public void doAutoSell() 
    {
        
    }
    
    @Override
    public void setAutoSell(final boolean active)
    {
        IAutoSeller.super.setRawAutoSell(active);
        
        super.getDatabaseManager().getChestTable().updateAutoSell(this);
    }

    private AutoSellGui autoSellGui;
    @Override
    public AutoSellGui getAutoSellGui() 
    {
        if(autoSellGui == null)
        {
            autoSellGui = new AutoSellGui(this);
        }
        return autoSellGui;
    }

    @Override
    public void resetAutoSellGuiInstance() 
    {
        autoSellGui = null;
    }
}

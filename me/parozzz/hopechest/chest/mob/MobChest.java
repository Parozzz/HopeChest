/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.configuration.GuiConfig;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class MobChest extends AbstractChest<CreatureType>
{
    private final Set<CreatureType> types;
    
    public MobChest(final WorldManager worldManager, final Location loc, final DatabaseManager databaseManager)
    {
        super(worldManager, loc, databaseManager);
        types = EnumSet.noneOf(CreatureType.class);
    }
    
    @Override
    public ChestType getType() 
    {
        return ChestType.MOB;
    }
    
    @Override
    public boolean canStoreItems(CreatureType t) 
    {
        return t!= null && (types.isEmpty() || types.contains(t));
    }

    @Override
    public Set<CreatureType> getSpecificTypes() 
    {
        return Collections.unmodifiableSet(types);
    }

    @Override
    public boolean addSpecificType(final CreatureType t) 
    {
        boolean added = t == null ? false : types.add(t);
        if(added)
        {
            super.getDatabaseManager().getChestTable().updateSubTypes(this);
        }
        return added;
    }

    @Override
    public void addRawSpecificType(CreatureType t) 
    {
        types.add(t);
    }

    @Override
    public boolean removeSpecificType(CreatureType t) 
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
    public Stream<ItemStack> getGuiItems(final GuiConfig guiConfig) 
    {
        return types.stream().map(guiConfig::getMobHead);
    }
}

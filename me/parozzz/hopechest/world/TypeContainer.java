/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Chunk;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class TypeContainer 
{
    private final Chunk chunk;
    private final Map<ChestType, List<AbstractChest>> map;
    public TypeContainer(final Chunk chunk)
    {
        this.chunk = chunk;
        map = new EnumMap(ChestType.class);
    }
    
    protected boolean addChest(final AbstractChest chest)
    {
        return chest == null 
                ? false 
                : map.computeIfAbsent(chest.getType(), temp -> new LinkedList<>()).add(chest);
    }
    
    protected boolean removeChest(final AbstractChest chest)
    {
        if(chest == null)
        {
            return false;
        }
        
        List<AbstractChest> chestList = map.get(chest.getType());
        if(chestList == null)
        {
            return false;
        }
        return chestList.remove(chest);
    }
    
    /**
     * Pass a list of items to be added to HopeChests in this chunk
     * @param itemList The list of ItemStack. All the remaining items will be stored in this same list.
     * @param type The CreatureType paramenter the chest needs to have.
     */
    public final void addMobItemStacks(final List<ItemStack> itemList, final CreatureType type)
    {
        addItemStacks(ChestType.MOB, itemList, type, true);
    }
    
    public final void addCropItemStacks(final List<ItemStack> itemList, final CropType type)
    {
        addItemStacks(ChestType.CROP, itemList, type, false);
    }
    
    public final void addCropItemStacks(final ItemStack itemStack, final CropType type)
    {
        addItemStacks(ChestType.CROP, Arrays.asList(itemStack), type, false);
    }
    
    private @Nonnull void addItemStacks(final ChestType chestType, final List<ItemStack> itemList, final Object type, final boolean returnExtra)
    {
        if(itemList == null || type == null || chestType == null)
        {
            throw new IllegalArgumentException();
        }
        
        List<AbstractChest> chestList = map.get(chestType);
        if(chestList == null)
        {
            return;
        }
        else if(chestList.isEmpty())
        {
            map.remove(chestType, chestList);
            return;
        }
        
        for(int x = 0; x < chestList.size(); x++)
        {
            AbstractChest chest = chestList.get(x);
            if(!chest.canStoreItems(type))
            {
                continue;
            }
            
            Collection<ItemStack> localRemainsColl = chest.getInventory().addItem(itemList.stream().toArray(ItemStack[]::new)).values();
            itemList.clear();
            itemList.addAll(localRemainsColl);
            if(itemList.isEmpty())
            {
                return;
            }
        }
        
        if(!returnExtra)
        {
            itemList.clear();
        }
    }
    
    public void forEach(final Consumer<AbstractChest> consumer)
    {
        map.values().stream().flatMap(List::stream).forEach(consumer);
    }
    
    public Chunk getChunk()
    {
        return chunk;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.database.DatabaseManager;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author Paros
 */
public class WorldManager
{
    private final static Logger logger = Logger.getLogger(WorldManager.class.getName());
    
    private final DatabaseManager databaseManager;
    private final ChestRegistry chestRegistry;
    private final ChestFactory chestFactory;
    private final World world;
    private final Map<Chunk, TypeContainer> chunkContainers;
    public WorldManager(final ChestRegistry chestRegistry, final ChestFactory chestFactory, final DatabaseManager databaseManager, final World w)
    {
        this.databaseManager = databaseManager;
        this.chestRegistry = chestRegistry;
        this.chestFactory = chestFactory;
        this.world = w;
        
        chunkContainers = new HashMap<>();
    }
    
    public final World getWorld()
    {
        return world;
    }
    
    protected final @Nullable AbstractChest addChest(final ChestType chestType, final BlockState blockState, final Object... initialSubTypes)
    {
        if(chestType == null || chestType.getChestClass() == null || !InventoryHolder.class.isInstance(blockState))
        {
            return null;
        }
        
        Location loc = blockState.getLocation();
        if(chestRegistry.hasPlacedChestAt(loc))
        {
            logger.log(Level.SEVERE, "Something wrong when adding chests. Duplicated Location?");
            return null;
        }
        
        AbstractChest chest = this.getChest(chestType, loc);
        Stream.of(initialSubTypes).forEach(chest::addRawSpecificType);
        
        chestRegistry.addPlacedChest(chest);
        chunkContainers.computeIfAbsent(loc.getChunk(), TypeContainer::new).addChest(chest);
        databaseManager.getChestTable().addChest(chest);
        return chest;
    }
    
    private final Map<ChestType, Constructor<? extends AbstractChest>> cachedConstructor = new EnumMap(ChestType.class);
    private @Nullable AbstractChest getChest(final ChestType chestType, final Location loc)
    {
        try {
            Constructor<? extends AbstractChest> cons = cachedConstructor.get(chestType);
            if(cons == null)
            {
                cons = chestType.getChestClass().getConstructor(WorldManager.class, Location.class, DatabaseManager.class);
                cachedConstructor.put(chestType, cons);
            }
            return cons.newInstance(this, loc, databaseManager);
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }
    
    public final ChestType getChestTypeAt(final Location loc)
    {
        AbstractChest chest = chestRegistry.getPlacedChest(loc);
        return chest == null ? null : chest.getType();
    }
    
    public final boolean hasChestAt(final Location loc)
    {
        return chestRegistry.hasPlacedChestAt(loc);
    }
    
    public final @Nullable AbstractChest getChestAt(final Location loc)
    {
        return chestRegistry.getPlacedChest(loc);
    }
    
    public final @Nullable ChestType removeChest(final Location loc)
    {
        return removeChest(loc, true);
    }
            
    public final @Nullable ChestType removeChest(final Location loc, final boolean dropItem)
    {
        if(loc == null)
        {
            return null;
        }
        
        AbstractChest placedChest = chestRegistry.removePlacedChestAt(loc);
        if(placedChest == null)
        {
            return null;
        }
        
        TypeContainer typeContainer = chunkContainers.get(loc.getChunk());
        if(typeContainer == null || !typeContainer.removeChest(placedChest))
        {
            return null;
        }
        
        databaseManager.getChestTable().removeChest(placedChest);
        if(dropItem)
        {
            loc.getWorld().dropItem(loc, chestFactory.getChestItemStack(placedChest));
        }
        return placedChest.getType();
    }
    
    public final @Nullable TypeContainer getByChunk(final Chunk c)
    {
        return c == null 
                ? null 
                : chunkContainers.get(c);
    }
    
    public final void unloadChunk(final Chunk c)
    {
        TypeContainer container = chunkContainers.remove(c);
        if(container != null)
        {
            container.forEach(chestRegistry::removePlacedChest);
        }
    }

    public final void loadChunk(final Chunk c)
    {
        databaseManager.getChestTable().queryChunk(c, query -> 
        {
            if(!query.isEmpty())
            {
                TypeContainer typeContainer = chunkContainers.computeIfAbsent(c, TypeContainer::new);
                query.forEach(queryItem -> 
                {
                    ChestType type = queryItem.getType();

                    AbstractChest chest = this.getChest(type, queryItem.getLocation());
                    queryItem.subTypeStream().map(type::convertString).forEach(chest::addRawSpecificType);

                    chestRegistry.addPlacedChest(chest);
                    typeContainer.addChest(chest);
                });
            }
        });
    }
    
}

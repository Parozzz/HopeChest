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
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.database.DatabaseManager;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
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
    private final HopeChestConfiguration config;
    private final World world;
    private final Map<Chunk, TypeContainer> chunkContainers;
    public WorldManager(final ChestRegistry chestRegistry, final ChestFactory chestFactory, final DatabaseManager databaseManager, final HopeChestConfiguration config, final World w)
    {
        this.databaseManager = databaseManager;
        this.chestRegistry = chestRegistry;
        this.chestFactory = chestFactory;
        this.config = config;
        this.world = w;
        
        chunkContainers = new HashMap<>();
    }
    
    public final World getWorld()
    {
        return world;
    }
    
    protected final AddChestResult addChest(final UUID owner, final ChestType chestType, final BlockState blockState, final boolean bypassLimit, final Object... initialSubTypes)
    {
        if(chestType == null || chestType.getChestClass() == null || !InventoryHolder.class.isInstance(blockState))
        {
            return new AddChestResult(Result.GENERIC_ERROR);
        }
        
        Location loc = blockState.getLocation();
        if(chestRegistry.hasPlacedChestAt(loc))
        {
            logger.log(Level.SEVERE, "Something wrong when adding chests. Duplicated Location?");
            return new AddChestResult(Result.DUPLICATED_LOCATION);
        }
        
        if(!bypassLimit && chestRegistry.getPlayerChestAmount(owner) >= config.getMaxPlayerChests())
        {
            Bukkit.getLogger().info("AMOUNT: " + chestRegistry.getPlayerChestAmount(owner));
            return new AddChestResult(Result.MAX_REACHED);
        }
        
        AbstractChest chest = this.getChest(owner, chestType, loc);
        if(chest == null)
        {
            return new AddChestResult(Result.GENERIC_ERROR);
        }
        
        Stream.of(initialSubTypes).forEach(chest::addRawSpecificType);
        
        chestRegistry.addPlacedChest(chest, false);
        chunkContainers.computeIfAbsent(loc.getChunk(), TypeContainer::new).addChest(chest);
        databaseManager.getChestTable().addChest(chest);
        return new AddChestResult(Result.SUCCESS, chest);
    }
    
    public enum Result
    {
        SUCCESS, DUPLICATED_LOCATION, MAX_REACHED, GENERIC_ERROR;
    }
    
    public class AddChestResult
    {
        private final Result resultEnum;
        private AddChestResult(final Result resultEnum)
        {
            this.resultEnum = resultEnum;
        }
        
        private AbstractChest chest;
        private AddChestResult(final Result resultEnum, final AbstractChest chest)
        {
            this(resultEnum);
            this.chest = chest;
        }
        
        public Result getResult()
        {
            return resultEnum;
        }
        
        public @Nullable AbstractChest getChest()
        {
            return chest;
        }
    }
    
    private final Map<ChestType, Constructor<? extends AbstractChest>> cachedConstructor = new EnumMap(ChestType.class);
    private @Nullable AbstractChest getChest(final UUID owner, final ChestType chestType, final Location loc)
    {
        try {
            Constructor<? extends AbstractChest> cons = cachedConstructor.get(chestType);
            if(cons == null)
            {
                cons = chestType.getChestClass().getConstructor(UUID.class, WorldManager.class, Location.class, DatabaseManager.class);
                cachedConstructor.put(chestType, cons);
            }
            return cons.newInstance(owner, this, loc, databaseManager);
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
        
        AbstractChest placedChest = chestRegistry.removePlacedChestAt(loc, false);
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
            container.forEach(chest -> chestRegistry.removePlacedChest(chest, true));
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

                    AbstractChest chest = this.getChest(queryItem.getOwner(), type, queryItem.getLocation());
                    queryItem.subTypeStream().filter(StringUtils::isNotBlank).map(type::convertString).forEach(chest::addRawSpecificType);
                    
                    chestRegistry.addPlacedChest(chest, true);
                    typeContainer.addChest(chest);
                });
            }
        });
    }
    
}

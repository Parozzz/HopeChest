/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest;

import java.util.Collection;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.gui.ChestGui;
import me.parozzz.hopechest.configuration.GuiConfig;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.world.WorldManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 * @param <T>
 */
public abstract class AbstractChest<T> 
{
    private final static Logger logger = Logger.getLogger(AbstractChest.class.getName());
    
    private final WorldManager worldManager;
    private final DatabaseManager databaseManager;
    private final UUID owner;
    private final Location location;
    public <H extends BlockState & InventoryHolder> AbstractChest(final UUID owner, final WorldManager worldManager, final Location loc, final DatabaseManager databaseManager)
    {
        this.worldManager = worldManager;
        this.databaseManager = databaseManager;
        this.owner = owner;
        this.location = loc;
    }
    
    public WorldManager getWorldManager()
    {
        return worldManager;
    }
    
    protected DatabaseManager getDatabaseManager()
    {
        return databaseManager;
    }
    
    public final Location getLocation()
    {
        return location;
    }
    
    public final UUID getOwner()
    {
        return owner;
    }
    
    public final boolean isOwner(final Player player)
    {
        return player == null ? false : owner.equals(player.getUniqueId());
    }
    
    public final Inventory getInventory()
    {
        InventoryHolder holder = this.getInventoryHolder();
        return holder == null ? null : holder.getInventory();
    }
    
    public final @Nullable InventoryHolder getInventoryHolder()
    {
        BlockState blockState = this.getLocation().getBlock().getState();
        if(!InventoryHolder.class.isInstance(blockState))
        {
            logger.log(Level.SEVERE, "The saved block at x:{0} y:{1} z:{2} in world:{4} is not an InventoryHolder anymore. Another plugin modified it?", new Object[] { 
                blockState.getX() , blockState.getY(), blockState.getZ(), blockState.getWorld().getName()
            });
            return null;
        }
        return (InventoryHolder)getLocation().getBlock().getState();
    }

    public abstract ChestType getType();
    public abstract Collection<T> getSpecificTypes();
    public abstract boolean addSpecificType(final T t);
    public abstract void addRawSpecificType(final T t);
    public abstract boolean removeSpecificType(final T t);
    public abstract int getSpecificTypesSize();
    public abstract boolean canStoreItems(final T t);
    public abstract Stream<ItemStack> getGuiItems(final GuiConfig guiConfig);
    
    private ChestGui cachedGui;
    public final ChestGui getChestGui()
    {
        if(cachedGui == null)
        {
            cachedGui = new ChestGui(this);
        }
        return cachedGui;
    }
    
    public final void resetChestGuiInstance()
    {
        cachedGui = null;
    }
}

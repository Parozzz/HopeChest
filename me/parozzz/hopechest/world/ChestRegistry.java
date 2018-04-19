/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.database.DatabaseManager;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author Paros
 */
public class ChestRegistry implements Listener
{
    private final Map<Location, AbstractChest> placedChests;
    private final Map<UUID, Integer> playerCounter;
    private final DatabaseManager database;
    public ChestRegistry(final DatabaseManager database)
    {
        placedChests = new HashMap<>();
        playerCounter = new ConcurrentHashMap<>();
        
        this.database = database;
    }
     
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerAsyncJoin(final AsyncPlayerPreLoginEvent e)
    {
        int amount = database.getChestTable().getPlayerChestNumber(e.getUniqueId());
        playerCounter.put(e.getUniqueId(), amount);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerQuit(final PlayerQuitEvent e)
    {
        playerCounter.remove(e.getPlayer().getUniqueId());
    }
    
    public boolean hasPlacedChestAt(final Location loc)
    {
        return placedChests.containsKey(loc);
    }
    
    public int getPlayerChestAmount(final UUID owner)
    {
        return playerCounter.getOrDefault(owner, 0);
    }
    
    protected @Nullable AbstractChest removePlacedChest(final AbstractChest chest, final boolean chunkUnload)
    {
        return removePlacedChestAt(chest.getLocation(), chunkUnload);
    }
    
    protected @Nullable AbstractChest removePlacedChestAt(final Location loc, final boolean chunkUnload)
    {
        AbstractChest chest = placedChests.remove(loc);
        
        if(!chunkUnload)
        {
            Optional.ofNullable(chest).map(AbstractChest::getOwner).ifPresent(owner -> playerCounter.compute(owner, (uuid, amount) -> amount - 1));
        }
        return chest;
    }
    
    protected void addPlacedChest(final AbstractChest chest, final boolean chunkLoad)
    {
        placedChests.put(chest.getLocation(), chest);
        
        if(!chunkLoad)
        {
            playerCounter.compute(chest.getOwner(), (uuid, amount) -> amount == null ? 1 : amount + 1);
        }
    }
    
    public @Nullable AbstractChest getPlacedChest(final Location loc)
    {
        return placedChests.get(loc);
    }
}

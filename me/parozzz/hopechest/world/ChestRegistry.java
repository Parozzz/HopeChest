/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.AbstractChest;
import org.bukkit.Location;

/**
 *
 * @author Paros
 */
public class ChestRegistry
{
    private final Map<Location, AbstractChest> placedChests;
    public ChestRegistry()
    {
        placedChests = new HashMap<>();
    }
    
    public boolean hasPlacedChestAt(final Location loc)
    {
        return placedChests.containsKey(loc);
    }
    
    protected AbstractChest removePlacedChest(final AbstractChest chest)
    {
        return placedChests.remove(chest.getLocation());
    }
    
    protected AbstractChest removePlacedChestAt(final Location loc)
    {
        return placedChests.remove(loc);
    }
    
    protected void addPlacedChest(final AbstractChest chest)
    {
        placedChests.put(chest.getLocation(), chest);
    }
    
    public @Nullable AbstractChest getPlacedChest(final Location loc)
    {
        return placedChests.get(loc);
    }
}

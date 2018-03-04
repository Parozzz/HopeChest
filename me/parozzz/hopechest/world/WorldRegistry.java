/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.util.HashMap;
import java.util.Map;
import me.parozzz.hopechest.HopeChest;
import me.parozzz.hopechest.database.DatabaseManager;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class WorldRegistry
{
    private final DatabaseManager databaseManager;
    private final HopeChest hopeChest;
    private final Map<World, WorldManager> worldManagers;
    public WorldRegistry(final HopeChest hopeChest, final DatabaseManager databaseManager)
    {
        this.hopeChest = hopeChest;
        this.databaseManager = databaseManager;
        
        worldManagers = new HashMap<>();
    }
    
    public WorldManager getWorldManager(final World world)
    {
        return worldManagers.computeIfAbsent(world, w -> new WorldManager(hopeChest.getChestRegistry(), hopeChest.getChestFactory(), databaseManager,  hopeChest.getConfiguration(), w));
    }
}

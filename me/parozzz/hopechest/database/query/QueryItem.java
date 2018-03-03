/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database.query;

import java.util.UUID;
import java.util.stream.Stream;
import me.parozzz.hopechest.chest.ChestType;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class QueryItem 
{
    private final int x;
    private final int y;
    private final int z;
    private final ChestType chestType;
    private final String subTypes;
    private final UUID owner;
    public QueryItem(final int x, final int y, final int z, final ChestType chestType, final String subTypes, final UUID owner)
    {
        this.x = x;
        this.y = y;
        this.z = z; 

        this.chestType = chestType;
        this.subTypes = subTypes;
        this.owner = owner;
    }

    private World world;
    protected void setWorld(final World world)
    {
        this.world = world;
    }

    public Location getLocation()
    {
        return new Location(world, x, y, z);
    }

    public ChestType getType()
    {
        return chestType;
    }
    
    public UUID getOwner()
    {
        return owner;
    }

    public Stream<String> subTypeStream()
    {
        return Stream.of(subTypes.split(","));
    }
}

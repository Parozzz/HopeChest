/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.utilities;

import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class ChunkUtil 
{
    public static String toString(final Chunk c)
    {
        return c.getX() + ":" + c.getZ();
    }
    
    public static String getStringChunkCoord(final Location loc)
    {
        return (loc.getBlockX() >> 4) + ":" + (loc.getBlockZ() >> 4);
    }
    
    public static @Nullable Chunk getChunk(final World world, final String stringChunk)
    {
        if(world == null || stringChunk == null || !stringChunk.contains(":"))
        {
            return null;
        }
        
        int splitterIndex = stringChunk.indexOf(":");
        
        int x = Integer.valueOf(stringChunk.substring(0, splitterIndex));
        int z = Integer.valueOf(stringChunk.substring(splitterIndex + 1));
        
        return world.getChunkAt(x, z);
    }
}

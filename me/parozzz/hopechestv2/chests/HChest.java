/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests;

import java.util.UUID;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import org.bukkit.block.Block;

/**
 *
 * @author Paros
 */
public abstract class HChest 
{
    private final Block b;
    private final ChestType ct;
    private final UUID owner;
    private final Object type;
    public HChest(final ChestType ct, final Object type, final UUID owner, final Block b)
    {
        this.ct=ct;
        this.type=type;
        this.owner=owner;
        this.b=b;
    }
    
    public Block getBlock()
    {
        return b;
    }
    
    public Object getType()
    {
        return type;
    }
    
    public ChestType getChestType()
    {
        return ct;
    }
    
    public UUID getOwner()
    {
        return owner;
    }
}

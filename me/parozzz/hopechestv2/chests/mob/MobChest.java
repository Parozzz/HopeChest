/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.mob;

import java.util.UUID;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import me.parozzz.hopechestv2.chests.HChest;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import org.bukkit.block.Block;

/**
 *
 * @author Paros
 */
public class MobChest extends HChest
{
    
    public MobChest(final UUID owner, final CreatureType type, final Block b) 
    {
        super(ChestType.MOB, type, owner, b);
    }
    
    @Override
    public CreatureType getType()
    {
        return (CreatureType)super.getType();
    }
    
}

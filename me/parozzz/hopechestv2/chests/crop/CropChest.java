/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.crop;

import java.util.UUID;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import me.parozzz.hopechestv2.chests.HChest;
import me.parozzz.hopechestv2.chests.crop.CropManager.CropType;
import org.bukkit.block.Block;

/**
 *
 * @author Paros
 */
public class CropChest extends HChest
{

    public CropChest(final UUID owner, final CropType type, final Block b) 
    {
        super(ChestType.CROP, type, owner, b);
    }
    
    @Override
    public CropType getType()
    {
        return (CropType)super.getType();
    }

    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;

/**
 *
 * @author Paros
 */
public class HeadHunting 
{
    
    public static void setStackCompoundData(final NMSStackCompound stack, final CreatureType ct)
    {
        NBTCompound compound = stack.getCompound("HopeChest");
        compound.setString("Type", ct.name());
        stack.setTag("HopeChest", compound);
    }
}

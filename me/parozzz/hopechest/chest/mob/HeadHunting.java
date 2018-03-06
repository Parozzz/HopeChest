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
    private final static String TAG = "HopeChest.HeadHunting";
    public static void setStackCompoundData(final NMSStackCompound stack, final CreatureType ct, final double value)
    {
        NBTCompound compound = stack.getCompound(TAG);
        compound.setString("Type", ct.name());
        compound.setDouble("Value", value);
        stack.setTag(TAG, compound);
    }
    
    public static boolean isValidHead(final NMSStackCompound stack)
    {
        return stack != null && stack.hasKey(TAG);
    }
    
    public static HeadInfo getHeadInfo(final NMSStackCompound stack)
    {
        if(!isValidHead(stack))
        {
            return null;
        }
        
        NBTCompound compound = stack.getCompound(TAG);
        return new HeadInfo(CreatureType.valueOf(compound.getString("Type")), compound.getDouble("Value"));
    }
    
    public static boolean sameType(final CreatureType ct, final NMSStackCompound stack)
    {
        if(!isValidHead(stack))
        {
            return false;
        }
        
        return ct.name().equals(stack.getCompound(TAG).getString("Type"));
    }
    
    public static class HeadInfo
    {
        private final CreatureType ct;
        private final double value;
        private HeadInfo(final CreatureType ct, final double value)
        {
            this.ct = ct;
            this.value = value;
        }
        
        public CreatureType getCreatureType()
        {
            return ct;
        }
        
        public double getValue()
        {
            return value;
        }
    }
}

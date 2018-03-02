/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest;

import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.configuration.chest.CropConfig;
import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.NMS.nbt.NBTType;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Paros
 */
public class SubTypeTokenItem 
{
    public static boolean isValid(final NMSStackCompound stack)
    {
        return stack.hasKeyOfType("HopeChest.SubTypeItem", NBTType.COMPOUND);
    }
    
    public static @Nullable SubTypeTokenItem getTokenItem(final ItemStack itemStack)
    {
        NMSStackCompound stack = new NMSStackCompound(itemStack);
        return isValid(stack) ? new SubTypeTokenItem(stack) : null;
    }
    
    private static final String TAG = "HopeChest.SubTypeItem";
    
    private final ItemStack itemStack;
    private final NBTCompound compound;
    private SubTypeTokenItem(final ChestType type, final String subType, final String translatedType, final HopeChestConfiguration config)
    {
        this.chestType = type;
        NMSStackCompound stack = config.getSubTypeToken();
        
        this.compound = new NBTCompound();
        this.setType(compound, type, subType);
        stack.setTag(TAG, compound);
        
        itemStack = stack.getItemStack();
        ItemMeta meta = itemStack.getItemMeta();
        ItemUtil.parseMetaVariable(meta, "{chest}", config.getLanguage().getMessage(type.getLanguageKey()));
        ItemUtil.parseMetaVariable(meta, "{type}", translatedType);
        itemStack.setItemMeta(meta);
    }
    
    private SubTypeTokenItem(final NMSStackCompound stack)
    {
        this.itemStack = stack.getItemStack();
        this.compound = stack.getCompound(TAG);
    }
    
    public SubTypeTokenItem(final CreatureType ct, final HopeChestConfiguration config)
    {
        this(ChestType.MOB, ct.name(), ((MobConfig)config.getConfig(ChestType.MOB)).getMobName(ct), config);
    }
    
    public SubTypeTokenItem(final CropType ct, final HopeChestConfiguration config)
    {
        this(ChestType.CROP, ct.name(), ((CropConfig)config.getConfig(ChestType.CROP)).getCropName(ct), config);
    }
    
    private ChestType chestType;
    public ChestType getChestType()
    {
        return chestType != null 
                ? chestType 
                : (chestType = ChestType.valueOf(compound.getString("Type")));
    }
    
    public @Nullable Object getSubType()
    {
        switch(getChestType())
        {
            case MOB:
                return getCreatureType();
            case CROP:
                return getCropType();
            default:
                return null;
        }
    }
    
    public @Nullable CreatureType getCreatureType()
    {
        try {
            return CreatureType.valueOf(compound.getString("SubType"));
        } catch(final IllegalArgumentException ex) {
            return null;
        }
    }
    
    public @Nullable CropType getCropType()
    {
        try {
            return CropType.valueOf(compound.getString("SubType"));
        } catch(final IllegalArgumentException ex) {
            return null;
        }
    }
    
    public ItemStack getItemStack()
    {
        return itemStack;
    }
    
    private void setType(final NBTCompound compound, final ChestType type, final String subType)
    {
        compound.setString("Type", type.name());
        compound.setString("SubType", subType);
    }
    
}

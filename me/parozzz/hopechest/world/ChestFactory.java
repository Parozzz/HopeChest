/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.world;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.SubTypeTokenItem;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.configuration.chest.ChestConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class ChestFactory 
{
    private final WorldRegistry worldRegistry;
    private final HopeChestConfiguration config;
    public ChestFactory(final WorldRegistry worldRegistry, final HopeChestConfiguration config)
    {
        this.worldRegistry = worldRegistry;
        this.config = config;
    }
    
    /**
     * Create a new crop chest with the passed blockState, which needs to implement inventory holder either
     * @param itemStack The ItemStack placed, this method already checks if the ItemStack NBT is valid.
     * @param blockState The inventoryHolder to create the new chest with.
     * @return The new chest if all parameters are valid, null if the chest already exists.
     */
    public @Nullable AbstractChest createNewChest(final ItemStack itemStack, final BlockState blockState)
    {
        if(itemStack == null || blockState == null)
        {
            return null;
        }
        
        NMSStackCompound stack = new NMSStackCompound(itemStack);
        if(!isChestItem(stack))
        {
           return null; 
        }
        
        NBTCompound mainCompound = stack.getCompound(CHEST_NBT);
        
        ChestType chestType = ChestType.valueOf(mainCompound.getString("Type"));
        if(chestType.getChestClass() == null)
        {
            return null;
        }
        
        String subTypeString = mainCompound.getString("SubTypes");
        Object[] subTypes = !subTypeString.isEmpty() 
                ? Stream.of(subTypeString.split(",")).map(chestType::convertString).toArray(Object[]::new)
                : new Object[0];

        return worldRegistry.getWorldManager(blockState.getWorld()).addChest(chestType, blockState, subTypes);
    }
    
    private static final String CHEST_NBT = "HopeChestNBT";
    
    public ItemStack getChestItemStack(final AbstractChest chest)
    {
        return getItemStack(chest.getType(), ((Stream<String>)chest.getSpecificTypes().stream().map(Objects::toString)).collect(Collectors.joining(",")));
    }
    
    public ItemStack getEmptyItemStack(final ChestType chestType)
    {
        return getItemStack(chestType, "");
    }
    
    public @Nullable SubTypeTokenItem getToken(final ChestType chestType, final Object subType)
    {
        if(!chestType.getSubTypeClass().isInstance(subType))
        {
            return null;
        }
        
        switch(chestType)
        {
            case MOB:
                return new SubTypeTokenItem((CreatureType)subType, this.config);
            case CROP:
                return new SubTypeTokenItem((CropType)subType, this.config);
            default:
                return null;
        }
    }
    
    /**
     * 
     * @param types The subTypes to be added to the ItemStack
     * @return The resulted ItemStack
     * @throws IllegalArgumentException If any of the paramenters is null
     * @throws IllegalArgumentException If the types array is not of the same Class as the ChestType requirement
     */
    public ItemStack getCropItemStack(final CropType... types)
    {
        if(types == null)
        {
            throw new IllegalArgumentException();
        }
        
        ChestType chestType = ChestType.CROP;
        if(types.length == 0)
        {
            return this.getEmptyItemStack(chestType);
        }

        return getItemStack(chestType, Stream.of(types).map(Enum::name).collect(Collectors.joining(",")));
    }
    
    public ItemStack getMobItemStack(final CreatureType... types)
    {
        if(types == null)
        {
            throw new IllegalArgumentException();
        }
        
        ChestType chestType = ChestType.MOB;
        if(types.length == 0)
        {
            return this.getEmptyItemStack(chestType);
        }

        return getItemStack(chestType, Stream.of(types).map(Enum::name).collect(Collectors.joining(",")));
    }
    
    private ItemStack getItemStack(final ChestType type, final String subType)
    {
        ChestConfig chestConfig = config.getConfig(type);
        
        NMSStackCompound stack = chestConfig.getStack();
        NBTCompound mainCompound = stack.getCompound(CHEST_NBT);
        mainCompound.setString("Type", type.name());
        mainCompound.setString("SubTypes", subType);
        stack.setTag(CHEST_NBT, mainCompound);
        return stack.getItemStack(); 
    }

    public boolean isChestItem(final NMSStackCompound stack)
    {
        return stack.hasKey(CHEST_NBT);
    }
}

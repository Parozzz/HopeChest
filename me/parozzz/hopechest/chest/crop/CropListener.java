/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.crop;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;
import me.parozzz.hopechest.world.TypeContainer;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.CropState;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.BlockState;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.Dye;
import org.bukkit.material.NetherWarts;

/**
 *
 * @author Paros
 */
public class CropListener implements Listener
{
    private final static Logger logger = Logger.getLogger(CropListener.class.getSimpleName());
    
    private final WorldRegistry worldRegistry;
    public CropListener(final WorldRegistry worldRegistry)
    {
        this.worldRegistry = worldRegistry;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockGrow(final BlockGrowEvent e)
    {
        BlockState newState = e.getNewState();
        if(!this.isRipe(newState))
        {
            return;
        }
        
        TypeContainer typeContainer = worldRegistry.getWorldManager(newState.getWorld()).getByChunk(newState.getChunk());
        if(typeContainer != null)
        {
            typeContainer.addCropItemStacks(this.getDrops(newState, 0), CropType.getByMaterial(newState.getType()));
            if(setSeeded(newState))
            {
                newState.update();
            }
            e.setCancelled(true);
        }
        
    }
    
    private List<ItemStack> getDrops(final BlockState blockState, final int enchantmentLooting)
    {
        List<ItemStack> dropList = new ArrayList<>();
        Material type = blockState.getType();
        switch(type)
        {
            case MELON_BLOCK:
                dropList.add(new ItemStack(Material.MELON, ThreadLocalRandom.current().nextInt(3, 7)));
                break;
            case CROPS:
                dropList.add(new ItemStack(Material.WHEAT));
                dropList.add(new ItemStack(Material.SEEDS,ThreadLocalRandom.current().nextInt(0, 4 + enchantmentLooting))); 
                break;
            case COCOA: 
                dropList.add(new Dye(DyeColor.BROWN).toItemStack(ThreadLocalRandom.current().nextInt(2,4))); 
                break;
            case POTATO:
                if(ThreadLocalRandom.current().nextInt(0, 100) < 2) 
                {
                    dropList.add(new ItemStack(Material.POISONOUS_POTATO)); 
                }
                dropList.add(new ItemStack(Material.POTATO_ITEM,ThreadLocalRandom.current().nextInt(1, 5 + enchantmentLooting)));
                break;
            case CARROT: 
                dropList.add(new ItemStack(Material.CARROT_ITEM,ThreadLocalRandom.current().nextInt(1, 5 + enchantmentLooting))); 
                break;
            case NETHER_WARTS: 
                dropList.add(new ItemStack(Material.NETHER_STALK,ThreadLocalRandom.current().nextInt(2 ,5 + enchantmentLooting))); 
                break;
            case SUGAR_CANE_BLOCK:
                dropList.add(new ItemStack(Material.SUGAR_CANE));
                break;
            case CACTUS:
            case PUMPKIN:
                dropList.add(new ItemStack(type));
                break;
            default:
                if(MCVersion.V1_9.isHigher() && type == Material.BEETROOT_BLOCK)
                {
                    dropList.add(new ItemStack(Material.BEETROOT));
                    dropList.add(new ItemStack(Material.BEETROOT_SEEDS,ThreadLocalRandom.current().nextInt(4 + enchantmentLooting))); 
                }
                break;
        }
        return dropList;
    }
    
    private boolean setSeeded(final BlockState blockState)
    {
        Material type = blockState.getType();
        switch(type)
        {
            case NETHER_WARTS:
                ((NetherWarts)blockState.getData()).setState(NetherWartsState.SEEDED);
                return true;
            case COCOA:
                ((CocoaPlant)blockState.getData()).setSize(CocoaPlant.CocoaPlantSize.SMALL);
                return true;
            case POTATO:
            case CARROT:
                Util.ifCheck(MCVersion.V1_8.isEqual(), 
                        () -> blockState.setRawData((byte)0), 
                        () -> ((Crops)blockState.getData()).setState(CropState.SEEDED));
                return true;
            case CROPS:
                ((Crops)blockState.getData()).setState(CropState.SEEDED); 
                return true;
            case CACTUS:
            case PUMPKIN:
            case SUGAR_CANE_BLOCK:
                blockState.setType(Material.AIR);
                return true;
            default:
                if(MCVersion.V1_9.isHigher() && type == Material.BEETROOT_BLOCK)
                {
                    ((Crops)blockState.getData()).setState(CropState.SEEDED);
                    return true;
                }
                return false;
        }
    }
    
    private boolean isRipe(final BlockState blockState)
    {
        Material type = blockState.getType();
        switch(type)
        {
            case NETHER_WARTS:
                return ((NetherWarts)blockState.getData()).getState() == NetherWartsState.RIPE; 
            case COCOA:
                return ((CocoaPlant)blockState.getData()).getSize() == CocoaPlant.CocoaPlantSize.LARGE; 
            case POTATO:
            case CARROT:
                return MCVersion.V1_8.isEqual() 
                        ? blockState.getRawData() == 7
                        : ((Crops)blockState.getData()).getState() == CropState.RIPE;
            case CROPS:
                return ((Crops)blockState.getData()).getState() == CropState.RIPE; 
            case CACTUS:
            case PUMPKIN:
            case SUGAR_CANE_BLOCK:
                return true;
            default:
                return MCVersion.V1_9.isHigher() && type == Material.BEETROOT_BLOCK && ((Crops)blockState.getData()).getState() == CropState.RIPE;
        }
    }
}

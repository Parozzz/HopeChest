/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import java.util.Map;
import java.util.Objects;
import me.parozzz.hopechest.dependency.DependencyManager;
import me.parozzz.hopechest.configuration.HeadHuntingConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.MCVersion;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.NMS.nbt.NBTCompound;
import me.parozzz.reflex.utilities.EntityUtil;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author Paros
 */
public class HeadHunting implements Listener
{
    private final HopeChestConfiguration config;
    public HeadHunting(final HopeChestConfiguration config)
    {
        this.config = config;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerHeadInteract(final PlayerInteractEvent e)
    {
        if(e.getItem() != null && e.getItem().getType() == Material.SKULL_ITEM && e.getItem().getDurability() == 3)
        {
            NMSStackCompound stack = new NMSStackCompound(e.getItem());
            
            HeadHunting.HeadInfo headInfo = HeadHunting.getHeadInfo(stack);
            if(headInfo == null)
            {
                return;
            }
            
            EntityUtil.CreatureType type = headInfo.getCreatureType();
            
            PlayerInventory inv = e.getPlayer().getInventory();
            Map<Integer, ? extends ItemStack> map = inv.all(Material.SKULL_ITEM);
            if(MCVersion.V1_8.isEqual() || e.getHand() == EquipmentSlot.HAND) //If the version is 1.8 it won't check for e.getHand method (1.9+).
            {
                map.remove(inv.getHeldItemSlot());
            }
            else
            {
                map.remove(40);
            }
            
            int totalHead = map.values().stream().filter(itemStack -> itemStack.getDurability() == 3).filter(itemStack -> 
            {
                NMSStackCompound localStack = new NMSStackCompound(itemStack);
                return HeadHunting.sameType(type, new NMSStackCompound(itemStack));
            }).mapToInt(ItemStack::getAmount).sum() + e.getItem().getAmount();
            
            double totalValue = headInfo.getValue() * totalHead;
            //I do expect to have the economy enabled for this listener to be handled. (Check in the main class)
            DependencyManager.getEconomy().depositPlayer(e.getPlayer(), totalValue); 
            
            config.getLanguage().getPlaceholder("heads_sold")
                    .parsePlaceholder("{amount}", ""+totalHead)
                    .parsePlaceholder("{money}", ""+totalValue)
                    .sendMessage(e.getPlayer());
        }
    }
    
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

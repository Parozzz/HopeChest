/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.crop;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopechestv2.Dependency;
import me.parozzz.hopechestv2.Configs;
import me.parozzz.hopechestv2.Configs.MessageEnum;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.chests.crop.CropManager.CropType;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.reflection.ItemNBT;
import org.bukkit.CropState;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NetherWartsState;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.CocoaPlant;
import org.bukkit.material.Crops;
import org.bukkit.material.Dye;
import org.bukkit.material.NetherWarts;

/**
 *
 * @author Paros
 */
public class CropHandler implements Listener
{
    private final Predicate<Block> isARipeCrop;
    private final BiFunction<Block, Integer, Set<ItemStack>> getCropDrops;
    private final Consumer<BlockBreakEvent> removeDrop;
    public CropHandler()
    {
        getCropDrops = (b, looting) -> 
        {
            Set<ItemStack> dropSet=new HashSet<>();
            switch(b.getType())
            {
                case CROPS: 
                    dropSet.add(new ItemStack(Material.WHEAT));
                    dropSet.add(new ItemStack(Material.SEEDS,ThreadLocalRandom.current().nextInt(0, 4+looting))); 
                    break;
                case COCOA: 
                    dropSet.add(new Dye(DyeColor.BROWN).toItemStack(ThreadLocalRandom.current().nextInt(2,4))); 
                    break;
                case POTATO:
                    if(ThreadLocalRandom.current().nextInt(0, 100)<2) 
                    {
                        dropSet.add(new ItemStack(Material.POISONOUS_POTATO)); 
                    }
                    dropSet.add(new ItemStack(Material.POTATO_ITEM,ThreadLocalRandom.current().nextInt(1, 5+looting)));
                    break;
                case CARROT: 
                    dropSet.add(new ItemStack(Material.CARROT_ITEM,ThreadLocalRandom.current().nextInt(1, 5+looting))); 
                    break;
                case NETHER_WARTS: 
                    dropSet.add(new ItemStack(Material.NETHER_STALK,ThreadLocalRandom.current().nextInt(2 ,5+looting))); 
                    break;
                case BEETROOT_BLOCK:
                    dropSet.add(new ItemStack(Material.BEETROOT));
                    dropSet.add(new ItemStack(Material.BEETROOT_SEEDS,ThreadLocalRandom.current().nextInt(4+looting))); 
                    break;
            }
            return dropSet;
        };
        
        removeDrop = Utils.bukkitVersion("1.12") ? e -> e.setDropItems(false) :  e ->
        {
            e.getBlock().setType(Material.AIR);
            e.setCancelled(true);
        };
        
        if(Utils.bukkitVersion("1.8"))
        {
            isARipeCrop = b -> 
            {
                switch(b.getType())
                {
                    case NETHER_WARTS:
                        return ((NetherWarts)b.getState().getData()).getState().equals(NetherWartsState.RIPE); 
                    case COCOA:
                        return ((CocoaPlant)b.getState().getData()).getSize().equals(CocoaPlant.CocoaPlantSize.LARGE); 
                    case POTATO:
                    case CARROT:
                        return b.getData()==7;
                    case CROPS:
                        return ((Crops)b.getState().getData()).getState().equals(CropState.RIPE); 
                    default:
                        return false;
                }
            };
        }
        else
        {
            isARipeCrop = b -> 
            {
                switch(b.getType())
                {
                    case NETHER_WARTS:
                        return ((NetherWarts)b.getState().getData()).getState().equals(NetherWartsState.RIPE); 
                    case COCOA:
                        return ((CocoaPlant)b.getState().getData()).getSize().equals(CocoaPlant.CocoaPlantSize.LARGE); 
                    case POTATO:
                    case CARROT:
                    case CROPS:
                        return ((Crops)b.getState().getData()).getState().equals(CropState.RIPE); 
                    default:
                        return false;
                }
            };
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent e)
    {
        if(e.getInventory().equals(CropManager.gui))
        {
            e.setCancelled(true);
            if(e.getInventory().equals(e.getClickedInventory()) && e.getCurrentItem().getType()!=Material.AIR)
            {
                ItemStack hand=Utils.getMainHand(e.getWhoClicked().getEquipment());
                
                if(hand==null || hand.getType()!=CropManager.chestType)
                {
                    MessageEnum.CONTAINERWRONG.send(e.getWhoClicked());
                }
                else if(!Configs.convertRenamedEnable && (hand.getItemMeta().hasDisplayName() || hand.getItemMeta().hasLore()))
                {
                    MessageEnum.NAMEDALREADY.send(e.getWhoClicked());
                }
                else if(Optional.ofNullable(ItemNBT.getKey(e.getCurrentItem(), CropManager.NBT_CONVERTCOST, double.class))
                            .map(cost -> Dependency.eco.withdrawPlayer((Player)e.getWhoClicked(), cost).transactionSuccess())
                            .orElseGet(() -> true))
                {
                    CropType ct=CropType.valueOf(ItemNBT.getKey(e.getCurrentItem(), CropManager.NBT_CROPTYPE, String.class));
                    
                    if(hand.getAmount()<=Configs.maxConvert)
                    {
                        Utils.setPlayerMainHand(e.getWhoClicked().getInventory(), CropManager.getOptions(ct).getChest());
                    }
                    else
                    {
                        e.getWhoClicked().getInventory().addItem(CropManager.getOptions(ct).getChest()).values()
                                .forEach(item -> e.getWhoClicked().getWorld().dropItem(e.getWhoClicked().getLocation(), item));
                        hand.setAmount(hand.getAmount() - Configs.maxConvert);
                    }
                    MessageEnum.CHESTCONVERTION.send(e.getWhoClicked());
                }
                else
                {
                    MessageEnum.MONEYLOW.send(e.getWhoClicked());
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onBlockBreak(final BlockBreakEvent e)
    {
        if(isARipeCrop.test(e.getBlock()))
        {
            int looting=Optional.ofNullable(Utils.getMainHand(e.getPlayer().getEquipment())).map(item -> item.getEnchantmentLevel(Enchantment.LOOT_BONUS_BLOCKS)).orElseGet(() -> 0);
            addDrops(e.getBlock().getLocation(), CropType.CROP, getCropDrops.apply(e.getBlock(), looting));
            removeDrop.accept(e);
        }
    }
    
    @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
    private void onGrow(final BlockGrowEvent e)
    {
        switch(e.getNewState().getType())
        {
            case CACTUS:
                e.setCancelled(!CropManager.cactusGrowAnyway);
                if(CropManager.cactusFarm && Utils.cardinals.stream().map(e.getNewState().getBlock()::getRelative).allMatch(Block::isEmpty))
                {
                    return;
                }
                e.setCancelled(addDrop(e.getNewState().getLocation(), CropType.CACTUS, new ItemStack(Material.CACTUS)));
                break;
            case MELON_BLOCK:
                e.setCancelled(addDrop(e.getNewState().getLocation(), CropType.MELON, new ItemStack(Material.MELON, ThreadLocalRandom.current().nextInt(3, 7))) 
                        || !CropManager.melonGrowAnyway);
                break;
            case PUMPKIN:
                e.setCancelled(addDrop(e.getNewState().getLocation(), CropType.PUMPKIN, new ItemStack(Material.PUMPKIN)) || !CropManager.pumpkinGrowAnyway);
                break;
            case SUGAR_CANE_BLOCK:
                e.setCancelled(addDrop(e.getNewState().getLocation(), CropType.SUGARCANE, new ItemStack(Material.SUGAR_CANE)) || !CropManager.sugarCaneGrowAnyway);
                break;
        }
    }
    
    private boolean addDrop(final Location l, final CropType ct, final ItemStack drop)
    {
        return addDrops(l, ct, Stream.of(drop).collect(Collectors.toSet()));
    }
    
    private boolean addDrops(final Location l, final CropType ct, final Set<ItemStack> drops)
    {
        return Optional.ofNullable(ChunkManager.getChestManager(l.getChunk())).map(manager ->
        {
            for(Inventory inv:Stream.concat(manager.getChestsByType(ct).stream(), manager.getChestsByType(CropType.ALL).stream())
                        .map(b -> (InventoryHolder)b.getState())
                        .map(InventoryHolder::getInventory).toArray(Inventory[]::new))
            {
                Collection<ItemStack> notFit=inv.addItem(drops.stream().toArray(ItemStack[]::new)).values();
                drops.clear();
                if(notFit.isEmpty())
                {
                    break;
                }
                drops.addAll(notFit);
            }
            
            return drops.isEmpty();
        }).orElseGet(() -> false);
    }
}

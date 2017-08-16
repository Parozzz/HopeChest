/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests;

import java.util.Optional;
import me.parozzz.hopechestv2.Configs;
import me.parozzz.hopechestv2.HopeChest;
import me.parozzz.hopechestv2.Configs.MessageEnum;
import me.parozzz.hopechestv2.Permissions;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import me.parozzz.hopechestv2.chests.crop.CropManager.CropType;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import me.parozzz.hopechestv2.utilities.reflection.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ChestHandler implements Listener
{
    public final static String NBT_CHESTTYPE="HopeChest.ChestType";
    public final static String NBT_TYPE="HopeChest.Type";
    public ChestHandler()
    {
        
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onPlayerLogout(final PlayerQuitEvent e)
    {
        ChunkManager.purgeData(e.getPlayer().getUniqueId(), JavaPlugin.getPlugin(HopeChest.class));
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onChestPlace(final BlockPlaceEvent e)
    {
        Optional.ofNullable(ItemNBT.getKey(e.getItemInHand(), NBT_CHESTTYPE, String.class)).filter(str -> !str.isEmpty()).map(ChestType::valueOf).ifPresent(ct -> 
        {
            if(!Configs.doubleChestEnabled && Utils.cardinals.stream().map(bf -> e.getBlockPlaced().getRelative(bf)).anyMatch(ChunkManager::isCustomChest))
            {
                MessageEnum.CHESTDOUBLED.send(e.getPlayer());
                e.setCancelled(true);
            }
            else
            {
                String type=ItemNBT.getKey(e.getItemInHand(), NBT_TYPE, String.class);
                switch(ct)
                {
                    case MOB:
                        if(Optional.ofNullable(ChunkManager.getChestManager(e.getBlockPlaced().getChunk()))
                                .map(manager -> manager.getChestsByType(CreatureType.valueOf(type)).size()+1)
                                .orElseGet(() -> 1)>Configs.chunkLimit)
                        {
                            MessageEnum.CHUNKLIMIT.send(e.getPlayer());
                            e.setCancelled(true);
                            return;
                        }
                        ChunkManager.addChest(e.getBlockPlaced().getChunk(), e.getBlockPlaced(), CreatureType.valueOf(type), e.getPlayer().getUniqueId());
                        break;
                    case CROP:
                        if(Optional.ofNullable(ChunkManager.getChestManager(e.getBlockPlaced().getChunk()))
                                .map(manager -> manager.getChestsByType(CropType.valueOf(type)).size()+1)
                                .orElseGet(() -> 1)>Configs.chunkLimit)
                        {
                            MessageEnum.CHUNKLIMIT.send(e.getPlayer());
                            e.setCancelled(true);
                            return;
                        }
                        ChunkManager.addChest(e.getBlockPlaced().getChunk(), e.getBlockPlaced(), CropType.valueOf(type), e.getPlayer().getUniqueId());
                        break;
                }
            }
        });   
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onChestBreak(final BlockBreakEvent e)
    {
        if(ChunkManager.isCustomChest(e.getBlock()))
        {
            HChest chest=ChunkManager.getCustomChest(e.getBlock());
            e.setCancelled(true);
            if(Configs.antigriefEnabled && !e.getPlayer().hasPermission(Permissions.ADMIN_PROTECTION) && !chest.getOwner().equals(e.getPlayer().getUniqueId()))
            {
                MessageEnum.CHESTPROTECTED.send(e.getPlayer());
            }
            else
            {
                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), ChunkManager.getChestItem(chest.getType()));
                
                ChunkManager.removeChest(e.getBlock());
                e.getBlock().setType(Material.AIR); 
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    private void onChestExplode(final EntityExplodeEvent e)
    {
        e.blockList().stream().filter(ChunkManager::isCustomChest).forEach(b -> ChunkManager.removeChest(b));
    }
}

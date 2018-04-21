/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest;

import me.parozzz.hopechest.PluginPermission;
import me.parozzz.hopechest.chest.autosell.IAutoSeller;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.world.ChestFactory;
import me.parozzz.hopechest.world.ChestRegistry;
import me.parozzz.hopechest.world.WorldManager;
import me.parozzz.hopechest.world.WorldManager.AddChestResult;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

/**
 *
 * @author Paros
 */
public class ChestListener implements Listener
{
    private final ChestFactory chestFactory;
    private final ChestRegistry chestRegistry;
    private final WorldRegistry worldRegistry;
    private final HopeChestConfiguration config;
    public ChestListener(final ChestFactory chestFactory, final ChestRegistry chestRegistry, final WorldRegistry worldRegistry, final HopeChestConfiguration config)
    {
        this.chestFactory = chestFactory;
        this.chestRegistry = chestRegistry;
        this.worldRegistry = worldRegistry;
        this.config = config;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockPlace(final BlockPlaceEvent e)
    {
        AddChestResult result = chestFactory.createNewChest(e.getItemInHand(), e.getBlockPlaced().getState(), e.getPlayer());
        if(result != null)
        {
            LanguageManager language = config.getLanguage();
            switch(result.getResult())
            {
                case MAX_REACHED:
                    e.setCancelled(true);
                    language.sendMessage(e.getPlayer(), "max_chest_exceeded");
                    break;
                case SUCCESS:
                    language.getPlaceholder("chest_placed")
                            .parsePlaceholder("{chest}", language.getMessage(result.getChest().getType().getLanguageKey()))
                            .sendMessage(e.getPlayer());
                    break;
                default:
                    e.setCancelled(true);
                    language.sendMessage(e.getPlayer(), "generic_error");
                    break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockDestroy(final BlockBreakEvent e)
    {
        ChestType removedType = worldRegistry.getWorldManager(e.getBlock().getWorld())
                .removeChest(e.getBlock().getLocation(), e.getPlayer().getGameMode() != GameMode.CREATIVE);
        if(removedType != null)
        {
            LanguageManager language = config.getLanguage();
            language.getPlaceholder("chest_destroyed").parsePlaceholder("{chest}", language.getMessage(removedType.getLanguageKey())).sendMessage(e.getPlayer());
            
            e.setCancelled(true);
            e.getBlock().setType(Material.AIR);
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChestInteract(final PlayerInteractEvent e)
    {
        if(e.getClickedBlock() == null || e.getAction() == Action.PHYSICAL)
        {
            return;
        }
        
        AbstractChest chest = chestRegistry.getPlacedChest(e.getClickedBlock().getLocation());
        if(chest != null)
        {
            //Owner protection. If the player is not the same (Without the bypass permission) won't be able to do anything, even open the chest.
            if(config.hasOwnerProtection() && !PluginPermission.INTERACT_BYPASSOWNER.hasPermission(e.getPlayer()) && !chest.isOwner(e.getPlayer()))
            {
                e.setCancelled(true);
                
                LanguageManager language = config.getLanguage();
                language.getPlaceholder("player_not_owner").parsePlaceholder("{chest}", language.getMessage(chest.getType().getLanguageKey())).sendMessage(e.getPlayer());
                return;
            }
            
            //The player must be sneaking for opening the gui or using the token.
            if(!e.getPlayer().isSneaking())
            {
                return;
            }
            
            e.setCancelled(true);
            
            switch(e.getAction())
            {
                case LEFT_CLICK_BLOCK:
                    if(chest instanceof IAutoSeller)
                    {
                        IAutoSeller autoSeller = (IAutoSeller)chest;
                        
                        boolean isEnabled = autoSeller.isAutoSellEnabled();     
                        autoSeller.setAutoSell(!isEnabled);
                        config.getLanguage().sendMessage(e.getPlayer(), isEnabled ? "autosell_disabled" : "autosell_enabled");
                    }
                    break;
                case RIGHT_CLICK_BLOCK:
                    SubTypeTokenItem tokenItem = SubTypeTokenItem.getTokenItem(e.getItem());
                    if(tokenItem == null || !PluginPermission.INTERACT_USETOKEN.hasPermission(e.getPlayer())) //Check if the player has a token and if he has the permission to use it.
                    {
                        chest.getChestGui().open(e.getPlayer());
                        return;
                    }

                    ChestType chestType = tokenItem.getChestType();
                    if(chestType != chest.getType()) //Check if the chestType is the same of the token used.
                    {
                        LanguageManager language = this.config.getLanguage();
                        language.getPlaceholder("wrong_token_type").parsePlaceholder("{chest}", language.getMessage(chestType.getLanguageKey())).sendMessage(e.getPlayer());
                        return;
                    }

                    if(!chest.addSpecificType(tokenItem.getSubType())) //Adding the subType to the chest and check if already exists.
                    {
                        config.getLanguage().sendMessage(e.getPlayer(), "token_subtype_already_exist");
                        return;
                    }
                    //Decrease the ItemStack in the player inventory
                    if(e.getPlayer().getGameMode() != GameMode.CREATIVE)
                    {
                        ItemUtil.decreaseItemStack(e.getItem(), e.getPlayer(), e.getPlayer().getInventory());
                    }
                    break;
            }
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityExplode(final EntityExplodeEvent e)
    {
        WorldManager worldManager = worldRegistry.getWorldManager(e.getLocation().getWorld());
        e.blockList().stream().map(Block::getLocation).forEach(worldManager::removeChest);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onBlockExplode(final BlockExplodeEvent e)
    {
        WorldManager worldManager = worldRegistry.getWorldManager(e.getBlock().getWorld());
        e.blockList().stream().map(Block::getLocation).forEach(worldManager::removeChest);
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChunkUnload(final ChunkUnloadEvent e)
    {
        worldRegistry.getWorldManager(e.getWorld()).unloadChunk(e.getChunk());
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onChunkLoad(final ChunkLoadEvent e)
    {
        worldRegistry.getWorldManager(e.getWorld()).loadChunk(e.getChunk());
    }
}

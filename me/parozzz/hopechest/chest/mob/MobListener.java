/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import java.util.Iterator;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.mob.HeadHunting.HeadInfo;
import me.parozzz.hopechest.configuration.HeadHuntingConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.hopechest.world.TypeContainer;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class MobListener implements Listener
{
    private final WorldRegistry worldRegistry;
    private final HopeChestConfiguration config;
    public MobListener(final WorldRegistry worldRegistry, final HopeChestConfiguration config)
    {
        this.worldRegistry = worldRegistry;
        this.config = config;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityDeath(final EntityDeathEvent e)
    {
        if(e.getEntityType() == EntityType.PLAYER)
        {
            return;
        }
        
        Location loc = e.getEntity().getLocation();
            
        CreatureType creatureType = CreatureType.getByLivingEntity(e.getEntity());

        HeadHuntingConfig headHunting = config.getHeadHuntingConfig();
        if(headHunting.doesAlwaysDrop())
        {
            e.getDrops().add(headHunting.getHeadInfo(creatureType).getHead());
        }

        TypeContainer typeContainer = worldRegistry.getWorldManager(loc.getWorld()).getByChunk(loc.getChunk());
        if(typeContainer != null)
        {
            MobConfig mobConfig = (MobConfig)config.getConfig(ChestType.MOB);

            Iterator<ItemStack> it = e.getDrops().iterator();
            while(it.hasNext())
            {
                if(mobConfig.isBlacklisted(it.next().getType()))
                {
                    it.remove();
                }
            }

            ItemStack head = null;
            if(!headHunting.doesAlwaysDrop())
            {
                head = headHunting.getHeadInfo(creatureType).getHead();
                e.getDrops().add(head);
            }

            typeContainer.addMobItemStacks(e.getDrops(), creatureType);
            e.getDrops().remove(head); //Remove the head. If is null, nothing will be done.
        }
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onPlayerHeadInteract(final PlayerInteractEvent e)
    {
        if(!ItemUtil.nonNull(e.getItem()) && e.getItem().getType() == Material.SKULL_ITEM && e.getItem().getDurability() == 3)
        {
            NMSStackCompound stack = new NMSStackCompound(e.getItem());
            
            HeadInfo headInfo = HeadHunting.getHeadInfo(stack);
            if(headInfo == null)
            {
                return;
            }
            
            
        }
    }
}

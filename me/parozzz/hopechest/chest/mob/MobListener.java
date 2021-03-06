/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import java.util.Iterator;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.configuration.HeadHuntingConfig;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.hopechest.world.TypeContainer;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
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
        
        CreatureType creatureType = CreatureType.getByLivingEntity(e.getEntity());

        HeadHuntingConfig headHunting = config.getHeadHuntingConfig();
        if(headHunting.isEnabled() && headHunting.doesAlwaysDrop())
        {
            e.getDrops().add(headHunting.getHeadInfo(creatureType).getHead());
        }

        TypeContainer typeContainer = worldRegistry.getWorldManager(e.getEntity().getWorld()).getByChunk(e.getEntity().getLocation().getChunk());
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
            if(headHunting.isEnabled() && !headHunting.doesAlwaysDrop())
            {
                head = headHunting.getHeadInfo(creatureType).getHead();
                e.getDrops().add(head);
            }

            typeContainer.addMobItemStacks(e.getDrops(), creatureType);
            e.getDrops().remove(head); //Remove the head. If is null, nothing will be done.
        }
    }
}

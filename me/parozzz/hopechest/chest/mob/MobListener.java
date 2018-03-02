/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import me.parozzz.hopechest.world.TypeContainer;
import me.parozzz.hopechest.world.WorldRegistry;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 *
 * @author Paros
 */
public class MobListener implements Listener
{
    private final WorldRegistry worldRegistry;
    public MobListener(final WorldRegistry worldRegistry)
    {
        this.worldRegistry = worldRegistry;
    }
    
    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    private void onEntityDeath(final EntityDeathEvent e)
    {
        if(e.getEntityType() != EntityType.PLAYER)
        {
            Location loc = e.getEntity().getLocation();
            
            TypeContainer typeContainer = worldRegistry.getWorldManager(loc.getWorld()).getByChunk(loc.getChunk());
            if(typeContainer != null)
            {
                CreatureType creatureType = CreatureType.getByLivingEntity(e.getEntity());
                typeContainer.addMobItemStacks(e.getDrops(), creatureType);
            }
        }
    }
}
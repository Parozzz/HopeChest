/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.mob;

import java.util.Iterator;
import me.parozzz.hopechest.chest.ChestType;
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
        if(e.getEntityType() != EntityType.PLAYER)
        {
            Location loc = e.getEntity().getLocation();
            
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
                
                CreatureType creatureType = CreatureType.getByLivingEntity(e.getEntity());
                typeContainer.addMobItemStacks(e.getDrops(), creatureType);
            }
        }
    }
}

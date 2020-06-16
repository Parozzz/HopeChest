/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest;

import java.util.UUID;
import me.parozzz.hopechest.chest.autosell.IAutoSeller;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.world.WorldManager;
import org.bukkit.Location;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.InventoryHolder;

/**
 *
 * @author Paros
 */
public abstract class AutoSellerChestAbstract<T> extends AbstractChest<T> implements IAutoSeller
{
    
    public <H extends BlockState & InventoryHolder> AutoSellerChestAbstract(final UUID owner, final WorldManager worldManager, final Location loc,
           final HopeChestConfiguration configuration, final DatabaseManager databaseManager) 
    {
        super(owner, worldManager, loc, configuration, databaseManager);
    }
    
    private int autoSellTimer = -1;
    @Override
    public void tickAutoSell() 
    {
        if(autoSellTimer == -1)
        {
            this.resetSellTimer();
        }
        else if(autoSellTimer-- <= 0)
        {
            
            this.resetSellTimer();
        }
    }
    
    @Override
    public void resetSellTimer()
    {
        autoSellTimer = super.getConfiguration().getAutoSellDelay();
    }
 
    @Override
    public int getRemainingSeconds() 
    {
        return autoSellTimer;
    }
    
    @Override
    public void setAutoSell(final boolean active)
    {
        IAutoSeller.super.setRawAutoSell(active);
        
        super.getDatabaseManager().getChestTable().updateAutoSell(this);
    }
}

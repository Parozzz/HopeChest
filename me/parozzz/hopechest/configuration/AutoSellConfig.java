package me.parozzz.hopechest.configuration;

import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.configuration.ConfigurationSection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
 
public class AutoSellConfig implements IConfig
{
    private final AutoSellGuiConfig guiConfig;
    protected AutoSellConfig()
    {
        this.guiConfig = new AutoSellGuiConfig();
    }

    private int sellDelaySeconds;
    
    @Override
    public void load(ConfigurationSection path) 
    {
        sellDelaySeconds = path.getInt("sellDelay");

        guiConfig.load(path.getConfigurationSection("Gui"));
    }
    
    /**
     * Get the delay for the auto sell function
     * @return The delay in seconds.
     */
    public int getSellDelay()
    {
        return sellDelaySeconds;
    }

    public AutoSellGuiConfig getGuiConfig()
    {
        return guiConfig;
    }
    
    public class AutoSellGuiConfig implements IConfig
    {
        private String title;
        private NMSStackCompound onStack;
        private NMSStackCompound offStack;
        
        @Override
        public void load(ConfigurationSection path) 
        {
            title = path.getString("title");
            
            onStack = new NMSStackCompound(ItemUtil.getItemByPath(path.getConfigurationSection("onItem")));
            offStack = new NMSStackCompound(ItemUtil.getItemByPath(path.getConfigurationSection("offItem")));
        }
        
        public NMSStackCompound getOnStack()
        {
            return onStack.clone();
        }
        
        public NMSStackCompound getOffStack()
        {
            return offStack.clone();
        }
        
        public String getTitle()
        {
            return title;
        }
    }
}
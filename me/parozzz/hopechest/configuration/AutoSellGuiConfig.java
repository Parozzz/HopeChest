
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public class AutoSellGuiConfig implements IConfig
{
    private final HopeChestConfiguration configuration;
    public AutoSellGuiConfig(final HopeChestConfiguration configuration)
    {
        this.configuration = configuration;
    }
    
    
    @Override
    public void load(ConfigurationSection path) 
    {
      	String title = Util.cc(path.getString("title"));
        int sellDelay = path.getInt("sellDelay");
      
      	ConfigurationSection guiPath = path.getConfigurationSection("gui");
      
        NMSStackCompound onStack = new NMSStackCompound(ItemUtil.getItemByPath(guiPath.getConfigurationSection("onItem"));
        NMSStackCompound offStack = new NMSStackCompound(ItemUtil.getItemByPath(guiPath.getConfigurationSection("offItem"));
    }
    
}

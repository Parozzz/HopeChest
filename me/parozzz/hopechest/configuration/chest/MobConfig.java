/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration.chest;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopechest.configuration.chest.ChestConfig;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public final class MobConfig extends ChestConfig
{
    private final static Logger logger = Logger.getLogger(MobConfig.class.getSimpleName());
    
    private final LanguageManager mobNames;
    private final Set<Material> blacklist;
    public MobConfig() 
    {
        super(ChestType.MOB);
        
        mobNames = new LanguageManager();
        blacklist = EnumSet.noneOf(Material.class);
    }
    
    @Override
    public void load(final ConfigurationSection path)
    {
        super.load(path);
        mobNames.loadSection(path.getConfigurationSection("MobNames"));
        
        blacklist.clear();
        path.getStringList("itemBlacklist").stream().forEach(value -> 
        {
            try {
                blacklist.add(Material.valueOf(value.toUpperCase()));
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "An item named {0} does not exist. Skipping in itemBlacklist.", value);
            }
        });
    }
    
    public boolean isBlacklisted(final Material type)
    {
        return blacklist.contains(type);
    }
    
    public String getMobName(final CreatureType ct)
    {
        return mobNames.getMessage(ct.name());
    }
    
}

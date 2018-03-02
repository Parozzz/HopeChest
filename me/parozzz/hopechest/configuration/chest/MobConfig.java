/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration.chest;

import me.parozzz.hopechest.configuration.chest.ChestConfig;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public final class MobConfig extends ChestConfig
{
    private final LanguageManager mobNames;
    public MobConfig() 
    {
        super(ChestType.MOB);
        mobNames = new LanguageManager();
    }
    
    @Override
    public void load(final ConfigurationSection path)
    {
        super.load(path);
        mobNames.loadSection(path.getConfigurationSection("MobNames"));
    }
    
    public String getMobName(final CreatureType ct)
    {
        return mobNames.getMessage(ct.name());
    }
    
}

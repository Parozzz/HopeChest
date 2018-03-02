/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration.chest;

import me.parozzz.hopechest.configuration.chest.ChestConfig;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.reflex.language.LanguageManager;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public final class CropConfig extends ChestConfig
{
    private final LanguageManager languageManager;
    public CropConfig() 
    {
        super(ChestType.CROP);
        
        languageManager = new LanguageManager();
    }
    
    @Override
    public void load(final ConfigurationSection path)
    {
        super.load(path);
        languageManager.loadSection(path.getConfigurationSection("CropNames"));
    }
    
    public String getCropName(final CropType ct)
    {
        return languageManager.getMessage(ct.name());
    }
}

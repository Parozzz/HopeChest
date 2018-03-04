/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.hopechest.configuration.chest.CropConfig;
import me.parozzz.hopechest.configuration.chest.ChestConfig;
import java.util.EnumMap;
import java.util.Map;
import me.parozzz.hopechest.HopeChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Paros
 */
public final class HopeChestConfiguration 
{
    private final HopeChest hopeChest;
    private final Map<ChestType, ChestConfig> chestConfigs;
    private final GuiConfig guiConfig;
    private final LanguageManager languageManager;
    public HopeChestConfiguration(final HopeChest hopeChest)
    {
        this.hopeChest = hopeChest;
        hopeChest.getDataFolder().mkdir();
        hopeChest.saveDefaultConfig();
        
        chestConfigs = new EnumMap(ChestType.class);
        chestConfigs.put(ChestType.MOB, new MobConfig());
        chestConfigs.put(ChestType.CROP, new CropConfig());
        
        guiConfig = new GuiConfig(this);
        
        languageManager = new LanguageManager();
        load();
    }
    
    public final void reLoad()
    {
        hopeChest.reloadConfig();
        load();
    }
    
    private NMSStackCompound subTypeToken;
    private boolean ownerProtection;
    private int maxPlayerChest;
    private void load()
    {
        FileConfiguration config = hopeChest.getConfig();
        languageManager.loadSection(config.getConfigurationSection("Language"));
        
        ownerProtection = config.getBoolean("ownerProtection");
        maxPlayerChest = config.getInt("maxPlayerChest");
        
        guiConfig.load(config.getConfigurationSection("Gui"));
        chestConfigs.get(ChestType.MOB).load(config.getConfigurationSection("Mob"));
        chestConfigs.get(ChestType.CROP).load(config.getConfigurationSection("Crop"));
        
        subTypeToken = new NMSStackCompound(ItemUtil.getItemByPath(config.getConfigurationSection("TokenItem")));
    }
    
    public int getMaxPlayerChests()
    {
        return maxPlayerChest;
    }
    
    public boolean hasOwnerProtection()
    {
        return ownerProtection;
    }
    
    public NMSStackCompound getSubTypeToken()
    {
        return subTypeToken.clone();
    }
    
    public LanguageManager getLanguage()
    {
        return languageManager;
    }
    
    public GuiConfig getGuiConfig()
    {
        return guiConfig;
    }
    
    public ChestConfig getConfig(final ChestType chestType)
    {
        return chestConfigs.get(chestType);
    }
}

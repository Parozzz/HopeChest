/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopechest.chest.mob.HeadHunting;
import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/**
 *
 * @author Paros
 */
public class HeadHuntingConfig implements IConfig
{
    private static final Logger logger = Logger.getLogger(HeadHuntingConfig.class.getName());
    
    private final MobConfig mobConfig;
    public HeadHuntingConfig(final MobConfig mobConfig)
    {
        this.mobConfig = mobConfig;
    }

    private boolean enabled;
    private boolean alwaysDrop;
    private NMSStackCompound defaultHeadStack;
    private final Map<CreatureType, HeadInfo> headInfoMap = new EnumMap(CreatureType.class);
    @Override
    public void load(final ConfigurationSection path) 
    {
        enabled = path.getBoolean("enabled");
        if(!enabled)
        {
            return;
        }
        
        alwaysDrop = path.getBoolean("alwaysDrop");
        headInfoMap.values().forEach(info -> info.cachedHead = null);
        
        ConfigurationSection headValuePath = path.getConfigurationSection("HeadValues");
        headValuePath.getKeys(false).forEach(key -> 
        {
            CreatureType type;
            try {
                type = CreatureType.valueOf(key.toUpperCase());
            } catch(final IllegalArgumentException ex) {
                logger.log(Level.WARNING, "A mob named {0} does not exists. Skipping in the HeadValues HeadHunting section.", key);
                return;
            }
            
            HeadInfo info = headInfoMap.computeIfAbsent(type, HeadInfo::new);
            info.setValue(headValuePath.getDouble(key));
        });
        
        defaultHeadStack = new NMSStackCompound(ItemUtil.getItemByPath(Material.SKULL_ITEM, (short)3, path.getConfigurationSection("MobHead")));
    }
    
    public boolean isEnabled()
    {
        return enabled;
    }
    
    public boolean doesAlwaysDrop()
    {
        return alwaysDrop;
    }
    
    public HeadInfo getHeadInfo(final CreatureType ct)
    {
        return headInfoMap.computeIfAbsent(ct, HeadInfo::new);
    }
    
    public class HeadInfo
    {
        private final CreatureType ct;
        private HeadInfo(final CreatureType ct)
        {
            this.ct = ct;
        }
        
        private double value = 1.0;
        private void setValue(final double value)
        {
            this.value = value;
        }
        
        public double getValue()
        {
            return value;
        }
        
        private ItemStack cachedHead;
        public ItemStack getHead()
        {
            if(cachedHead == null)
            {
                NMSStackCompound stack = defaultHeadStack.clone();
                HeadHunting.setStackCompoundData(stack, ct, value);
                cachedHead = stack.getItemStack();
                
                ItemMeta meta = cachedHead.getItemMeta();
                ItemUtil.parseMetaVariable(meta, "{cost}", String.format(Locale.ENGLISH, "%.00f", value));
                ItemUtil.parseMetaVariable(meta, "{mob}", mobConfig.getMobName(ct));
                cachedHead.setItemMeta(meta);
            }
            return cachedHead.clone();
        }
    }
}

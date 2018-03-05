/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopechest.chest.mob.HeadHunting;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class HeadHuntingConfig implements IConfig
{
    private static final Logger logger = Logger.getLogger(HeadHuntingConfig.class.getName());
    
    private final HopeChestConfiguration config;
    public HeadHuntingConfig(final HopeChestConfiguration config)
    {
        this.config = config;
    }

    private NMSStackCompound defaultHeadStack;
    private final Map<CreatureType, HeadInfo> headInfoMap = new EnumMap(CreatureType.class);
    @Override
    public void load(final ConfigurationSection path) 
    {
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
                HeadHunting.setStackCompoundData(stack, ct);
                cachedHead = stack.getItemStack();
            }
            return cachedHead;
        }
    }
}

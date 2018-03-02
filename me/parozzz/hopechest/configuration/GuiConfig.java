/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import java.util.EnumMap;
import java.util.Map;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.chest.gui.ChestGui;
import me.parozzz.hopechest.chest.mob.HeadHunting;
import me.parozzz.hopechest.configuration.chest.MobConfig;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.HeadUtil;
import me.parozzz.reflex.utilities.HeadUtil.MobHead;
import me.parozzz.reflex.utilities.ItemUtil;
import me.parozzz.reflex.utilities.Util;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

/**
 *
 * @author Paros
 */
public final class GuiConfig implements IConfig
{
    private final Map<CropType, ItemStack> cropItems;
    private final HopeChestConfiguration config;
    public GuiConfig(final HopeChestConfiguration config)
    {
        this.config = config;
        
        cropItems = new EnumMap(CropType.class);
    }
    
    private String title;
    private NMSStackCompound mobHeadStack;
    @Override
    public void load(final ConfigurationSection path) 
    {
        cachedHeads.clear();
        cropItems.clear();
        
        title = Util.cc(path.getString("title"));
        
        ItemStack mobHeadItemStack = ItemUtil.getItemByPath(Material.SKULL_ITEM, (short)3, path.getConfigurationSection("MobHead"));
        mobHeadStack = new NMSStackCompound(mobHeadItemStack);
        
        path.getConfigurationSection("Crop").getKeys(false).forEach(key -> 
        {
            CropType cropType;
            try {
                cropType = CropType.valueOf(key.toUpperCase());
            } catch(final IllegalArgumentException ex) {
                
                return;
            }
            
            ConfigurationSection localPath = path.getConfigurationSection("Crop."+key);
            NMSStackCompound stack = new NMSStackCompound(ItemUtil.getItemByPath(localPath));
            ChestGui.setStackData(stack, ChestType.CROP, cropType.name());
            cropItems.put(cropType, stack.getItemStack());
        });
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public ItemStack getCropItem(final CropType cropType)
    {
        return cropItems.get(cropType);
    }
    
    private final Map<CreatureType, ItemStack> cachedHeads = new EnumMap(CreatureType.class);
    public ItemStack getMobHead(final CreatureType ct)
    {
        ItemStack cachedItem = cachedHeads.computeIfAbsent(ct, type -> 
        {
            NMSStackCompound localMobHeadStack = mobHeadStack.clone();
            HeadHunting.setStackCompoundData(localMobHeadStack, type);
            ChestGui.setStackData(localMobHeadStack, ChestType.MOB, type.name());
            
            ItemStack itemStack = localMobHeadStack.getItemStack();
            ItemMeta meta = itemStack.getItemMeta();
            
            String mobName = ((MobConfig)config.getConfig(ChestType.MOB)).getMobName(type);
            if(mobName == null)
            {
                Bukkit.getLogger().info("MobName null. Type:" + type);
            }
            ItemUtil.parseMetaVariable(meta, "{mob}", mobName == null ? type.name() : mobName);
            HeadUtil.addTexture((SkullMeta)meta, MobHead.valueOf(type.name()).getUrl());
            itemStack.setItemMeta(meta);
            return itemStack;
        });
        
        return cachedItem.clone();
    }
}

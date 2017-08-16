/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.mob;

import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import me.parozzz.hopechestv2.Dependency;
import me.parozzz.hopechestv2.chests.ChestHandler;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import me.parozzz.hopechestv2.chests.Options;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import me.parozzz.hopechestv2.utilities.reflection.HeadUtils;
import me.parozzz.hopechestv2.utilities.reflection.HeadUtils.MobHead;
import me.parozzz.hopechestv2.utilities.reflection.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class MobManager 
{
    public final static String NBT_CREATURETYPE="HopeChest.CreatureType";
    public final static String NBT_HEADCOST="HopeChest.HeadValue"; 
    public final static String NBT_CONVERTCOST="HopeChest.MobConvertCost";
    
    public static boolean enabled=false;
    
    public static boolean headDropEnabled;
    public static boolean headDropAnyway;
    public static boolean headSellEnabled;
    
    public static Material chestType;
    
    public static Inventory gui;
    private static final EnumMap<CreatureType, MobOptions> options=new EnumMap(CreatureType.class);
    private static final EnumSet<Material> itemBlacklist=EnumSet.noneOf(Material.class);
    public static void clear()
    {
        options.clear();
        itemBlacklist.clear();
    }
    
    public static void init(final FileConfiguration c)
    {
        enabled=true;
        
        headDropEnabled=c.getBoolean("headDrop");
        headDropAnyway=headDropEnabled ? c.getBoolean("headDropAnyway") : false;
        headSellEnabled=headDropEnabled ? c.getBoolean("headSell") : false;
        
        itemBlacklist.addAll(c.getStringList("itemBlacklist").stream().map(String::toUpperCase).map(Material::valueOf).collect(Collectors.toSet()));
        
        ItemStack defaultHead=Utils.getItemByPath(Material.SKULL_ITEM, (short)3, c.getConfigurationSection("HeadItem"));
        ItemStack defaultMobChest=Utils.getItemByPath(c.getConfigurationSection("ContainerItem"));
        chestType=defaultMobChest.getType();
        ItemStack defaultGuiItem=Utils.getItemByPath(Material.SKULL_ITEM, (short)3, c.getConfigurationSection("GuiItem"));
        
        ConfigurationSection mPath=c.getConfigurationSection("Mob");
        gui=Bukkit.createInventory(null, ((mPath.getKeys(false).size()/9)+1)*9, Utils.color(c.getString("guiTitle")));
        mPath.getKeys(false).stream().map(mPath::getConfigurationSection).forEach(path -> 
        {          
            CreatureType ct=CreatureType.valueOf(path.getName().toUpperCase());
            
            String name=Utils.color(path.getString("name"));
            boolean canConvert=path.getBoolean("convert");
            
            ItemStack assignedChest=defaultMobChest.clone();
            assignedChest=Utils.parseItemVariable(assignedChest, "%mob%", name);
            try 
            {
                ItemNBT nbt=new ItemNBT(assignedChest);
                nbt.addValue(ChestHandler.NBT_CHESTTYPE, ChestType.MOB.name());
                nbt.addValue(ChestHandler.NBT_TYPE, ct.name());
                assignedChest=nbt.buildItem();
            } 
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) 
            {
                Logger.getLogger(MobManager.class.getName()).log(Level.SEVERE, null, ex);
            }        
            ChunkManager.addChestItem(ct, assignedChest);
            
            MobOptions mobOptions=new MobOptions(assignedChest, name, ct);       
            if(headDropEnabled && ct!=CreatureType.ALL)
            {
                ItemStack assignedHead=defaultHead.clone();
                HeadUtils.addTexture(assignedHead, MobHead.valueOf(ct.name()).getUrl());
                try 
                {
                    assignedHead=Utils.parseItemVariable(assignedHead, "%mob%", name);
                    
                    ItemNBT nbt=new ItemNBT(assignedHead);
                    nbt.addValue(NBT_CREATURETYPE, ct.name());
                    
                    if(Dependency.isEconomyEnabled() && headSellEnabled)
                    {
                        Double headValue=path.getDouble("headValue");
                        
                        nbt.addValue(NBT_HEADCOST, headValue);
                        assignedHead=Utils.parseItemVariable(nbt.buildItem(), "%money%", headValue.toString());
                    }
                    else
                    {
                        assignedHead=nbt.buildItem();
                    }
                    
                } 
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
                    Logger.getLogger(MobManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                mobOptions.setHead(assignedHead);
            }
            
            options.put(ct, mobOptions);
            
            if(canConvert)
            {
                ItemStack guiHead=defaultGuiItem.clone();
                guiHead=Utils.parseItemVariable(guiHead, "%mob%", name);
                if(ct==CreatureType.ALL)
                {
                    guiHead.setType(Material.valueOf(path.getString("guiId").toUpperCase()));
                }
                else
                {
                    HeadUtils.addTexture(guiHead, MobHead.valueOf(ct.name()).getUrl());
                }

                try 
                {
                    ItemNBT nbt=new ItemNBT(guiHead);
                    nbt.addValue(NBT_CREATURETYPE, ct.name());
                    if(Dependency.isEconomyEnabled())
                    {
                        Double cost=path.getDouble("convertCost", 0);
                        nbt.addValue(NBT_CONVERTCOST, cost);
                        guiHead=Utils.parseItemVariable(nbt.buildItem(), "%guiCost%", cost.toString());
                    }
                    else
                    {
                        guiHead=nbt.buildItem();
                    }

                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
                    Logger.getLogger(MobManager.class.getName()).log(Level.SEVERE, null, ex);
                }

                gui.addItem(guiHead); 
            }
        });
    }
    
    public static MobOptions getOptions(final CreatureType ct)
    {
        return options.get(ct);
    }
    
    public static boolean isBlacklisted(final Material type)
    {
        return itemBlacklist.contains(type);
    }
    
    public static class MobOptions extends Options
    {

        public MobOptions(final ItemStack chest, final String mobName, final CreatureType type) 
        {
            super(chest, mobName, type);
        }

        @Override
        public CreatureType getType()
        {
            return (CreatureType)super.getType();
        }
        
        private ItemStack head;
        public void setHead(final ItemStack head)
        {
            this.head=head;
        }
        
        public boolean hasHead()
        {
            return head!=null;
        }
        
        public ItemStack getHead()
        {
            return head.clone();
        }
    }
}

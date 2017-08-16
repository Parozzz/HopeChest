/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.crop;

import java.lang.reflect.InvocationTargetException;
import java.util.EnumMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.parozzz.hopechestv2.Dependency;
import me.parozzz.hopechestv2.chests.ChestHandler;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.chests.ChunkManager.ChestType;
import me.parozzz.hopechestv2.chests.Options;
import me.parozzz.hopechestv2.utilities.Utils;
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
public class CropManager 
{
    public static enum CropType
    {
        ALL,CACTUS,PISTONCANE,SUGARCANE,MELON,PUMPKIN,WATERCROP,CROP;
    }
    
    public final static String NBT_CROPTYPE="HopeChest.CropType";
    public final static String NBT_CONVERTCOST="HopeChest.CropConvertCost";
    
    public static boolean cactusGrowAnyway;
    public static boolean cactusFarm;
    
    public static boolean melonGrowAnyway;
    public static boolean pumpkinGrowAnyway;
    public static boolean sugarCaneGrowAnyway;
    
    public static Material chestType;
    public static Inventory gui;
    
    
    private static final EnumMap<CropType, CropOptions> options=new EnumMap(CropType.class);
    public static void clear()
    {
        options.clear();
    }
    
    public static void init(final FileConfiguration c)
    {
        cactusGrowAnyway=c.getBoolean("cactusGrowAnyway");
        cactusFarm=c.getBoolean("cactusFarm");
        
        melonGrowAnyway=c.getBoolean("melonGrowAnyway");
        pumpkinGrowAnyway=c.getBoolean("pumpkinGrowAnyway");
        sugarCaneGrowAnyway=c.getBoolean("sugarCaneGrowAnyway");
        
        ItemStack defaultChest=Utils.getItemByPath(c.getConfigurationSection("ContainerItem"));
        chestType=defaultChest.getType();
        
        ConfigurationSection cPath=c.getConfigurationSection("Crops");
        gui=Bukkit.createInventory(null, ((cPath.getKeys(false).size()/9)+1)*9, Utils.color(c.getString("guiTitle")));
        cPath.getKeys(false).stream().map(cPath::getConfigurationSection).forEach(path -> 
        {
            CropType ct=CropType.valueOf(path.getName().toUpperCase());
            
            String name=Utils.color(path.getString("name"));
            boolean canConvert=path.getBoolean("convert");
            
            ItemStack assignedChest=defaultChest.clone();
            assignedChest=Utils.parseItemVariable(assignedChest, "%crop%", name);
            try 
            {
                ItemNBT nbt=new ItemNBT(assignedChest);
                nbt.addValue(ChestHandler.NBT_CHESTTYPE, ChestType.CROP.name());
                nbt.addValue(ChestHandler.NBT_TYPE, ct.name());
                assignedChest=nbt.buildItem();
            } 
            catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) 
            {
                Logger.getLogger(CropManager.class.getName()).log(Level.SEVERE, null, ex);
            }        
            ChunkManager.addChestItem(ct, assignedChest);
            
            CropOptions option=new CropOptions(assignedChest, name, ct);
            
            if(canConvert)
            {
                ItemStack guiItem=Utils.getItemByPath(Material.valueOf(path.getString("guiId").toUpperCase()), (short)0, c.getConfigurationSection("GuiItem"));
                guiItem=Utils.parseItemVariable(guiItem, "%crop%", name);
                try 
                {
                    ItemNBT nbt=new ItemNBT(guiItem);
                    nbt.addValue(NBT_CROPTYPE, ct.name());
                    if(Dependency.isEconomyEnabled())
                    {
                        Double cost=path.getDouble("convertCost", 0);
                        nbt.addValue(NBT_CONVERTCOST, cost);
                        guiItem=Utils.parseItemVariable(nbt.buildItem(), "%guiCost%", cost.toString());
                    }
                    else
                    {
                        guiItem=nbt.buildItem();
                    }

                }
                catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException ex) {
                    Logger.getLogger(CropManager.class.getName()).log(Level.SEVERE, null, ex);
                }
                gui.addItem(guiItem);
            }
            options.put(ct, option);
        });
    }
    
    public static CropOptions getOptions(final CropType ct)
    {
        return options.get(ct);
    }
    
    public static class CropOptions extends Options
    {

        public CropOptions(final ItemStack chest,final String cropName, final CropType type) 
        {
            super(chest, cropName, type);
        }
        
        @Override
        public CropType getType()
        {
            return (CropType)super.getType();
        }
    }
}

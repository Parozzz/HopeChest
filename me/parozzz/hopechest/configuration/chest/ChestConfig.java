/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration.chest;

import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.configuration.IConfig;
import me.parozzz.reflex.NMS.itemStack.NMSStackCompound;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public abstract class ChestConfig<T> implements IConfig
{
    private final ChestType chestType;
    public ChestConfig(final ChestType chestType)
    {
        this.chestType = chestType;
    }
    
    public final ChestType getChestType()
    {
        return chestType;
    }
    
    public NMSStackCompound getStack()
    {
        return (NMSStackCompound)stack.clone();
    }
    
    private NMSStackCompound stack;
    @Override
    public void load(final ConfigurationSection path)
    {
        ItemStack itemStack = ItemUtil.getItemByPath(path.getConfigurationSection("ChestItem"));
        this.stack = new NMSStackCompound(itemStack);
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests;

import me.parozzz.hopechestv2.utilities.Utils;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public abstract class Options 
{
    private final Object type;
    private final ItemStack chest;
    private final String name;
    public Options(final ItemStack chest, final String name, final Object type)
    {
        this.chest=chest;
        this.name=name;
        this.type=type;
    }

    public Object getType()
    {
        return type;
    }

    public ItemStack getChest()
    {
        return chest.clone();
    }

    public String getName()
    {
        return name;
    }
}

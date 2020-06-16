/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.dependency;

import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public interface IWorth 
{
    /**
     * Get the cost of the itemStack
     * @param item The itemStack to get the cost of
     * @return The cost value or -1 if the item is not sellable.
     */
    public double getCost(final ItemStack item);
    
}

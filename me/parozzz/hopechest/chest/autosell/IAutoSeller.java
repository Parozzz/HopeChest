/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.autosell;

/**
 *
 * @author Paros
 */
public interface IAutoSeller 
{
    public default void setAutoSell(final boolean active)
    {
        if(active)
        {
            AutoSellRunnable.getInstance().addAutoSeller(this);
        }
        else
        {
            AutoSellRunnable.getInstance().removeAutoSeller(this);
        }
    }
    
    public void doAutoSell();
}

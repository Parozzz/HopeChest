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
    public void setAutoSell(final boolean active);
    
    public default void setRawAutoSell(final boolean active)
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
    
    public default boolean isAutoSellEnabled()
    {
        return AutoSellRunnable.getInstance().contains(this);
    }
    
    public void doAutoSell();
    public AutoSellGui getAutoSellGui();
    public void resetAutoSellGuiInstance();
}

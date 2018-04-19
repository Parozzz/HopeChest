/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.autosell;

import me.parozzz.hopechest.dependency.DependencyManager;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;

/**
 *
 * @author Paros
 */
public class AutoSellGui
{
    private static HopeChestConfiguration config;
    public static void setConfiguration(final HopeChestConfiguration configParam)
    {
        config = configParam;
    }
    
    private final AbstractChest chest;
    public AutoSellGui(final AbstractChest chest)
    {
        this.chest = chest;
    }
}

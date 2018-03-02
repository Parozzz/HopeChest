/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.configuration;

import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author Paros
 */
public interface IConfig 
{
    public void load(final ConfigurationSection path);
}

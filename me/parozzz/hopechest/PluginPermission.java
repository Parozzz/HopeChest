/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest;

import org.bukkit.command.CommandSender;

/**
 *
 * @author Paros
 */
public enum PluginPermission 
{
    COMMAND_GETCHEST("hopechest.command" , "getchest"),
    COMMAND_GIVECHEST("hopechest.command", "givechest"),
    COMMAND_GETTOKEN("hopechest.command", "gettoken"),
    COMMAND_GIVETOKEN("hopechest.command", "givetoken");
        
    private final String subSection;
    private final String perm;
    private PluginPermission(final String subSection, final String perm)
    {
        this.subSection = subSection;
        this.perm = perm;
    }

    public boolean hasPermission(final CommandSender cs)
    {
        if(cs == null)
        {
            return false;
        }
        
        return cs.hasPermission(subSection+".*") || cs.hasPermission(subSection+perm);
    }
}

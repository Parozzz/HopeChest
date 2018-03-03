/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Paros
 */
public class HChestTeleportCommand extends Command
{
    public HChestTeleportCommand()
    {
        super("hchestteleport");
    }
    
    @Override
    public boolean execute(CommandSender cs, String commandLabel, String[] val) 
    {
        if(!Player.class.isInstance(cs))
        {
            cs.sendMessage(ChatColor.RED + "This command can be only used by players.");
            return true;
        }
        
        if(val.length != 5)
        {
            cs.sendMessage(ChatColor.GRAY + "hchestteleport <worldName> <x> <y> <z>");
            return true;
        }
        
        World world = Bukkit.getWorld(val[0]);
        if(world == null)
        {
            return true;
        }
        
        try {
            double x = Double.parseDouble(val[1]);
            double y = Double.parseDouble(val[2]);
            double z = Double.parseDouble(val[3]);
            
            ((Player)cs).teleport(new Location(world, x, y, z));
        } catch(final NumberFormatException ex) {
            
        }
        
        return true;
    }
    
}

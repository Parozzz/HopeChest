/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopechestv2.Configs.MessageEnum;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.chests.crop.CropManager;
import me.parozzz.hopechestv2.chests.crop.CropManager.CropType;
import me.parozzz.hopechestv2.chests.mob.MobManager;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Paros
 */
public class MainCommand implements CommandExecutor
{
    
    private final String[] list;
    private final Map<String, ItemStack> stringChests;
    public MainCommand()
    {
        stringChests=new HashMap<>(ChunkManager.getChestMap());
        
        list=new String[2];
        list[0]=ChatColor.GREEN+"CROP -> "+
                ChatColor.GRAY+Stream.of(CropType.values())
                        .map(Enum::name)
                        .map(str -> str.equalsIgnoreCase("ALL")?"CROPALL":str)
                        .filter(stringChests::containsKey)
                        .map(String::toLowerCase)
                        .collect(Collectors.joining(", "));
        
        list[1]=ChatColor.GREEN+"MOB -> "+
                ChatColor.GRAY+Stream.of(CreatureType.values())
                        .map(Enum::name)
                        .map(str -> str.equalsIgnoreCase("ALL")?"MOBALL":str)
                        .filter(stringChests::containsKey)
                        .map(String::toLowerCase).collect(Collectors.joining(", "));
    }
    
    private final EnumSet<MessageEnum> help=EnumSet.of(MessageEnum.CROPHELP, MessageEnum.MOBHELP, MessageEnum.LISTHELP, MessageEnum.GIVEHELP, MessageEnum.RELOADHELP);
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmd, String alias, String[] val) 
    {
        if(val.length==0)
        {
            help.forEach(message -> message.send(cs));
            return true;
        }
        
        if(cs.hasPermission(Permissions.ADMIN_COMMAND))
        {
            switch(val[0].toLowerCase())
            {
                case "reload":
                    try 
                    {
                        JavaPlugin.getPlugin(HopeChest.class).load(true);
                        return MessageEnum.RELOADED.send(cs);
                    } 
                    catch (FileNotFoundException | UnsupportedEncodingException ex) 
                    {
                        Logger.getLogger(MainCommand.class.getName()).log(Level.SEVERE, null, ex);
                        return true;
                    }
                case "list":
                    cs.sendMessage(list);
                    return true;
                case "give":
                    if(val.length<3)
                    {
                        return MessageEnum.GIVEHELP.send(cs);
                    }
                    
                    return Optional.ofNullable(stringChests.get(val[1].toUpperCase())).map(ItemStack::clone)
                            .map(item -> 
                            {
                                if(val.length==4 && Utils.isNumber(val[3]))
                                {
                                    item.setAmount(Integer.valueOf(val[3]));
                                }
                                return item;
                            })
                            .map(item -> 
                            {
                                (val[2].equalsIgnoreCase("*") ? 
                                        Bukkit.getOnlinePlayers().stream() : 
                                        Optional.ofNullable(Bukkit.getPlayer(val[2])).map(p -> Stream.of(p)).orElseGet(() -> 
                                        { 
                                            MessageEnum.PLAYERWRONG.send(cs);
                                            return Stream.empty();
                                        }))
                                        .filter(p -> 
                                        {
                                            Collection<ItemStack> notFit=p.getInventory().addItem(item).values();
                                            if(notFit.isEmpty())
                                            {
                                                p.sendMessage(MessageEnum.ITEMRECEIVE.get()
                                                        .replace("%item%", item.getItemMeta().hasDisplayName()? item.getItemMeta().getDisplayName():item.getType().name())
                                                        .replace("%amount%", Objects.toString(item.getAmount())));
                                            }
                                            else
                                            {
                                                notFit.forEach(remained -> p.getLocation().getWorld().dropItem(p.getLocation(), remained));
                                            }
                                            return !notFit.isEmpty();
                                        }).forEach(p -> MessageEnum.INVENTORYFULL.send(p));
                                return true;
                            }).orElseGet(() -> MessageEnum.ITEMWRONG.send(cs));
            }
        }
        
        if(cs instanceof Player)
        {
            if(val[0].equalsIgnoreCase("mob"))
            {
                if(!cs.hasPermission(Permissions.MOB_COMMAND))
                {
                    return MessageEnum.NOPERMISSION.send(cs);
                }
                ((Player)cs).openInventory(MobManager.gui);
            }
            else if(val[0].equalsIgnoreCase("crop"))
            {
                if(!cs.hasPermission(Permissions.CROP_COMMAND))
                {
                    return MessageEnum.NOPERMISSION.send(cs);
                }
                ((Player)cs).openInventory(CropManager.gui);
            }
        }
        return true;
    }
    
}

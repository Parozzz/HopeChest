/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2;

import java.util.EnumMap;
import java.util.Optional;
import me.parozzz.hopechestv2.utilities.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

/**
 *
 * @author Paros
 */
public class Configs 
{
    public enum MessageEnum
    {
        LISTHELP(Permissions.ADMIN_COMMAND), 
        MOBHELP(Permissions.MOB_COMMAND),
        CROPHELP(Permissions.CROP_COMMAND), 
        GIVEHELP(Permissions.ADMIN_COMMAND), 
        RELOADHELP(Permissions.ADMIN_COMMAND), RELOADED(""),
        PLAYERWRONG(""), ITEMWRONG(""), INVENTORYFULL(""),
        ITEMRECEIVE(""), CHESTCONVERTION(""), NAMEDALREADY(""), CONTAINERWRONG(""),
        NOPERMISSION(""), MONEYLOW(""), HEADSOLD(""), CHUNKLIMIT(""), 
        CHESTPROTECTED(""), CHESTDOUBLED("");
        
        private final String perm;
        private MessageEnum(final String perm)
        {
            this.perm=perm;
        }
        
        public boolean send(final CommandSender cs)
        {
            if(perm.isEmpty() || cs.hasPermission(perm))
            {
                Optional.of(messages.get(this)).filter(str -> !str.isEmpty()).ifPresent(message -> cs.sendMessage(message));
            }
            return true;
        }
        
        public String get()
        {
            return messages.get(this);
        }
    }
    
    public static boolean doubleChestEnabled;
    public static boolean antigriefEnabled;
    public static int chunkLimit;
    public static boolean convertRenamedEnable;
    public static int maxConvert;
    
    private final static EnumMap<MessageEnum, String> messages=new EnumMap(MessageEnum.class);

    public static void clear()
    {
        messages.clear();
    }
    
    public static void init(final FileConfiguration c)
    {
        antigriefEnabled=c.getBoolean("antigriefEnabled");
        doubleChestEnabled=c.getBoolean("doubleChest");
        chunkLimit=c.getInt("chunkLimit");
        convertRenamedEnable=c.getBoolean("convertRenamedChest");
        maxConvert=c.getInt("maxConvert");
        
        ConfigurationSection mPath=c.getConfigurationSection("Messages");
        mPath.getKeys(false).forEach(str -> messages.put(MessageEnum.valueOf(str.toUpperCase()), Utils.color(mPath.getString(str))));
    }
}

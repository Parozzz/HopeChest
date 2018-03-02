/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.SubTypeTokenItem;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.utilities.PlayerUtil;
import me.parozzz.hopechest.world.ChestFactory;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public final class HChestCommand extends Command
{
    private static final Logger logger = Logger.getLogger(HChestCommand.class.getName());
    
    private final HopeChestConfiguration config;
    private final ChestFactory chestFactory;
    private final Map<String, SubCommand> subCommandMap;
    public HChestCommand(final HopeChestConfiguration config, final ChestFactory chestFactory, final DatabaseManager database)
    {
        super("hchest");
        
        this.setAliases(Arrays.asList("hpc", "ch", "assignchest"));
        
        this.config = config;
        this.chestFactory = chestFactory;
        
        subCommandMap = new HashMap<>();
        subCommandMap.put("getchest", new SubCommand(PluginPermission.COMMAND_GETCHEST, "command_getchest", 1).setPlayerConsumer((p, val) -> 
        {
            ChestType chestType = this.getChestType(p, val[0]);
            if(chestType == null)
            {
                return;
            }
            
            ItemStack toGet = val.length == 1 
                    ? chestFactory.getEmptyItemStack(chestType)
                    : getChestWithSubTypes(p, p, chestType, Stream.of(val).skip(1).toArray(String[]::new));
            if(toGet != null)
            {
                PlayerUtil.sendItemStack(toGet, p);
            }
        }));
        
        subCommandMap.put("givechest", new SubCommand(PluginPermission.COMMAND_GIVECHEST, "command_givechest", 2).setGeneralConsumer((cs, val) -> 
        {
            Player receiver = Bukkit.getPlayer(val[0]);
            if(receiver == null)
            {
                config.getLanguage().sendMessage(cs, "player_offline");
                return;
            }
            
            subCommandMap.get("getchest").executeConsumer(receiver, Stream.of(val).skip(1).toArray(String[]::new));
        }));
        
        subCommandMap.put("gettoken", new SubCommand(PluginPermission.COMMAND_GETTOKEN, "command_gettoken", 2).setPlayerConsumer((p, val) -> 
        {
            ChestType chestType = this.getChestType(p, val[0]);
            Object subType;
            if(chestType == null || (subType = getSubType(p, chestType, val[1])) == null)
            {
                return;
            }
            
            SubTypeTokenItem tokenItem = chestFactory.getToken(chestType, subType);
            if(tokenItem == null)
            {
                config.getLanguage().sendMessage(p, "command_error");
                return;
            }
            
            PlayerUtil.sendItemStack(tokenItem.getItemStack(), p);
        }));
        
        subCommandMap.put("givetoken", new SubCommand(PluginPermission.COMMAND_GIVETOKEN, "command_givetoken", 3).setGeneralConsumer((cs, val) -> 
        {
            Player receiver = Bukkit.getPlayer(val[0]);
            if(receiver == null)
            {
                config.getLanguage().sendMessage(cs, "player_offline");
                return;
            }
            
            subCommandMap.get("gettoken").executeConsumer(cs, Stream.of(val).skip(1).toArray(String[]::new));
        }));
        
        subCommandMap.put("reload", new SubCommand(PluginPermission.COMMAND_RELOAD, "command_reload", 0).setGeneralConsumer((cs, val) -> 
        {
            try {
                config.reLoad();
                config.getLanguage().sendMessage(cs, "configuration_reloaded");
            } catch(final Exception ex) {
                logger.log(Level.SEVERE, "Error during reloading the configuration", ex);
                config.getLanguage().sendMessage(cs, "configuration_reload_error");
            }
        }));
    }
    
    private @Nullable ItemStack getChestWithSubTypes(final CommandSender cs, final Player player, final ChestType chestType, final String[] array)
    {
        if(chestType == null || array == null)
        {
            return null;
        }
        
        String lastType = null;
        try {
            ItemStack toGive;
            switch(chestType)
            {
                case MOB:
                    CreatureType[] creatureTypes = new CreatureType[array.length];
                    for(int x = 0; x < array.length; x++)
                    {
                        lastType = array[x];
                        creatureTypes[x] = CreatureType.valueOf(lastType.toUpperCase());
                    }
                    toGive = chestFactory.getMobItemStack(creatureTypes);
                    break;
                case CROP:
                    CropType[] cropTypes = new CropType[array.length];
                    for(int x = 0; x < array.length; x++)
                    {
                        lastType = array[x];
                        cropTypes[x] = CropType.valueOf(lastType.toUpperCase());
                    }
                    toGive = chestFactory.getCropItemStack(cropTypes);
                    break;
                default:
                    return null;
            }
            
            return toGive;
        } catch(final IllegalArgumentException ex) {
            config.getLanguage().getPlaceholder("wrong_subtype").parsePlaceholder("{type}", lastType).sendMessage(cs);
            return null;
        }
    }
    
    private @Nullable Object getSubType(final CommandSender cs, final ChestType chestType, final String value)
    {
        try {
            switch(chestType)
            {
                case MOB:
                    return CreatureType.valueOf(value.toUpperCase());
                case CROP:
                    return CropType.valueOf(value.toUpperCase());
                default: 
                    return null;
            }
        } catch(final IllegalArgumentException ex) {
            config.getLanguage().getPlaceholder("wrong_subtype").parsePlaceholder("{type}", value).sendMessage(cs);
            return null;
        }
    }
    
    private @Nullable ChestType getChestType(final CommandSender cs, final String val)
    {
        try {
            return ChestType.valueOf(val.toUpperCase()); 
        } catch(final IllegalArgumentException ex) {
            config.getLanguage().getPlaceholder("no_such_chesttype").parsePlaceholder("{name}", val).sendMessage(cs);
            return null;
        }
    }
    
    
    @Override
    public boolean execute(CommandSender cs, String commandLabel, String[] val) 
    {
        if(val.length == 0)
        {
            return sendCommandHelp(cs);
        }
        
        SubCommand subCommand = subCommandMap.get(val[0].toLowerCase());
        if(subCommand == null)
        {
            config.getLanguage().getPlaceholder("command_wrong").parsePlaceholder("{command}", val[0]).sendMessage(cs);
            return true;
        }
        
        subCommand.executeConsumer(cs, Stream.of(val).skip(1).toArray(String[]::new));
        return true;
    }
    
    private boolean sendCommandHelp(final CommandSender cs)
    {
        subCommandMap.values().stream()
                .filter(help -> help.hasPermission(cs))
                .forEach(help -> help.sendHelpMessage(cs));
        return true;
    }
    
    private class SubCommand
    {
        private final PluginPermission perm;
        private final String helpMessageKey;
        private final int arraySize;
        private SubCommand(final PluginPermission perm, final String helpMessageKey, final int arraySize)
        {
            this.perm = perm;
            this.helpMessageKey = helpMessageKey;
            this.arraySize = arraySize;
        }
        
        private BiConsumer<CommandSender, String[]> generalConsumer;
        private SubCommand setGeneralConsumer(final BiConsumer<CommandSender, String[]> consumer)
        {
            this.generalConsumer = consumer;
            return this;
        }
        
        private BiConsumer<Player, String[]> playerConsumer;
        private SubCommand setPlayerConsumer(final BiConsumer<Player, String[]> consumer)
        {
            this.playerConsumer = consumer;
            return this;
        }
        
        public void executeConsumer(final CommandSender cs, final String[] array)
        {
            if(!this.hasPermission(cs))
            {
                config.getLanguage().sendMessage(cs, "no_permission");
            }
            else if(array.length < arraySize)
            {
                sendHelpMessage(cs);
            }
            else
            {
                if(generalConsumer != null)
                {
                    generalConsumer.accept(cs, array);
                }
                
                if(cs instanceof Player && playerConsumer != null)
                {
                    playerConsumer.accept((Player)cs, array);
                }
            }
        }
        
        private boolean hasPermission(final CommandSender cs)
        {
            return perm.hasPermission(cs);
        }
        
        private void sendHelpMessage(final CommandSender cs)
        {
            config.getLanguage().sendMessage(cs, helpMessageKey);
        }
    }
}

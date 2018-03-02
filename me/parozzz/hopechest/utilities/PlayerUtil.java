/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.utilities;

import java.util.Collection;
import me.parozzz.hopechest.configuration.HopeChestConfiguration;
import me.parozzz.reflex.language.LanguageManager;
import me.parozzz.reflex.utilities.ItemUtil;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author Paros
 */
public class PlayerUtil 
{
    private PlayerUtil() {}
    
    private static HopeChestConfiguration config;
    public static void setConfig(final HopeChestConfiguration configParam)
    {
        config = configParam;
    }
    
    public static void sendItemStack(final ItemStack itemStack, final HumanEntity player)
    {
        Collection<ItemStack> toDrop = player.getInventory().addItem(itemStack).values();
        LanguageManager languageManager = config.getLanguage();
        languageManager.getPlaceholder("item_received").parsePlaceholder("{item}", ItemUtil.getItemStackName(itemStack)).sendMessage(player);
        if(!toDrop.isEmpty())
        {
            languageManager.sendMessage(player, "inventory_full");

            Location loc = player.getLocation();
            toDrop.forEach(extraItem -> loc.getWorld().dropItem(loc, extraItem));
        }
    }
}

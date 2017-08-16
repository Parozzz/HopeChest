/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests.mob;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import me.parozzz.hopechestv2.Configs;
import me.parozzz.hopechestv2.Dependency;
import me.parozzz.hopechestv2.HopeChest;
import me.parozzz.hopechestv2.Configs.MessageEnum;
import me.parozzz.hopechestv2.chests.ChunkManager;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import me.parozzz.hopechestv2.utilities.reflection.ItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
/**
 *
 * @author Paros
 */
public class MobHandler implements Listener
{
    
    public MobHandler()
    {
        if(MobManager.headSellEnabled && Dependency.isEconomyEnabled())
        {
            Bukkit.getServer().getPluginManager().registerEvents(new Listener()
            {
                @EventHandler(ignoreCancelled=false, priority=EventPriority.HIGHEST)
                private void onPlayerInteract(final PlayerInteractEvent e)
                {
                    if(e.getAction()==Action.RIGHT_CLICK_AIR && e.getItem()!=null && e.getItem().getType()==Material.SKULL_ITEM && e.getItem().getDurability()==3)
                    {
                        Optional.ofNullable(ItemNBT.getKey(e.getItem(), MobManager.NBT_HEADCOST, double.class)).filter(value -> value!=0).ifPresent(value -> 
                        {
                            e.setCancelled(true);
                            double headSold=Arrays.asList(e.getPlayer().getInventory().getContents())
                                    .stream().filter(Objects::nonNull).filter(item -> item.isSimilar(e.getItem()))
                                    .mapToDouble(item -> 
                                    {
                                        e.getPlayer().getInventory().remove(item);
                                        return item.getAmount();
                                    }).sum();
                            e.getPlayer().sendMessage(MessageEnum.HEADSOLD.get().replace("%amount%", Objects.toString(headSold)).replace("%total%", Objects.toString(headSold*value)));
                            Dependency.eco.depositPlayer(e.getPlayer(), headSold*value);
                        });
                    }
                }
            }, JavaPlugin.getPlugin(HopeChest.class));
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onInventoryClick(final InventoryClickEvent e)
    {
        if(e.getInventory().equals(MobManager.gui))
        {
            e.setCancelled(true);
            if(e.getInventory().equals(e.getClickedInventory()) && e.getCurrentItem().getType()!=Material.AIR)
            {
                ItemStack hand=Utils.getMainHand(e.getWhoClicked().getEquipment());
                if(hand==null || hand.getType()!=MobManager.chestType)
                {
                    MessageEnum.CONTAINERWRONG.send(e.getWhoClicked());
                }
                else if(!Configs.convertRenamedEnable && (hand.getItemMeta().hasDisplayName() || hand.getItemMeta().hasLore()))
                {
                    MessageEnum.NAMEDALREADY.send(e.getWhoClicked());
                }
                else if(Optional.ofNullable(ItemNBT.getKey(e.getCurrentItem(), MobManager.NBT_CONVERTCOST, double.class))
                            .map(cost -> Dependency.eco.withdrawPlayer((Player)e.getWhoClicked(), cost).transactionSuccess())
                            .orElseGet(() -> true))
                {
                    CreatureType ct=CreatureType.valueOf(ItemNBT.getKey(e.getCurrentItem(), MobManager.NBT_CREATURETYPE, String.class));
                    Bukkit.getLogger().info(Objects.toString(Configs.maxConvert));
                    if(hand.getAmount()<=Configs.maxConvert)
                    {
                        Utils.setPlayerMainHand(e.getWhoClicked().getInventory(), MobManager.getOptions(ct).getChest());
                    }
                    else
                    {
                        e.getWhoClicked().getInventory().addItem(MobManager.getOptions(ct).getChest()).values()
                                .forEach(item -> e.getWhoClicked().getWorld().dropItem(e.getWhoClicked().getLocation(), item));
                        hand.setAmount(hand.getAmount() - Configs.maxConvert);
                    }
                    MessageEnum.CHESTCONVERTION.send(e.getWhoClicked());
                }
                else
                {
                    MessageEnum.MONEYLOW.send(e.getWhoClicked());
                }
            }
        }
    }
    
    @EventHandler(ignoreCancelled=true, priority=EventPriority.HIGHEST)
    public void onCreatureDeath(final EntityDeathEvent e)
    {
        Optional.ofNullable(MobManager.getOptions(CreatureType.getByLivingEntity(e.getEntity()))).ifPresent(option -> 
        {
            if(MobManager.headDropEnabled && MobManager.headDropAnyway && option.hasHead())
            {
                e.getDrops().add(option.getHead());
            }

            Optional.ofNullable(ChunkManager.getChestManager(e.getEntity().getLocation().getChunk())).ifPresent(manager -> 
            {
                boolean headDropped=!MobManager.headDropEnabled || MobManager.headDropAnyway || !option.hasHead();
                for(Inventory inv:Stream.concat(manager.getChestsByType(option.getType()).stream(), manager.getChestsByType(CreatureType.ALL).stream())
                        .map(b -> (InventoryHolder)b.getState())
                        .map(InventoryHolder::getInventory).toArray(Inventory[]::new))
                {
                    if(!headDropped)
                    {
                        e.getDrops().add(option.getHead());
                        headDropped=true;
                    }
                    
                    Collection<ItemStack> notFit=inv.addItem(e.getDrops().stream().toArray(ItemStack[]::new)).values();
                    e.getDrops().clear();
                    if(notFit.isEmpty())
                    {
                        break;
                    }
                    e.getDrops().addAll(notFit);
                }
            }); 
        });

    }
}

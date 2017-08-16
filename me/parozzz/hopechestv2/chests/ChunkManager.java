/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechestv2.chests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopechestv2.HopeChest;
import me.parozzz.hopechestv2.chests.crop.CropChest;
import me.parozzz.hopechestv2.chests.crop.CropManager;
import me.parozzz.hopechestv2.chests.crop.CropManager.CropType;
import me.parozzz.hopechestv2.chests.mob.MobChest;
import me.parozzz.hopechestv2.chests.mob.MobManager;
import me.parozzz.hopechestv2.utilities.Utils;
import me.parozzz.hopechestv2.utilities.Utils.CreatureType;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Paros
 */
public class ChunkManager 
{
    private final static String METADATA="HopeChest.Chest";
    public enum ChestType
    {
        MOB, CROP;
    }
    
    private final static Map<UUID, Set<HChest>> playerChests=new HashMap<>();
    private final static Map<String, ChestManager> chunks=new HashMap<>();
    
    public static void clear()
    {
        playerChests.clear();
        chunks.clear();
    }
    
    public static void addChest(final Chunk c, final Block b, final CreatureType type, final UUID owner)
    {
        addChest(Utils.chunkToString(c), b, type, owner);
    }
    
    public static void addChest(final String chunk, final Block b, final CreatureType type, final UUID owner)
    {
        MobChest hChest=new MobChest(owner, type, b);
        Optional.ofNullable(chunks.get(chunk)).orElseGet(() -> 
        {
            ChestManager manager=new ChestManager();
            chunks.put(chunk, manager);
            return manager;
        }).addChest(b, hChest);
        
        Optional.ofNullable(playerChests.get(owner)).orElseGet(() -> 
        {
            Set<HChest> set=new HashSet<>();
            playerChests.put(owner, set);
            return set;
        }).add(hChest);
    }
    
    public static void addChest(final Chunk c, final Block b, final CropType type, final UUID owner)
    {
        addChest(Utils.chunkToString(c), b, type, owner);
    }
    
    public static void addChest(final String chunk, final Block b, final CropType type, final UUID owner)
    {
        CropChest hChest=new CropChest(owner, type, b);
        Optional.ofNullable(chunks.get(chunk)).orElseGet(() -> 
        {
            ChestManager manager=new ChestManager();
            chunks.put(chunk, manager);
            return manager;
        }).addChest(b, hChest);
        
        Optional.ofNullable(playerChests.get(owner)).orElseGet(() -> 
        {
            Set<HChest> set=new HashSet<>();
            playerChests.put(owner, set);
            return set;
        }).add(hChest);
    }
 
    public static boolean isCustomChest(final Block b)
    {
        return b.hasMetadata(METADATA);
    }
    
    public static HChest getCustomChest(final Block b)
    {
        return (HChest)b.getMetadata(METADATA).get(0).value();
    }
    
    public static void removeChest(final Block b)
    {
        if(isCustomChest(b))
        {
            HChest hChest=getCustomChest(b);
            playerChests.get(hChest.getOwner()).remove(hChest);
            
            chunks.get(Utils.chunkToString(b.getChunk())).removeChest(b);
            b.removeMetadata(METADATA, JavaPlugin.getPlugin(HopeChest.class));
        }
    }
    
    public static ChestManager getChestManager(final Chunk c)
    {
        return getChestManager(Utils.chunkToString(c));
    }
    
    public static ChestManager getChestManager(final String chunk)
    {
        return chunks.get(chunk);
    }
    
    private static final Map<Object, ItemStack> chests=new HashMap<>();
    public static void addChestItem(final Object type, final ItemStack item)
    {
        chests.put(type, item);
    }
    
    public static ItemStack getChestItem(final Object type)
    {
        return chests.get(type);
    }
    
    public static Map<String, ItemStack> getChestMap()
    {
        return chests.entrySet().stream().collect(Collectors.toMap(entry -> 
        {
            if(entry.getKey().equals(CropType.ALL))
            {
                return "CROPALL";
            }
            else if(entry.getKey().equals(CreatureType.ALL))
            {
                return "MOBALL";
            }
            else
            {
                return entry.getKey().toString();
            }
        }, entry -> entry.getValue().clone()));
    }
    
    public static void saveData(final JavaPlugin instance)
    {
        Map<UUID, FileConfiguration> saveMap=new HashMap<>();
        chunks.values().stream().forEach(manager -> 
        {
            manager.chests.values().stream().flatMap(List::stream).forEach(b ->
            {
                HChest chest=getCustomChest(b);
                playerChests.remove(chest.getOwner());
                
                FileConfiguration c=Optional.ofNullable(saveMap.get(chest.getOwner())).orElseGet(() -> 
                {
                    FileConfiguration newConfig=new YamlConfiguration();
                    saveMap.put(chest.getOwner(), newConfig);
                    return newConfig;
                });
                
                List<String> chestList=c.getStringList(chest.getChestType().name());
                chestList.add(Stream.of(b.getWorld().getName(), b.getX(), b.getY(), b.getZ(), chest.getType()).map(Object::toString).collect(Collectors.joining(";")));
                c.set(chest.getChestType().name(), chestList);
            });
        });
        
        saveMap.forEach((u , c) -> 
        {
            try 
            {
                c.save(new File(instance.getDataFolder()+File.separator+"datafolder", u.toString()+".yml"));
            } 
            catch (IOException ex) 
            {
                Logger.getLogger(ChunkManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        
        playerChests.keySet().stream().map(u -> new File(instance.getDataFolder()+File.separator+"datafolder", u.toString()+".yml")).forEach(file -> file.delete());
    }
    
    public static void purgeData(final UUID u, final JavaPlugin instance)
    {
        if(Optional.ofNullable(playerChests.get(u)).filter(Set::isEmpty).isPresent())
        {
            playerChests.remove(u);
            new File(instance.getDataFolder()+File.separator+"datafolder", u.toString()+".yml").delete();
        }
    }
    
    public static void loadData(final JavaPlugin instance)
    {
        File dataFolder=new File(instance.getDataFolder()+File.separator+"datafolder");
        if(!dataFolder.exists())
        {
            return;
        }
        Stream.of(dataFolder.listFiles()).forEach(file -> 
        {
            UUID owner=UUID.fromString(file.getName().replace(".yml", ""));
            FileConfiguration c=YamlConfiguration.loadConfiguration(file);
            
            Stream.of(ChestType.values()).forEach(ct -> 
            {
                c.getStringList(ct.name()).stream().map(str -> str.split(";")).forEach(array -> 
                {
                    World w=Bukkit.getWorld(array[0]);

                    if(w==null)
                    {
                        Bukkit.getLogger().log(Level.SEVERE, "[HopeChest] World named {0} does not exist. Have you changed the name? Skipping block", array[0]);
                        return;
                    }

                    Block b=w.getBlockAt(Integer.valueOf(array[1]), Integer.valueOf(array[2]), Integer.valueOf(array[3]));
                    try
                    {
                        switch(ct)
                        {
                            case MOB:
                                if(b.getType()!=MobManager.chestType)
                                {
                                    Bukkit.getLogger().log(Level.SEVERE, "[HopeChest] Block at {0} is not of the same type as in config", Stream.of(b.getX(), b.getY(), b.getZ()).map(Object::toString).collect(Collectors.joining(",")));
                                }
                                ChunkManager.addChest(b.getChunk(), b, CreatureType.valueOf(array[4]), owner);
                                break;
                            case CROP:
                                if(b.getType()!=CropManager.chestType)
                                {
                                    Bukkit.getLogger().log(Level.SEVERE, "[HopeChest] Block at {0} is not of the same type as in config", Stream.of(b.getX(), b.getY(), b.getZ()).map(Object::toString).collect(Collectors.joining(",")));
                                }
                                ChunkManager.addChest(b.getChunk(), b, CropType.valueOf(array[4]), owner);
                                break;
                        }
                    }
                    catch(IllegalArgumentException ex)
                    {
                        Bukkit.getLogger().log(Level.CONFIG, "[HopeChest] Found not valid value. Using old versions? Skipping", ex);
                    }

                });
            });
        });
    }
    
    public static class ChestManager
    {
        private final Map<Object, List<Block>> chests;
        public ChestManager()
        {
            chests=new HashMap<>();
        }
        
        public void addChest(final Block b, final HChest chest)
        {
            Optional.ofNullable(chests.get(chest.getType())).orElseGet(() -> 
            { 
                List<Block> list=new ArrayList<>();
                chests.put(chest.getType(), list);
                return list;
            }).add(b);
            b.setMetadata(METADATA, new FixedMetadataValue(JavaPlugin.getPlugin(HopeChest.class), chest));
        }
        
        public boolean removeChest(final Block b)
        {
            return Optional.of(ChunkManager.getCustomChest(b))
                    .map(HChest::getType)
                    .map(chests::get)
                    .flatMap(Optional::ofNullable)
                    .map(set -> set.remove(b))
                    .orElseGet(() -> false);
        }
        
        public List<Block> getChestsByType(final Object obj)
        {
            return Optional.ofNullable(chests.get(obj)).orElseGet(() -> Collections.EMPTY_LIST);
        }
    }
}

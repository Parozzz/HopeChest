/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.chest.crop.CropChest;
import me.parozzz.reflex.utilities.TaskUtil;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class ChestTable 
{
    private static final Logger logger = Logger.getLogger(ChestTable.class.getName());
    
    private final DatabaseManager databaseManager;
    protected ChestTable(final DatabaseManager databaseManager)
    {
        this.databaseManager = databaseManager;
        
        try (Connection con = databaseManager.getConnection()) {
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS chests (world TEXT, x INTEGER, y INTEGER, z INTEGER, chunkX INTEGER, chunkZ INTEGER, type TEXT, subTypes TEXT);");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private final String ADD_CHEST = "INSERT INTO chests (world, x, y, z, chunkX, chunkZ, type, subTypes) VALUES(?, ?, ?, ?, ?, ?, ?, ?);";
    public void addChest(final AbstractChest chest)
    {
        Location loc = chest.getLocation();
        
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        String type = chest.getType().name();
        String subTypes = ((Stream<String>)chest.getSpecificTypes().stream().map(Objects::toString)).collect(Collectors.joining(","));
        
        TaskUtil.scheduleAsync(() -> 
        {
            try(Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(ADD_CHEST);
                prepared.setString(1, world);
                prepared.setInt(2, x);
                prepared.setInt(3, y);
                prepared.setInt(4, z);
                prepared.setInt(5, x >> 4);
                prepared.setInt(6, z >> 4);
                prepared.setString(7, type);
                prepared.setString(8, subTypes);
                prepared.executeUpdate();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private final String UPDATE_SUBTYPES = "UPDATE chests SET subTypes = ? WHERE world = ? AND x  = ? AND y  = ? AND z  = ?;";
    public void updateSubTypes(final AbstractChest chest)
    {
        Location loc = chest.getLocation();
        
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        String subTypes = ((Stream<String>)chest.getSpecificTypes().stream().map(Objects::toString)).collect(Collectors.joining(","));
        TaskUtil.scheduleAsync(() -> 
        {
            try(Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(UPDATE_SUBTYPES);
                prepared.setString(1, subTypes);
                prepared.setString(2, world);
                prepared.setInt(3, x);
                prepared.setInt(4, y);
                prepared.setInt(5, z);
                prepared.executeUpdate();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private final String REMOVE_CHEST = "DELETE FROM chests WHERE world = ? AND x = ? AND y = ? AND z = ? AND type = ?;";
    public void removeChest(final AbstractChest<Object> chest)
    {
        Location loc = chest.getLocation();
        
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        String type = chest.getType().name();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try(Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(REMOVE_CHEST);
                prepared.setString(1, world);
                prepared.setInt(2, x);
                prepared.setInt(3, y);
                prepared.setInt(4, z);
                prepared.setString(5, type);
                prepared.executeUpdate();
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        });
    }
    
    private final String CHUNK_QUERY = "SELECT * FROM chests WHERE world = ? AND chunkX = ? AND chunkZ = ?;";
    public void queryChunk(final Chunk chunk, final Consumer<ChestQuery> consumer)
    {
        World world = chunk.getWorld();
        
        String stringWorld = world.getName();
        int x = chunk.getX();
        int z = chunk.getZ();
        
        TaskUtil.scheduleAsync(() -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(CHUNK_QUERY);
                prepared.setString(1, stringWorld);
                prepared.setInt(2, x);
                prepared.setInt(3, z);
                
                List<ChestQuery> queryList = new LinkedList<>();
                
                ResultSet set = prepared.executeQuery();
                while(set.next())
                {
                    int qx = set.getInt("x");
                    int qy = set.getInt("y");
                    int qz = set.getInt("z");
                    ChestType chestType = ChestType.valueOf(set.getString("type"));
                    String subTypes = set.getString("subTypes");
                    
                    queryList.add(new ChestQuery(qx, qy, qz, chestType, subTypes));
                }
                
                            
                TaskUtil.scheduleSync(() -> 
                {
                    queryList.forEach(query -> 
                    {
                        query.loadLocation(world);
                        consumer.accept(query);
                    });
                });
                
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } 
        });
    }
    
    public final class ChestQuery
    {
        private final int x;
        private final int y;
        private final int z;
        private final ChestType chestType;
        private final String subTypes;
        private ChestQuery(final int x, final int y, final int z, final ChestType chestType, final String subTypes)
        {
            this.x = x;
            this.y = y;
            this.z = z;
            
            this.chestType = chestType;
            this.subTypes = subTypes;
        }
        
        private Location loc;
        private void loadLocation(final World world)
        {
            loc = new Location(world, x, y, z);
        }
        
        public Location getLocation()
        {
            return loc;
        }
        
        public ChestType getType()
        {
            return chestType;
        }
        
        public Stream<String> subTypeStream()
        {
            return Stream.of(subTypes.split(","));
        }
    }
}

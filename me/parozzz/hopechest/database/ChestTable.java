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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import me.parozzz.hopechest.chest.AbstractChest;
import me.parozzz.hopechest.chest.ChestType;
import me.parozzz.hopechest.database.DatabaseManager;
import me.parozzz.hopechest.database.query.IQueryResult;
import me.parozzz.hopechest.database.query.QueryItem;
import me.parozzz.hopechest.database.query.MultipleQueryResult;
import me.parozzz.hopechest.database.query.SingleQueryResult;
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
            con.createStatement().execute("CREATE TABLE IF NOT EXISTS chests (world TEXT, x INTEGER, y INTEGER, z INTEGER, chunkX INTEGER, chunkZ INTEGER, type TEXT, subTypes TEXT, owner TEXT);");
        } catch (SQLException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }
    
    private final String ADD_CHEST = "INSERT INTO chests (world, x, y, z, chunkX, chunkZ, type, subTypes, owner) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?);";
    public void addChest(final AbstractChest chest)
    {
        Location loc = chest.getLocation();
        
        String world = loc.getWorld().getName();
        int x = loc.getBlockX();
        int y = loc.getBlockY();
        int z = loc.getBlockZ();
        
        String type = chest.getType().name();
        String subTypes = ((Stream<String>)chest.getSpecificTypes().stream().map(Objects::toString)).collect(Collectors.joining(","));
        String owner = chest.getOwner().toString();
        
        Bukkit.getScheduler().runTaskAsynchronously(databaseManager.getPlugin(), () -> 
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
                prepared.setString(9, owner);
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
        Bukkit.getScheduler().runTaskAsynchronously(databaseManager.getPlugin(), () -> 
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
        
        Bukkit.getScheduler().runTaskAsynchronously(databaseManager.getPlugin(), () -> 
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
    public void queryChunk(final Chunk chunk, final Consumer<IQueryResult> consumer)
    {
        World world = chunk.getWorld();
        
        String stringWorld = world.getName();
        int x = chunk.getX();
        int z = chunk.getZ();
        
        Bukkit.getScheduler().runTaskAsynchronously(databaseManager.getPlugin(), () -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(CHUNK_QUERY);
                prepared.setString(1, stringWorld);
                prepared.setInt(2, x);
                prepared.setInt(3, z);
                 
                SingleQueryResult result = new SingleQueryResult(world);
                
                ResultSet set = prepared.executeQuery();
                while(set.next())
                {
                    int qx = set.getInt("x");
                    int qy = set.getInt("y");
                    int qz = set.getInt("z");
                    ChestType chestType = ChestType.valueOf(set.getString("type"));
                    String subTypes = set.getString("subTypes");
                    UUID uuid = UUID.fromString(set.getString("owner"));
                    
                    result.addItem(new QueryItem(qx, qy, qz, chestType, subTypes, uuid));
                }
                
                Bukkit.getScheduler().runTask(databaseManager.getPlugin(), () -> consumer.accept(result));
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } 
        });
    }
    
    private final String PLAYER_QUERY = "SELECT * FROM chests WHERE owner = ?;";
    public void queryUUID(final UUID uuid, final Consumer<IQueryResult> consumer)
    {
        String uuidString = uuid.toString();
        Bukkit.getScheduler().runTaskAsynchronously(databaseManager.getPlugin(), () -> 
        {
            try (Connection con = databaseManager.getConnection()) {
                PreparedStatement prepared = con.prepareStatement(PLAYER_QUERY);
                prepared.setString(1, uuidString);
                 
                MultipleQueryResult result = new MultipleQueryResult();
                
                ResultSet set = prepared.executeQuery();
                while(set.next())
                {
                    int qx = set.getInt("x");
                    int qy = set.getInt("y");
                    int qz = set.getInt("z");
                    ChestType chestType = ChestType.valueOf(set.getString("type"));
                    String subTypes = set.getString("subTypes");
                    
                    result.addItem(set.getString("world"), new QueryItem(qx, qy, qz, chestType, subTypes, uuid));
                }
                
                Bukkit.getScheduler().runTask(databaseManager.getPlugin(), () -> consumer.accept(result));
            } catch (SQLException ex) {
                logger.log(Level.SEVERE, null, ex);
            } 
        });
    }
}

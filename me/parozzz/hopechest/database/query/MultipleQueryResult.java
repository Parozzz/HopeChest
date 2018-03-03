/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database.query;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class MultipleQueryResult implements IQueryResult
{
    private final Map<String, List<QueryItem>> queryItems;
    public MultipleQueryResult()
    {
        queryItems = new HashMap<>();
    }
    
    public void addItem(final String worldName, final QueryItem queryItem)
    {
        queryItems.computeIfAbsent(worldName, temp -> new LinkedList<>()).add(queryItem);
    }
    
    @Override
    public boolean isEmpty()
    {
        return queryItems.isEmpty();
    }
    
    @Override
    public void forEach(final Consumer<QueryItem> consumer)
    {
        queryItems.forEach((worldName, list) -> 
        {
            World world = Bukkit.getWorld(worldName);
            if(world == null)
            {
                return;
            }
            
            list.forEach(item -> 
            {
                item.setWorld(world);
                consumer.accept(item);
            });
        });
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database.query;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import org.bukkit.World;

/**
 *
 * @author Paros
 */
public class SingleQueryResult implements IQueryResult
{
    private final World world;
    private final List<QueryItem> queryItems;
    public SingleQueryResult(final World world)
    {
        this.world = world;
        queryItems = new LinkedList<>();
    }
    
    public void addItem(final QueryItem queryItem)
    {
        queryItems.add(queryItem);
    }

    @Override
    public boolean isEmpty() 
    {
        return queryItems.isEmpty();
    }
    
    @Override
    public void forEach(Consumer<QueryItem> consumer) 
    {
        queryItems.forEach(item -> 
        {
            item.setWorld(world);
            consumer.accept(item);
        });
    }
    
}

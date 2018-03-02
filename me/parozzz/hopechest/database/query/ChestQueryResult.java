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
public class ChestQueryResult 
{
    private final List<QueryItem> itemList;
    private final World world;
    public ChestQueryResult(final World world)
    {
        this.world = world;
        itemList = new LinkedList<>();
    }
    
    public void addItem(final QueryItem item)
    {
        itemList.add(item);
    }
    
    public boolean isEmpty()
    {
        return itemList.isEmpty();
    }
    
    public void forEach(final Consumer<QueryItem> consumer)
    {
        itemList.forEach(item -> 
        {
            item.loadLocation(world);
            consumer.accept(item);
        });
    }
}

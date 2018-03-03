/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.database.query;

import java.util.function.Consumer;

/**
 *
 * @author Paros
 */
public interface IQueryResult 
{
    public void forEach(final Consumer<QueryItem> consumer);
    public boolean isEmpty();
}

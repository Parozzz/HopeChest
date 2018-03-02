/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Paros
 */
public class NonEmptyList<A>
{
    private final List<A> list;
    public NonEmptyList(final A a)
    {
        list = new ArrayList<>();
        list.add(a);
    }
    
    public void add(final A a)
    {
        this.list.add(a);
    }
    
    public List<A> getList()
    {
        return Collections.unmodifiableList(list);
    }
}

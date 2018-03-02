/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.api;

import javax.annotation.Nullable;
import me.parozzz.hopechest.chest.AbstractChest;
import org.bukkit.Location;

/**
 *
 * @author Paros
 */
public interface HopeChestAPI 
{
    public boolean hasChestAt(final Location loc);
    public @Nullable AbstractChest getChestAt(final Location loc);
}

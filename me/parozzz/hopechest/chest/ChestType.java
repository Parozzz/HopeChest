/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest;

import java.util.function.Function;
import me.parozzz.hopechest.chest.crop.CropChest;
import me.parozzz.hopechest.chest.crop.CropType;
import me.parozzz.hopechest.chest.mob.MobChest;
import me.parozzz.reflex.utilities.EntityUtil.CreatureType;

/**
 *
 * @author Paros
 */
public enum ChestType 
{
    MOB(MobChest.class, CreatureType.class, CreatureType::valueOf, "mobchest"), 
    CROP(CropChest.class, CropType.class, CropType::valueOf, "cropchest"),
    EXPERIENCE(null, int.class, Integer::parseInt, "");
    
    private final Class<? extends AbstractChest> chestClass;
    private final Class<?> subTypeClass;
    private final Function<String, Object> stringConvert;
    private final String languageKey;
    private <T> ChestType(final Class<? extends AbstractChest> chestClass, final Class<T> subTypeClass, final Function<String, T> stringConvert, final String languageKey)
    {
        this.chestClass = chestClass;
        this.subTypeClass = subTypeClass;
        this.stringConvert = (Function<String, Object>)stringConvert;
        this.languageKey = languageKey;
    }
    
    public Class<? extends AbstractChest> getChestClass()
    {
        return chestClass;
    }
    
    public Class<?> getSubTypeClass()
    {
        return subTypeClass;
    }
    
    public Object convertString(final String str)
    {
        return stringConvert.apply(str);
    }
    
    public String getLanguageKey()
    {
        return languageKey;
    }
}

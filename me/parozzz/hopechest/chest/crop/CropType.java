/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package me.parozzz.hopechest.chest.crop;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import me.parozzz.reflex.MCVersion;
import org.bukkit.Material;

/**
 *
 * @author Paros
 */
public enum CropType {
    CACTUS(Material.CACTUS),
    SUGAR_CANE(Material.SUGAR_CANE_BLOCK),
    MELON(Material.MELON_BLOCK),
    PUMPKIN(Material.PUMPKIN),
    CROPS(Material.CROPS),
    COCOA(Material.COCOA),
    POTATO(Material.POTATO),
    CARROT(Material.CARROT),
    NETHER_WARTS(Material.NETHER_WARTS),
    BEETROOT_BLOCK(MCVersion.V1_9.isHigher() ? Material.BEETROOT_BLOCK : null);
    
    private final Material material;
    private CropType(final Material material)
    {
        this.material = material;
    }
    
    public Material getMaterial()
    {
        return material;
    }
    
    private static final Map<Material, CropType> byMaterialMap = Stream.of(CropType.values()).collect(Collectors.toMap(type -> type.getMaterial(), Function.identity()));
    public static @Nullable CropType getByMaterial(final Material m)
    {
        return byMaterialMap.get(m);
    }
}

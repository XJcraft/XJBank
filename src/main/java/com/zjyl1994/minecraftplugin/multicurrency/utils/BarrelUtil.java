/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author zjyl1994
 */
public class BarrelUtil {

    public static List<Location> searchBarrel(Block baseBlock) {
        return searchBarrel(baseBlock, 2);
    }

    public static List<Location> searchBarrel(Block baseBlock, int radius) {
        // 查找最近的纸桶
        List<Location> paperBarrel = new ArrayList<>();
        for (int x = -(radius); x <= radius; x++) {
            for (int y = -(radius); y <= radius; y++) {
                for (int z = -(radius); z <= radius; z++) {
                    Block relative = baseBlock.getRelative(x, y, z);
                    if (relative.getType() == Material.BARREL) {
                        paperBarrel.add(relative.getLocation());
                    }
                }
            }
        }
        return paperBarrel;
    }

    public static Optional<Location> checkBarrelListItemStack(List<Location> loclist, ItemStack is) {
        for (Location l : loclist) {
            Block b = l.getBlock();
            if (b.getType() == Material.BARREL) {
                Barrel bar = (Barrel) b.getState();
                if (bar.getInventory().contains(is.getType(),is.getAmount())) {
                    return Optional.of(l);
                }
            }
        }
        return Optional.empty();
    }
}

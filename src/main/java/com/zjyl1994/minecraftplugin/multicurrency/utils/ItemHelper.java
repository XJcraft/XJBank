/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 *
 * @author zjyl1994
 */
public class ItemHelper {

    // 把 itemstack 强行塞给用户，塞不下就喷地上
    public static void givePlayerItemStack(Player p, ItemStack is) {
        // 检查有没有空间
        PlayerInventory inventory = p.getInventory();
        int emptySlot = inventory.firstEmpty();
        if (emptySlot != -1) {// 身上有空间
            inventory.setItem(emptySlot, is);
        } else {// 喷到地上
            p.getWorld().dropItemNaturally(p.getLocation(), is);
        }
    }
    // 检查用户背包里的东西够不够
    public static boolean checkPlayerItemStack(Player p, ItemStack is){
        PlayerInventory inv = p.getInventory();
        return inv.contains(is.getType(), is.getAmount());
    }
    // 从用户手里扣走一定数量的东西
    public static void removePlayerItemStack(Player p, ItemStack is){
        PlayerInventory inv = p.getInventory();
        inv.removeItem(is);
    }
}

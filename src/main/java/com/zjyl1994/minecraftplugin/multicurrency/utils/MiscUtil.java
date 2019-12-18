/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 *
 * @author zjyl1994
 */
public class MiscUtil {
    public static String getPlayerLocationString(Player p){
        Location l = p.getLocation();
        StringBuilder sb = new StringBuilder();
        sb.append("[(");
        sb.append(l.getBlockX());
        sb.append(",");
        sb.append(l.getBlockY());
        sb.append(",");
        sb.append(l.getBlockZ());
        sb.append(")@");
        sb.append(l.getWorld().getName());
        sb.append("]");
        return sb.toString();
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Directional;

/**
 *
 * @author reezhu
 */
public class SignUtil {
    /**
     * 判断一个材料是否是告示牌(插在地上的、或物品)
     *
     * @param material 被判断的材料
     * @return 这个材料是否是告示牌
     */
    public static boolean isSign(Material material) {
        return
                material == Material.ACACIA_SIGN ||
                        material == Material.BIRCH_SIGN ||
                        material == Material.DARK_OAK_SIGN ||
                        material == Material.JUNGLE_SIGN ||
                        material == Material.OAK_SIGN ||
                        material == Material.SPRUCE_SIGN;
    }

    /**
     * 判断一个材料是否是告示牌(放在墙上的)
     *
     * @param material 被判断的材料
     * @return 这个材料是否是告示牌
     */
    public static boolean isWallSign(Material material) {
        return
                material == Material.ACACIA_WALL_SIGN ||
                        material == Material.BIRCH_WALL_SIGN ||
                        material == Material.DARK_OAK_WALL_SIGN ||
                        material == Material.JUNGLE_WALL_SIGN ||
                        material == Material.OAK_WALL_SIGN ||
                        material == Material.SPRUCE_WALL_SIGN;
    }

    public static Block getSignDep(Block sign) {
        if (!isWallSign(sign.getType()))
            return null;
        BlockFace signFace = ((Directional) sign.getBlockData()).getFacing();
        return sign.getRelative(signFace.getOppositeFace());
    }
}

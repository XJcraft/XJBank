/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency;

import com.zjyl1994.minecraftplugin.multicurrency.command.AccountCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CheckCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CurrencyCMD;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * 操作侦听器
 *
 * @author zjyl1994
 */
public class MultiCurrencyEventListener implements Listener {

    private final MultiCurrencyPlugin plugin;
    private final CurrencyCMD currencyInstance;
    private final CheckCMD checkInstance;
    private final AccountCMD accountInstance;

    public MultiCurrencyEventListener(MultiCurrencyPlugin plugin) {
        this.plugin = plugin;
        this.currencyInstance = CurrencyCMD.getInstance();
        this.checkInstance = CheckCMD.getInstance();
        this.accountInstance = AccountCMD.getInstance();
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) { // 对方块右键
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                // 银行相关牌子
                if(ChatColor.stripColor(sign.getLine(0).trim()).equalsIgnoreCase("[ATM]")){
                    // 此处处理牌子
                    switch (ChatColor.stripColor(sign.getLine(1).trim())) {
                        case "支票兑现":
                            CheckCMD.getInstance().cashBlukCheck(p);
                            break;
                    }
                }
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency;

import com.zjyl1994.minecraftplugin.multicurrency.command.AccountCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CheckCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CurrencyCMD;
import com.zjyl1994.minecraftplugin.multicurrency.services.ATMService;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

/**
 * 操作侦听器
 *
 * @author zjyl1994
 */
public class MultiCurrencyEventListener implements Listener {

    private final MultiCurrencyPlugin plugin;

    public MultiCurrencyEventListener(MultiCurrencyPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerUse(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) { // 对方块右键
            if (event.getClickedBlock().getState() instanceof Sign) {
                Sign sign = (Sign) event.getClickedBlock().getState();
                // 银行相关牌子
                if(ChatColor.stripColor(sign.getLine(0).trim()).equalsIgnoreCase("[ATM]")){
                    // 此处进入对话
                    Location signLocation = sign.getBlock().getLocation();
                    ATMService atmInstance = new ATMService(plugin,signLocation);
                    atmInstance.Start(p);
                }
            }
        }
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.services.BankService;
import com.zjyl1994.minecraftplugin.multicurrency.services.CheckService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.CheckUtil;
import com.zjyl1994.minecraftplugin.multicurrency.utils.CurrencyEntity;
import com.zjyl1994.minecraftplugin.multicurrency.utils.ItemHelper;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 支票操作指令
 * @author zjyl1994
 */
public class CheckCMD {
    
    private CheckCMD() {}
    private static class SingletonHolder {
        private static final CheckCMD INSTANCE = new CheckCMD();
    }
    public static CheckCMD getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
    // 产生一张支票
    public void makeCheck(Player p, String currencyCode, String amount) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                String upperCode = currencyCode.toUpperCase();
                BigDecimal roundAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
                OperateResult transferResult = CheckService.makeCheck(p.getName(), upperCode, roundAmount);
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (transferResult.getSuccess()) {
                            // 在玩家手里生成支票
                            ItemStack is = CheckUtil.getCheck(new CurrencyEntity(upperCode,roundAmount), p.getName());
                            ItemHelper.givePlayerItemStack(p, is);
                        } else {
                            p.sendMessage(transferResult.getReason());
                        }
                    }
                });
            }
        });
    }
    
    
}

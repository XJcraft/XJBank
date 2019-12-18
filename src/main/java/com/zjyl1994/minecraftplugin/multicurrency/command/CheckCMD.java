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
import com.zjyl1994.minecraftplugin.multicurrency.utils.MiscUtil;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * 支票操作指令
 *
 * @author zjyl1994
 */
public class CheckCMD {

    private CheckCMD() {
    }

    private static class SingletonHolder {

        private static final CheckCMD INSTANCE = new CheckCMD();
    }

    public static CheckCMD getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 产生一张支票
    public void makeCheck(Player p, String currencyCode, String amount) {
        if(!ItemHelper.checkPlayerItemStack(p, new ItemStack(Material.PAPER,4))){
            p.sendMessage("获取一本支票需要4张纸");
            return;
        }
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
                            // 拿走4张纸
                            ItemHelper.removePlayerItemStack(p, new ItemStack(Material.PAPER,4));
                            // 在玩家手里生成支票
                            ItemStack is = CheckUtil.getCheck(new CurrencyEntity(upperCode, roundAmount), p.getName());
                            ItemHelper.givePlayerItemStack(p, is);
                        } else {
                            p.sendMessage(transferResult.getReason());
                        }
                    }
                });
            }
        });
    }

    // 兑付玩家手上的支票
    public void cashCheck(Player p) {
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();
        Optional<CurrencyEntity> checkValue = CheckUtil.getValue(itemInMainHand);
        if (checkValue.isEmpty()) {
            p.sendMessage("请手持有效支票再试");
        } else {
            CurrencyEntity ce = checkValue.get();
            // 获得玩家位置信息
            String remark = MiscUtil.getPlayerLocationString(p);
            Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
                @Override
                public void run() {
                    String currencyCode = ce.getCurrencyCode().toUpperCase();
                    BigDecimal amount = ce.getAmount().setScale(4, RoundingMode.DOWN);
                    OperateResult transferResult = CheckService.cashCheck(p.getName(), currencyCode, amount, remark);
                    Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            if (transferResult.getSuccess()) {
                                // 在玩家手里抢走已经兑付的支票
                                p.getInventory().remove(itemInMainHand);
                                // 还给用户4张纸
                                ItemStack paperIS = new ItemStack(Material.PAPER);
                                paperIS.setAmount(4);
                                ItemHelper.givePlayerItemStack(p, paperIS);
                                p.sendMessage("支票" + currencyCode + amount.toString() + "已入账");
                            } else {
                                p.sendMessage(transferResult.getReason());
                            }
                        }
                    });
                }
            });
        }
    }
}

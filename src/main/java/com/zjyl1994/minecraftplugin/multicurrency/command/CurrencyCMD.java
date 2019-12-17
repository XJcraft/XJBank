/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.services.CurrencyService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 货币操作指令
 *
 * @author zjyl1994
 */
public class CurrencyCMD {

    // 新增货币 /bank currnecy new [货币代码] [货币名称]
    // 增发货币 /bank currency incr [货币代码] [增发货币数量]
    // 减少货币 /bank currency decr [货币代码] [减少货币数量]
    // 重命名货币 /bank currency rename [货币代码] [新货币名称]
    private CurrencyCMD() {
    }

    private static class SingletonHolder {

        private static final CurrencyCMD INSTANCE = new CurrencyCMD();
    }

    public static CurrencyCMD getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void newCommand(Player p, String currencyCode, String currencyName) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                OperateResult newCurrency = CurrencyService.newCurrency(currencyCode, currencyName, p.getName());
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (newCurrency.getSuccess()) {
                            p.sendMessage(currencyName + "新建完成，货币代码" + currencyCode.toUpperCase());
                        } else {
                            p.sendMessage(newCurrency.getReason());
                        }
                    }
                });
            }
        });
    }

    public void incrCommand(Player p, String currencyCode, String amount) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                BigDecimal bdAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
                OperateResult reserveIncr = CurrencyService.reserveIncr(currencyCode, bdAmount, p.getName());
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (reserveIncr.getSuccess()) {
                            p.sendMessage(currencyCode.toUpperCase() + "成功增发储备金" + bdAmount.toString());
                        } else {
                            p.sendMessage(reserveIncr.getReason());
                        }
                    }
                });
            }
        });
    }

    public void decrCommand(Player p, String currencyCode, String amount) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                BigDecimal bdAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
                OperateResult reserveDecr = CurrencyService.reserveDecr(currencyCode, bdAmount, p.getName());
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (reserveDecr.getSuccess()) {
                            p.sendMessage(currencyCode.toUpperCase() + "成功回收储备金" + bdAmount.toString());
                        } else {
                            p.sendMessage(reserveDecr.getReason());
                        }
                    }
                });
            }
        });
    }

    public void renameCommand(Player p, String currencyCode, String currencyName) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                OperateResult renameCurrency = CurrencyService.renameCurrency(currencyCode, currencyName, p.getName());
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (renameCurrency.getSuccess()) {
                            p.sendMessage(currencyCode.toUpperCase() + "重命名成功");
                        } else {
                            p.sendMessage(renameCurrency.getReason());
                        }
                    }
                });
            }
        });
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.services.BankService;
import com.zjyl1994.minecraftplugin.multicurrency.services.CurrencyService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 存款账户指令
 *
 * @author zjyl1994
 */
public class AccountCMD {

    private AccountCMD() {
    }

    private static class SingletonHolder {

        private static final AccountCMD INSTANCE = new AccountCMD();
    }

    public static AccountCMD getInstance() {
        return SingletonHolder.INSTANCE;
    }

    // 转账
    public void transferToAccount(Player p, String payTo, String currencyCode, String amount) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                BigDecimal roundAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
                OperateResult transferResult = BankService.transferTo(p.getName(), payTo, currencyCode.toUpperCase(), roundAmount, TxTypeEnum.ELECTRONIC_TRANSFER_OUT, "");
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (transferResult.getSuccess()) {
                            p.sendMessage("成功向" + payTo + "转账" + currencyCode.toUpperCase() + roundAmount.toString());
                        } else {
                            p.sendMessage(transferResult.getReason());
                        }
                    }
                });
            }
        });
    }

    // 从储备金账户转出
    public void reserveTransferOut(Player p, String currencyCode, String amount) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                String upperCurrencyCode = currencyCode.toUpperCase();
                BigDecimal roundAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
                OperateResult transferResult;
                if(CurrencyService.isCurrencyOwner(upperCurrencyCode, p.getName())){
                    transferResult = BankService.transferTo("$" + upperCurrencyCode, p.getName(), upperCurrencyCode, roundAmount, TxTypeEnum.ELECTRONIC_TRANSFER_OUT, "");
                }else{
                    transferResult = new OperateResult(false,"您不是该货币的发行人，无权操作储备金帐户");
                } 
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (transferResult.getSuccess()) {
                            p.sendMessage("成功从" + upperCurrencyCode + "准备金账户转出" + roundAmount.toString());
                        } else {
                            p.sendMessage(transferResult.getReason());
                        }
                    }
                });
            }
        });
    }
}

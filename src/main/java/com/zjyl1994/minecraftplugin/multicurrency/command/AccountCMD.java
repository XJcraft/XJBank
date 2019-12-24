/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.services.BankService;
import com.zjyl1994.minecraftplugin.multicurrency.services.CurrencyService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.AccountBalanceEntity;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxLogEntity;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
                if (CurrencyService.isCurrencyOwner(upperCurrencyCode, p.getName())) {
                    transferResult = BankService.transferTo("$" + upperCurrencyCode, p.getName(), upperCurrencyCode, roundAmount, TxTypeEnum.ELECTRONIC_TRANSFER_OUT, "");
                } else {
                    transferResult = new OperateResult(false, "您不是该货币的发行人，无权操作储备金帐户");
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

    // 查询账户余额
    public void getAccountInfo(Player p) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                OperateResult accountInfo = BankService.getAccountInfo(p.getName());
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        if (accountInfo.getSuccess()) {
                            var data = (ArrayList<AccountBalanceEntity>) (accountInfo.getData());
                            StringBuilder resultString = new StringBuilder();

                            resultString.append(ChatColor.GOLD).append("您的账户余额为\n").append(ChatColor.RESET);
                            data.forEach(x -> {
                                resultString.append(x.getCurrencyName());
                                resultString.append(" (");
                                resultString.append(x.getCurrencyCode());
                                resultString.append(") ");
                                resultString.append(x.getBalance().setScale(4, RoundingMode.DOWN).toString());
                                resultString.append("\n");
                            });
                            p.sendMessage(resultString.toString());
                        } else {
                            p.sendMessage(accountInfo.getReason());
                        }
                    }
                });
            }
        });
    }

    // 查询账户交易日志
    public void getAccountTradeLog(Player p, Integer pageNo) {
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                OperateResult accountLogPageNo = BankService.getAccountTradeLogTotalPage(p.getName());
                if (accountLogPageNo.getSuccess()) {
                    int totalPage = (int) accountLogPageNo.getData();
                    if (pageNo > totalPage) {
                        Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                p.sendMessage("最多只有" + Integer.toString(totalPage) + "页");
                            }
                        });
                    } else {
                        OperateResult accountLog = BankService.getAccountTradeLog(p.getName(), pageNo);
                        Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                            @Override
                            public void run() {
                                if (accountLog.getSuccess()) {
                                    var data = (ArrayList<TxLogEntity>) (accountLog.getData());
                                    long nowTime = System.currentTimeMillis();
                                    DecimalFormat decimalFormat = new DecimalFormat("0.00");
                                    StringBuilder resultString = new StringBuilder();
                                    resultString.append(ChatColor.GOLD).append(ChatColor.BOLD).append("===== 对账单 ").append(pageNo).append("/").append(totalPage).append("=====\n").append(ChatColor.RESET);
                                    data.forEach(x -> {
                                        long txTime = x.getTxTime().getTime();
                                        String hourAgo = decimalFormat.format((nowTime - txTime) / 1000 / 3600);
                                        resultString.append(hourAgo);
                                        resultString.append("小时前 ");
                                        switch (x.getTxType()) {
                                            case CURRENCY_RESERVE_INCREASE:
                                                resultString.append("准备金增发");
                                                break;
                                            case CURRENCY_RESERVE_DECREASE:
                                                resultString.append("准备金回收");
                                                break;
                                            case ELECTRONIC_TRANSFER_IN:
                                                resultString.append(x.getTxUsername());
                                                resultString.append("向你转账");
                                                break;
                                            case ELECTRONIC_TRANSFER_OUT:
                                                resultString.append("向");
                                                resultString.append(x.getTxUsername());
                                                resultString.append("转账");
                                                break;
                                            case CHECK_TRANSFER_OUT:
                                                resultString.append("支票开出");
                                                break;
                                            case CHECK_TRANSFER_IN:
                                                resultString.append("支票入账");
                                                break;
                                            default:
                                                resultString.append("未知行为");
                                        }
                                        resultString.append(x.getCurrencyCode());
                                        resultString.append(x.getAmount().setScale(4, RoundingMode.DOWN).toString());
                                        String remark = x.getRemark();
                                        if (!remark.isBlank()) {
                                            resultString.append(" 备注:");
                                            resultString.append(remark);
                                        }
                                        resultString.append("\n");
                                    });
                                    resultString.append(ChatColor.GRAY);
                                    resultString.append("== 使用 /bank log [页码] 查看其它页==");
                                    p.sendMessage(resultString.toString());
                                } else {
                                    p.sendMessage(accountLog.getReason());
                                }
                            }
                        });
                    }
                } else {
                    Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), new Runnable() {
                        @Override
                        public void run() {
                            p.sendMessage(accountLogPageNo.getReason());
                        }
                    });
                }
            }
        });
    }
}

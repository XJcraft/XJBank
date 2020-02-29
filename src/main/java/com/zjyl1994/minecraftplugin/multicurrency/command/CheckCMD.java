/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.services.CheckService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bukkit.Location;
import org.bukkit.block.Barrel;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;

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
        if (!ItemHelper.checkPlayerItemStack(p, new ItemStack(Material.PAPER, 4))) {
            p.sendMessage("获取一本支票需要4张纸");
            return;
        }
        BigDecimal roundAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
        if (roundAmount.compareTo(BigDecimal.ZERO) <= 0) { //操作数必须大于0
            p.sendMessage("金额必须大于0");
            return;
        }
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), () -> {
            String upperCode = currencyCode.toUpperCase();
            OperateResult transferResult = CheckService.makeCheck(p.getName(), upperCode, roundAmount);
            Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), () -> {
                if (transferResult.getSuccess()) {
                    // 拿走4张纸
                    ItemHelper.removePlayerItemStack(p, new ItemStack(Material.PAPER, 4));
                    // 在玩家手里生成支票
                    ItemStack is = CheckUtil.getCheck(new CurrencyEntity(upperCode, roundAmount), p.getName());
                    ItemHelper.givePlayerItemStack(p, is);
                } else {
                    p.sendMessage(transferResult.getReason());
                }
            });
        });
    }

    // 产生一张支票(ATM机具使用)
    public void makeCheckOnATM(Player p, Block atmDepend, String currencyCode, String amount) {
        BigDecimal roundAmount = new BigDecimal(amount).setScale(4, RoundingMode.DOWN);
        if (roundAmount.compareTo(BigDecimal.ZERO) <= 0) { //操作数必须大于0
            p.sendMessage("金额必须大于0");
            return;
        }
        Optional<Location> paperBarrel = Optional.empty();
        if (atmDepend != null) {
            List<Location> barrelList = BarrelUtil.searchBarrel(atmDepend);
            if (!barrelList.isEmpty()) {
                paperBarrel = BarrelUtil.checkBarrelListItemStack(barrelList, new ItemStack(Material.PAPER, 4));
            }
        }
        if (paperBarrel.isEmpty() && !ItemHelper.checkPlayerItemStack(p, new ItemStack(Material.PAPER, 4))) {
            p.sendMessage("获取一本支票需要4张纸");
            return;
        }
        final Optional<Location> paperBarrelFinal = paperBarrel;
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), () -> {
            String upperCode = currencyCode.toUpperCase();
            OperateResult transferResult = CheckService.makeCheck(p.getName(), upperCode, roundAmount);
            Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), () -> {
                if (transferResult.getSuccess()) {
                    // 拿走4张纸
                    if (paperBarrelFinal.isPresent()) {
                        Location barrelLocation = paperBarrelFinal.get();
                        Barrel barrel = (Barrel)barrelLocation.getBlock().getState();
                        barrel.getInventory().removeItem(new ItemStack(Material.PAPER, 4));
                    } else {
                        ItemHelper.removePlayerItemStack(p, new ItemStack(Material.PAPER, 4));
                    }
                    // 在玩家手里生成支票
                    ItemStack is = CheckUtil.getCheck(new CurrencyEntity(upperCode, roundAmount), p.getName());
                    ItemHelper.givePlayerItemStack(p, is);
                } else {
                    p.sendMessage(transferResult.getReason());
                }
            });
        });
    }

    // 兑付玩家手上的支票
    public void cashCheck(Player p, String forceCurrencyCode) {
        ItemStack itemInMainHand = p.getInventory().getItemInMainHand();
        Optional<CurrencyEntity> checkValue = CheckUtil.getValue(itemInMainHand);
        if (checkValue.isEmpty()) {
            p.sendMessage("请手持有效支票再试");
        } else {
            CurrencyEntity ce = checkValue.get();
            if (!forceCurrencyCode.isBlank() && !ce.getCurrencyCode().equalsIgnoreCase(forceCurrencyCode)) {
                p.sendMessage("您手持的不是" + forceCurrencyCode.toUpperCase() + "支票");
                return;
            }
            Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), () -> {
                String currencyCode = ce.getCurrencyCode().toUpperCase();
                BigDecimal amount = ce.getAmount().setScale(4, RoundingMode.DOWN);
                OperateResult transferResult = CheckService.cashCheck(p.getName(), currencyCode, amount, ce.getRemark() + "的支票");
                Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), () -> {
                    if (transferResult.getSuccess()) {
                        // 在玩家手里抢走已经兑付的支票
                        p.getInventory().remove(itemInMainHand);
                        p.sendMessage("支票" + currencyCode + amount.toString() + "已入账");
                    } else {
                        p.sendMessage(transferResult.getReason());
                    }
                });
            });
        }
    }

    // 批量签发支票
    public void makeBlukCheck(Player p, String currencyCode, String currencyAmount, String checkAmount) {
        String upperCode = currencyCode.toUpperCase();
        Integer iCheckAmount = MiscUtil.getIntegerFromString(checkAmount);
        if (iCheckAmount <= 0) {
            p.sendMessage("支票数量必须大于0");
            return;
        }
        int iPaperAmount = iCheckAmount * 4;
        BigDecimal bdCurrencyAmount = new BigDecimal(currencyAmount).setScale(4, RoundingMode.DOWN);
        if (bdCurrencyAmount.compareTo(BigDecimal.ZERO) <= 0) { //操作数必须大于0
            p.sendMessage("金额必须大于0");
            return;
        }
        BigDecimal bdTotalCurrencyAmount = bdCurrencyAmount.multiply(new BigDecimal(iCheckAmount));
        // 检查纸够不够
        if (!ItemHelper.checkPlayerItemStack(p, new ItemStack(Material.PAPER, iPaperAmount))) {
            p.sendMessage("获取" + iCheckAmount + "本支票需要" + iPaperAmount + "张纸");
            return;
        }
        // 异步转账
        Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), () -> {
            OperateResult transferResult = CheckService.makeCheck(p.getName(), upperCode, bdTotalCurrencyAmount);
            Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), () -> {
                if (transferResult.getSuccess()) {
                    // 拿走对应的纸
                    ItemHelper.removePlayerItemStack(p, new ItemStack(Material.PAPER, iPaperAmount));
                    // 塞给玩家
                    for (int i = 0; i < iCheckAmount; i++) {
                        ItemStack is = CheckUtil.getCheck(new CurrencyEntity(upperCode, bdCurrencyAmount), p.getName());
                        ItemHelper.givePlayerItemStack(p, is);
                    }
                } else {
                    p.sendMessage(transferResult.getReason());
                }
            });
        });
    }

    // 批量兑付支票
    public void cashBlukCheck(Player p) {
        PlayerInventory inventory = p.getInventory();
        int invSize = inventory.getSize();
        for (int i = 0; i < invSize; i++) {
            ItemStack checkItem = inventory.getItem(i);
            if (checkItem != null) {
                Optional<CurrencyEntity> checkValue = CheckUtil.getValue(checkItem);
                if (checkValue.isPresent()) { // 有效支票触发异步转账
                    Bukkit.getScheduler().runTaskAsynchronously(MultiCurrencyPlugin.getInstance(), () -> {
                        CurrencyEntity ce = checkValue.get();
                        OperateResult transferResult = CheckService.cashCheck(p.getName(), ce.getCurrencyCode(), ce.getAmount(), ce.getRemark() + "的支票");
                        Bukkit.getScheduler().runTask(MultiCurrencyPlugin.getInstance(), () -> {
                            if (transferResult.getSuccess()) {
                                // 在玩家手里抢走已经兑付的支票
                                p.getInventory().remove(checkItem);
                                p.sendMessage("支票" + ce.getCurrencyCode() + ce.getAmount().toString() + "已入账");
                            } else {
                                p.sendMessage(transferResult.getReason());
                            }
                        });
                    });
                }
            }
        }
    }
}

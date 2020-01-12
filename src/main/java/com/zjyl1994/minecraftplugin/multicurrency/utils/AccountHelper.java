/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

/**
 * 账户定义
 *
 * @author zjyl1994
 */
public class AccountHelper {
    public static final String MONETARY_AUTHORITY = "$XJ_MONETARY_AUTHORITY"; // 金管局帐号(系统)
    public static final String CHECK_CENTER = "$CHECK_CENTER"; // 支票中心帐号

    // 检查是否是无限金额账号
    public static Boolean isUnlimitedAccount(String accountName) {
        return accountName.equalsIgnoreCase(MONETARY_AUTHORITY) || accountName.equalsIgnoreCase(CHECK_CENTER);
    }
}

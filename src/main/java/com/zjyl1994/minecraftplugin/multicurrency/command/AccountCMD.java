/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

/**
 * 存款账户指令
 * @author zjyl1994
 */
public class AccountCMD {
    
    private AccountCMD() {}
    private static class SingletonHolder {
        private static final AccountCMD INSTANCE = new AccountCMD();
    }
    public static AccountCMD getInstance() {
        return SingletonHolder.INSTANCE;
    }
    
}

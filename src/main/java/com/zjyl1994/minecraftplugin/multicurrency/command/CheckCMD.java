/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.command;

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
    
}

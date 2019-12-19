/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import java.math.BigDecimal;

/**
 *
 * @author zjyl1994
 */
public class AccountBalanceEntity {
    private String currencyCode;
    private String currencyName;
    private BigDecimal balance;

    public AccountBalanceEntity(String currencyCode, String currencyName, BigDecimal balance) {
        this.currencyCode = currencyCode;
        this.currencyName = currencyName;
        this.balance = balance;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public String getCurrencyName() {
        return currencyName;
    }

    public BigDecimal getBalance() {
        return balance;
    }
    
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import java.math.BigDecimal;

/**
 * 描述某一种货币的实体
 * @author zjyl1994
 */
public class CurrencyEntity {
    String currencyCode;
    BigDecimal amount;

    public CurrencyEntity(String currencyCode, BigDecimal amount) {
        this.currencyCode = currencyCode;
        this.amount = amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public BigDecimal getAmount() {
        return amount;
    }
    
}

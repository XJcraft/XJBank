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
public class CurrencyInfoEntity {
    private String name;    // 货币名称
    private String owner;  // 货币所有者
    private BigDecimal total;   // 货币发行总量
    private BigDecimal reserve; // 储备金余额
    private BigDecimal balanceSum;  // 存款总余额

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public BigDecimal getReserve() {
        return reserve;
    }

    public void setReserve(BigDecimal reserve) {
        this.reserve = reserve;
    }

    public BigDecimal getBalanceSum() {
        return balanceSum;
    }

    public void setBalanceSum(BigDecimal balanceSum) {
        this.balanceSum = balanceSum;
    }
    
}

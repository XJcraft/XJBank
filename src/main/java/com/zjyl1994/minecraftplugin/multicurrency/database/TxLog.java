/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.database;

import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 交易记录
 * @author zjyl1994
 */
public class TxLog {
    Integer Id;
    String username; // 交易人
    String txUsername; // 交易对方
    Timestamp txTime; // 交易时间
    TxTypeEnum txType; // 交易类别
    String currencyCode; // 交易币种
    BigDecimal amount; // 交易金额
    String remark; // 交易备注

    public Integer getId() {
        return Id;
    }

    public void setId(Integer Id) {
        this.Id = Id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTxUsername() {
        return txUsername;
    }

    public void setTxUsername(String txUsername) {
        this.txUsername = txUsername;
    }

    public Timestamp getTxTime() {
        return txTime;
    }

    public void setTxTime(Timestamp txTime) {
        this.txTime = txTime;
    }

    public TxTypeEnum getTxType() {
        return txType;
    }

    public void setTxType(TxTypeEnum txType) {
        this.txType = txType;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
    
}
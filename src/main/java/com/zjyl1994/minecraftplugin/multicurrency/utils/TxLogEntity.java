/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * @author zjyl1994
 */
public class TxLogEntity {
    String txUsername; // 交易对方
    Timestamp txTime; // 交易时间
    TxTypeEnum txType; // 交易类别
    String currencyCode; // 交易币种
    BigDecimal amount; // 交易金额
    String remark; // 交易备注

    public TxLogEntity() {
    }

    public TxLogEntity(String txUsername, Timestamp txTime, TxTypeEnum txType, String currencyCode, BigDecimal amount, String remark) {
        this.txUsername = txUsername;
        this.txTime = txTime;
        this.txType = txType;
        this.currencyCode = currencyCode;
        this.amount = amount;
        this.remark = remark;
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

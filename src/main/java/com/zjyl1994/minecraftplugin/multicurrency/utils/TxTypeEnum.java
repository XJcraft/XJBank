/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

/**
 * 交易类别
 *
 * @author zjyl1994
 */
public enum TxTypeEnum {
    NULL_OPERATE, // 空操作
    CURRENCY_RESERVE_INCREASE, // 货币储备新增
    CURRENCY_RESERVE_DECREASE, // 货币储备减少
    ELECTRONIC_TRANSFER_IN, // 电子渠道转入
    ELECTRONIC_TRANSFER_OUT, // 电子渠道转出
    SHOP_TRADE_IN, // 商店交易收入
    SHOP_TRADE_OUT, // 商店交易扣款
    CURRENCY_EXCHANGE_IN, // 货币兑换入账
    CURRENCY_EXCHANGE_OUT, // 货币兑换扣款
    CHECK_TRANSFER_OUT, // 实体支票开出
    CHECK_TRANSFER_IN, // 实体支票入账
}
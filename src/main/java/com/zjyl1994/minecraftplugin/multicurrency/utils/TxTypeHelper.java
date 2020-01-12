/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

/**
 * 交易类型工具
 *
 * @author zjyl1994
 */
public class TxTypeHelper {
    public static TxTypeEnum negate(TxTypeEnum tte) {
        switch (tte) {
            case CURRENCY_RESERVE_INCREASE:
                return TxTypeEnum.CURRENCY_RESERVE_DECREASE;
            case CURRENCY_RESERVE_DECREASE:
                return TxTypeEnum.CURRENCY_RESERVE_INCREASE;
            case ELECTRONIC_TRANSFER_IN:
                return TxTypeEnum.ELECTRONIC_TRANSFER_OUT;
            case ELECTRONIC_TRANSFER_OUT:
                return TxTypeEnum.ELECTRONIC_TRANSFER_IN;
            case SHOP_TRADE_IN:
                return TxTypeEnum.SHOP_TRADE_OUT;
            case SHOP_TRADE_OUT:
                return TxTypeEnum.SHOP_TRADE_IN;
            case CURRENCY_EXCHANGE_IN:
                return TxTypeEnum.CURRENCY_EXCHANGE_OUT;
            case CURRENCY_EXCHANGE_OUT:
                return TxTypeEnum.CURRENCY_EXCHANGE_IN;
            case CHECK_TRANSFER_OUT:
                return TxTypeEnum.CHECK_TRANSFER_IN;
            case CHECK_TRANSFER_IN:
                return TxTypeEnum.CHECK_TRANSFER_OUT;
            default:
                return TxTypeEnum.NULL_OPERATE;
        }
    }
}
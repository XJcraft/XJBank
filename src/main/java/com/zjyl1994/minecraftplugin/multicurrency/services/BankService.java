/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.services;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.utils.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;

/**
 * 银行业务逻辑
 *
 * @author zjyl1994
 */
public class BankService {

    private static final String SELECT_INFO = "SELECT mc_account.code,mc_currency.name,mc_account.balance FROM mc_account LEFT JOIN mc_currency ON mc_account.code = mc_currency.code WHERE mc_account.username =  ? AND mc_account.balance > 0";
    private static final String SELECT_BALANCE = "SELECT `balance` FROM `mc_account` WHERE `username` = ? AND `code` = ?";
    private static final String UPDATE_BALANCE = "INSERT INTO `mc_account` (`username`,`code`,`balance`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `balance` = `balance` + ?";
    private static final String INSERT_TX_LOG = "INSERT INTO mc_tx_log (username,tx_username,tx_time,tx_type,currency_code,amount,remark) VALUES (?,?,NOW(),?,?,?,?)";
    private static final String SELECT_TX_LOG = "SELECT * FROM mc_tx_log WHERE username = ? ORDER BY tx_time DESC LIMIT ?,5";
    private static final String SELECT_TX_LOG_TOTAL_COUNT = "SELECT COUNT(1) FROM mc_tx_log WHERE username = ?";
    private static final String SELECT_TX_LOG_SPECIFIC_CURRENCY = "SELECT * FROM mc_tx_log WHERE username = ? AND currency_code = ? ORDER BY tx_time DESC LIMIT ?,5";
    private static final String SELECT_TX_LOG_SPECIFIC_CURRENCY_TOTAL_COUNT = "SELECT COUNT(1) FROM mc_tx_log WHERE username = ? AND currency_code = ?";

    // 查询用户特定币种的余额
    public static OperateResult queryCurrencyBalance(String username, String currencyCode) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE)) {
            BigDecimal balance;
            try {
                selectBalance.setString(1, username);
                selectBalance.setString(2, currencyCode);
                ResultSet result = selectBalance.executeQuery();
                if (result.next()) {
                    balance = result.getBigDecimal("balance");
                } else { // 没有记录
                    balance = new BigDecimal(0);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", balance);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[queryCurrencyBalance SQLException]{0}", e.getMessage());
            return new OperateResult(false, "查询失败-数据库异常");
        }
    }

    // 转账功能
    // payer 付款人 payee 收款人 currencyCode 货币代码 amount 金额
    public static OperateResult transferTo(String payer, String payee, String currencyCode, BigDecimal amount, TxTypeEnum txType, String remark) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE);  PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE);  PreparedStatement insertLog = connection.prepareStatement(INSERT_TX_LOG)) {
            if (!AccountHelper.isUnlimitedAccount(payer)) {
                // 非无限账户需要检查付款人有没有足够的钱
                BigDecimal payerBalance; // 付款人余额
                try {
                    selectBalance.setString(1, payer);
                    selectBalance.setString(2, currencyCode);
                    ResultSet result = selectBalance.executeQuery();
                    if (result.next()) {
                        payerBalance = result.getBigDecimal("balance");
                    } else { // 没有记录
                        payerBalance = new BigDecimal(0);
                    }
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
                if (amount.compareTo(payerBalance) > 0) { // 待转账金额大于账户余额
                    return new OperateResult(false, "向" + payee + "转账失败,可用余额不足，" + payer + "当前余额" + payerBalance.setScale(4, RoundingMode.DOWN));
                }
                // 付款人扣款逻辑
                try {
                    updateBalance.setString(1, payer);
                    updateBalance.setString(2, currencyCode);
                    updateBalance.setBigDecimal(3, amount.negate());
                    updateBalance.setBigDecimal(4, amount.negate());
                    updateBalance.executeUpdate();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            try {
                // 操作日志
                insertLog.setString(1, payer);
                insertLog.setString(2, payee);
                insertLog.setInt(3, txType.ordinal());
                insertLog.setString(4, currencyCode);
                insertLog.setBigDecimal(5, amount);
                insertLog.setString(6, remark);
                insertLog.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            if (!AccountHelper.isUnlimitedAccount(payee)) {
                // 收款人非无限账户需要增加款项
                try {
                    updateBalance.setString(1, payee);
                    updateBalance.setString(2, currencyCode);
                    updateBalance.setBigDecimal(3, amount);
                    updateBalance.setBigDecimal(4, amount);
                    updateBalance.executeUpdate();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }
            try {
                // 操作日志
                insertLog.setString(1, payee);
                insertLog.setString(2, payer);
                insertLog.setInt(3, TxTypeHelper.negate(txType).ordinal());
                insertLog.setString(4, currencyCode);
                insertLog.setBigDecimal(5, amount);
                insertLog.setString(6, remark);
                insertLog.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
            return new OperateResult(true, "OK");
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[transferTo SQLException]{0}", e.getMessage());
            return new OperateResult(false, "转账失败-数据库异常");
        }
    }

    // 查询余额
    public static OperateResult getAccountInfo(String username) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectInfo = connection.prepareStatement(SELECT_INFO)) {
            ArrayList<AccountBalanceEntity> detail = new ArrayList<>();
            try {
                selectInfo.setString(1, username);
                ResultSet result = selectInfo.executeQuery();
                while (result.next()) {
                    String currencyCode = result.getString("code");
                    String currencyName = result.getString("name");
                    BigDecimal balance = result.getBigDecimal("balance");
                    detail.add(new AccountBalanceEntity(currencyCode, currencyName, balance));
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", detail);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[getAccountInfo SQLException]{0}", e.getMessage());
            return new OperateResult(false, "查询失败-数据库异常");
        }
    }

    // 查询交易日志
    public static OperateResult getAccountTradeLog(String username, Integer pageNo, String forceCurrencyCode) {
        String sql;
        if (forceCurrencyCode.isBlank()) {
            sql = SELECT_TX_LOG;
        } else {
            sql = SELECT_TX_LOG_SPECIFIC_CURRENCY;
        }
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectTxLog = connection.prepareStatement(sql)) {
            ArrayList<TxLogEntity> detail = new ArrayList<>();
            Integer pageOffset = (pageNo - 1) * 5;
            try {
                selectTxLog.setString(1, username);
                if (!forceCurrencyCode.isBlank()) {
                    selectTxLog.setString(2, forceCurrencyCode.toUpperCase());
                    selectTxLog.setInt(3, pageOffset);
                }else{
                    selectTxLog.setInt(2, pageOffset);
                }
                ResultSet result = selectTxLog.executeQuery();
                while (result.next()) {
                    String txUsername = result.getString("tx_username");
                    Timestamp txTime = result.getTimestamp("tx_time");
                    TxTypeEnum txType = TxTypeEnum.values()[result.getInt("tx_type")];
                    String currencyCode = result.getString("currency_code");
                    BigDecimal amount = result.getBigDecimal("amount");
                    String remark = result.getString("remark");
                    detail.add(new TxLogEntity(txUsername, txTime, txType, currencyCode, amount, remark));
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", detail);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[getAccountTradeLog SQLException]{0}", e.getMessage());
            return new OperateResult(false, "查询失败-数据库异常");
        }
    }

    // 查询交易日志对应的页数
    public static OperateResult getAccountTradeLogTotalPage(String username, String forceCurrencyCode) {
        String sql;
        if (forceCurrencyCode.isBlank()) {
            sql = SELECT_TX_LOG_TOTAL_COUNT;
        } else {
            sql = SELECT_TX_LOG_SPECIFIC_CURRENCY_TOTAL_COUNT;
        }
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectTxLog = connection.prepareStatement(sql)) {
            Integer totalCount;
            try {
                selectTxLog.setString(1, username);
                if (!forceCurrencyCode.isBlank()) {
                    selectTxLog.setString(2, forceCurrencyCode.toUpperCase());
                }
                ResultSet result = selectTxLog.executeQuery();
                if (result.next()) {
                    totalCount = result.getInt(1);
                } else { // 没有记录
                    totalCount = 0;
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", (totalCount + 4) / 5);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[getAccountTradeLogTotalPage SQLException]{0}", e.getMessage());
            return new OperateResult(false, "查询失败-数据库异常");
        }
    }
}

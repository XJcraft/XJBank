/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.services;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.utils.AccountHelper;
import com.zjyl1994.minecraftplugin.multicurrency.utils.CurrencyInfoEntity;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * @author zjyl1994
 */
public class CurrencyService {

    private static final String SELECT_CURRENCY = "SELECT * FROM mc_currency WHERE `code` = ?";
    private static final String INSERT_CURRNECY = "INSERT INTO mc_currency (`code`,`owner`,`name`) VALUES(?,?,?)";
    private static final String UPDATE_CURRENCY_NAME = "UPDATE mc_currency SET `name` = ? WHERE `code` = ?";
    private static final String UPDATE_BALANCE = "INSERT INTO `mc_account` (`username`,`code`,`balance`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `balance` = `balance` + ?";
    private static final String UPDATE_CURRENCY_TOTAL = "UPDATE mc_currency SET `total` = `total` + ? WHERE `code` = ?";
    private static final String INSERT_TX_LOG = "INSERT INTO mc_tx_log (username,tx_username,tx_time,tx_type,currency_code,amount,remark) VALUES (?,?,NOW(),?,?,?,?)";
    private static final String SELECT_CURRENCY_INFO = "SELECT * FROM (SELECT owner,name,total AS currencyTotal FROM mc_currency WHERE CODE = ?) AS a,\n" +
            "(SELECT balance AS reserveBalance FROM mc_account WHERE username = CONCAT(\"$\",?)) AS b,\n" +
            "(SELECT SUM(balance)AS accountBalanceSum FROM mc_account WHERE username != CONCAT(\"$\",?) AND CODE = ?) AS c;";

    // 检查是否货币持有人
    public static Boolean isCurrencyOwner(String currencyCode, String playerName) {
        try (
                Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement selectCurrency = connection.prepareStatement(SELECT_CURRENCY)) {
            String owner;
            try {
                selectCurrency.setString(1, currencyCode);
                ResultSet result = selectCurrency.executeQuery();
                if (result.next()) {
                    owner = result.getString("owner");
                } else {
                    owner = "";
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return playerName.equalsIgnoreCase(owner);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[isCurrencyOwner SQLException]{0}", e.getMessage());
            return false;
        }
    }

    // 新建货币
    public static OperateResult newCurrency(String currencyCode, String currencyName, String playerName) {
        if (!currencyCode.matches("^[A-Za-z]{3}$")) {
            return new OperateResult(false, "货币代码必须是三位字母");
        }
        currencyCode = currencyCode.toUpperCase();
        try (
                Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement newCurrencyStmt = connection.prepareStatement(INSERT_CURRNECY)) {
            try {
                newCurrencyStmt.setString(1, currencyCode);
                newCurrencyStmt.setString(2, playerName);
                newCurrencyStmt.setString(3, currencyName);
                newCurrencyStmt.executeUpdate();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                if (e.getErrorCode() == 1062) {
                    return new OperateResult(false, "此货币代码已被占用，请换一个~");
                } else {
                    throw e;
                }
            }
            return new OperateResult(true, "OK");
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[newCurrency SQLException]{0}", e.getMessage());
            return new OperateResult(false, "新建货币失败-数据库异常");
        }
    }

    // 重命名货币
    public static OperateResult renameCurrency(String currencyCode, String currencyName, String playerName) {
        if (isCurrencyOwner(currencyCode, playerName)) {
            try (
                    Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement renameCurrencyStmt = connection.prepareStatement(UPDATE_CURRENCY_NAME)) {
                try {
                    renameCurrencyStmt.setString(1, currencyName);
                    renameCurrencyStmt.setString(2, currencyCode);
                    renameCurrencyStmt.executeUpdate();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
                return new OperateResult(true, "OK");
            } catch (SQLException e) {
                MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[renameCurrency SQLException]{0}", e.getMessage());
                return new OperateResult(false, "重命名货币失败-数据库异常");
            }
        } else {
            return new OperateResult(false, "您并非此货币的发行者");
        }
    }

    // 准备金增加
    public static OperateResult reserveIncr(String currencyCode, BigDecimal amount, String playerName) {
        String reserveAccount = "$" + currencyCode;
        BigDecimal roundAmount = amount.setScale(4, RoundingMode.DOWN);
        if (isCurrencyOwner(currencyCode, playerName)) {
            try (
                    Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE); PreparedStatement updateCurrencyTotal = connection.prepareStatement(UPDATE_CURRENCY_TOTAL); PreparedStatement insertLog = connection.prepareStatement(INSERT_TX_LOG)) {
                try {
                    // 更新储备账户内的钱
                    updateBalance.setString(1, reserveAccount);
                    updateBalance.setString(2, currencyCode);
                    updateBalance.setBigDecimal(3, roundAmount);
                    updateBalance.setBigDecimal(4, roundAmount);
                    updateBalance.executeUpdate();
                    // 更新货币总量
                    updateCurrencyTotal.setBigDecimal(1, roundAmount);
                    updateCurrencyTotal.setString(2, currencyCode);
                    updateCurrencyTotal.executeUpdate();
                    // 更新操作日志
                    insertLog.setString(1, AccountHelper.MONETARY_AUTHORITY);
                    insertLog.setString(2, reserveAccount);
                    insertLog.setInt(3, TxTypeEnum.CURRENCY_RESERVE_INCREASE.ordinal());
                    insertLog.setString(4, currencyCode);
                    insertLog.setBigDecimal(5, roundAmount);
                    insertLog.setString(6, "");
                    insertLog.executeUpdate();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
                return new OperateResult(true, "OK");
            } catch (SQLException e) {
                MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[incrCurrency SQLException]{0}", e.getMessage());
                return new OperateResult(false, "货币增发失败-数据库异常");
            }
        } else {
            return new OperateResult(false, "您并非此货币的发行者");
        }
    }

    // 准备金减少
    public static OperateResult reserveDecr(String currencyCode, BigDecimal amount, String playerName) {
        String reserveAccount = "$" + currencyCode;
        BigDecimal roundAmount = amount.setScale(4, RoundingMode.DOWN).negate();
        if (isCurrencyOwner(currencyCode, playerName)) {
            try (
                    Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE); PreparedStatement updateCurrencyTotal = connection.prepareStatement(UPDATE_CURRENCY_TOTAL); PreparedStatement insertLog = connection.prepareStatement(INSERT_TX_LOG)) {
                try {
                    // 更新储备账户内的钱
                    updateBalance.setString(1, reserveAccount);
                    updateBalance.setString(2, currencyCode);
                    updateBalance.setBigDecimal(3, roundAmount);
                    updateBalance.setBigDecimal(4, roundAmount);
                    updateBalance.executeUpdate();
                    // 更新货币总量
                    updateCurrencyTotal.setBigDecimal(1, roundAmount);
                    updateCurrencyTotal.setString(2, currencyCode);
                    updateCurrencyTotal.executeUpdate();
                    // 更新操作日志
                    insertLog.setString(1, reserveAccount);
                    insertLog.setString(2, AccountHelper.MONETARY_AUTHORITY);
                    insertLog.setInt(3, TxTypeEnum.CURRENCY_RESERVE_DECREASE.ordinal());
                    insertLog.setString(4, currencyCode);
                    insertLog.setBigDecimal(5, roundAmount);
                    insertLog.setString(6, "");
                    insertLog.executeUpdate();
                    connection.commit();
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
                return new OperateResult(true, "OK");
            } catch (SQLException e) {
                MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[decrCurrency SQLException]{0}", e.getMessage());
                return new OperateResult(false, "货币回收失败-数据库异常");
            }
        } else {
            return new OperateResult(false, "您并非此货币的发行者");
        }
    }

    // 获取货币详细信息
    public static OperateResult getCurrencyInfo(String currencyCode) {
        try (
                Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection(); PreparedStatement selectCurrency = connection.prepareStatement(SELECT_CURRENCY_INFO)) {
            CurrencyInfoEntity cie = new CurrencyInfoEntity();
            try {
                selectCurrency.setString(1, currencyCode);
                selectCurrency.setString(2, currencyCode);
                selectCurrency.setString(3, currencyCode);
                selectCurrency.setString(4, currencyCode);
                ResultSet result = selectCurrency.executeQuery();
                if (result.next()) {
                    cie.setOwner(result.getString("owner"));
                    cie.setName(result.getString("name"));
                    cie.setTotal(result.getBigDecimal("currencyTotal"));
                    cie.setReserve(result.getBigDecimal("reserveBalance"));
                    cie.setBalanceSum(result.getBigDecimal("accountBalanceSum"));
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", cie);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[getCurrencyInfo SQLException]{0}", e.getMessage());
            return new OperateResult(false, "获取货币信息失败-数据库异常");
        }
    }
}

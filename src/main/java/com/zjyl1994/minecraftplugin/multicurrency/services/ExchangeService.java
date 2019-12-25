/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.services;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.utils.AccountHelper;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OperateResult;
import static com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum.CURRENCY_EXCHANGE_IN;
import static com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeEnum.CURRENCY_EXCHANGE_OUT;
import com.zjyl1994.minecraftplugin.multicurrency.utils.TxTypeHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

/**
 * 货币兑换逻辑
 *
 * @author zjyl1994
 */
public class ExchangeService {

    // 汇率为1单位外币可兑本币数量，FROM为外币，TO为本币
    private static final String GET_EXCHANGE_RATE = "SELECT amount FROM `exchange_rate` WHERE `from` = ? AND `to` = ?";
    private static final String SET_EXCHANGE_RATE = "INSERT INTO `exchange_rate` (`from`,`to`,`amount`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `amount` = ?";
    private static final String SELECT_BALANCE = "SELECT `balance` FROM `account` WHERE `username` = ? AND `code` = ?";
    private static final String UPDATE_BALANCE = "INSERT INTO `account` (`username`,`code`,`balance`) VALUES (?,?,?) ON DUPLICATE KEY UPDATE `balance` = `balance` + ?";
    private static final String INSERT_TX_LOG = "INSERT INTO tx_log (username,tx_username,tx_time,tx_type,currency_code,amount,remark) VALUES (?,?,NOW(),?,?,?,?)";
// 获得货币汇率

    public static OperateResult getExchangeRate(String currencyCodeFrom, String currencyCodeTo) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectExchangeRate = connection.prepareStatement(GET_EXCHANGE_RATE);) {
            BigDecimal exchangeRate;
            try {
                selectExchangeRate.setString(1, currencyCodeFrom);
                selectExchangeRate.setString(2, currencyCodeTo);
                ResultSet result = selectExchangeRate.executeQuery();
                if (result.next()) {
                    exchangeRate = result.getBigDecimal("amount");
                } else { // 没有记录
                    exchangeRate = new BigDecimal(0);
                }
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            return new OperateResult(true, "OK", exchangeRate);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[getExchangeRate SQLException]{0}", e.getMessage());
            return new OperateResult(false, "查询失败-数据库异常");
        }
    }

    // 设置货币汇率
    public static OperateResult setExchangeRate(String currencyCodeFrom, String currencyCodeTo, BigDecimal rate) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement setExchangeRate = connection.prepareStatement(SET_EXCHANGE_RATE);) {
            try {
                setExchangeRate.setString(1, currencyCodeFrom);
                setExchangeRate.setString(2, currencyCodeTo);
                setExchangeRate.setBigDecimal(3, rate);
                setExchangeRate.setBigDecimal(4, rate);
                setExchangeRate.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
            return new OperateResult(true, "OK");
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[setExchangeRate SQLException]{0}", e.getMessage());
            return new OperateResult(false, "设置货币汇率失败-数据库异常");
        }
    }

    // 兑换，from为外币，to为本币，amount为本币数量 兑换amount本币需要(1/exchangeRate)*amount外币
    public static OperateResult exchange(String username, String currencyCodeFrom, String currencyCodeTo, BigDecimal amount) {
        try (
                 Connection connection = MultiCurrencyPlugin.getInstance().getHikari().getConnection();  PreparedStatement selectBalance = connection.prepareStatement(SELECT_BALANCE);  PreparedStatement selectExchangeRate = connection.prepareStatement(GET_EXCHANGE_RATE);  PreparedStatement updateBalance = connection.prepareStatement(UPDATE_BALANCE);  PreparedStatement insertLog = connection.prepareStatement(INSERT_TX_LOG);) {
            // 查询汇率
            BigDecimal exchangeRate;
            try {
                selectExchangeRate.setString(1, currencyCodeFrom);
                selectExchangeRate.setString(2, currencyCodeTo);
                ResultSet result = selectExchangeRate.executeQuery();
                if (result.next()) {
                    exchangeRate = result.getBigDecimal("amount");
                } else { // 没有记录
                    exchangeRate = BigDecimal.ZERO;
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            // 计算所需外币数量
            if (exchangeRate.compareTo(BigDecimal.ZERO) == 0) {
                // 没有汇率，不可兑换
                return new OperateResult(false, "兑换失败," + currencyCodeTo + "发行人没有指定" + currencyCodeFrom + "->" + currencyCodeTo + "的汇率");
            }
            BigDecimal exchangeRequire = BigDecimal.ONE.divide(exchangeRate).multiply(amount);// 所需的外币数量
            // 检查玩家外币数量够不够
            BigDecimal playerBalance;
            try {
                selectBalance.setString(1, username);
                selectBalance.setString(2, currencyCodeFrom);
                ResultSet result = selectBalance.executeQuery();
                if (result.next()) {
                    playerBalance = result.getBigDecimal("balance");
                } else { // 没有记录
                    playerBalance = BigDecimal.ZERO;
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            if (exchangeRequire.compareTo(playerBalance) > 0) { // 待兑换外币数量超过玩家持有外币
                return new OperateResult(false, "兑换失败,兑换" + currencyCodeTo + amount.setScale(4, RoundingMode.DOWN) + "需要" + currencyCodeFrom + exchangeRequire.setScale(4, RoundingMode.DOWN) + "，当前余额" + currencyCodeFrom + playerBalance.setScale(4, RoundingMode.DOWN));
            }
            // 检查准备金够不够
            BigDecimal reserveBalance;
            try {
                selectBalance.setString(1, "$" + currencyCodeTo);
                selectBalance.setString(2, currencyCodeTo);
                ResultSet result = selectBalance.executeQuery();
                if (result.next()) {
                    reserveBalance = result.getBigDecimal("balance");
                } else { // 没有记录
                    reserveBalance = new BigDecimal(0);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            if (amount.compareTo(reserveBalance) > 0) { // 待兑换本币数量超过本币储备金额度
                return new OperateResult(false, "兑换失败," + currencyCodeTo + "储备金不足，当前余额" + reserveBalance.setScale(4, RoundingMode.DOWN));
            }
            // 玩家扣外币款
            try {
                updateBalance.setString(1, username);
                updateBalance.setString(2, currencyCodeFrom);
                updateBalance.setBigDecimal(3, exchangeRequire.negate());
                updateBalance.setBigDecimal(4, exchangeRequire.negate());
                updateBalance.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            // 准备金转入外币款
            try {
                updateBalance.setString(1, "$"+currencyCodeTo);
                updateBalance.setString(2, currencyCodeFrom);
                updateBalance.setBigDecimal(3, exchangeRequire);
                updateBalance.setBigDecimal(4, exchangeRequire);
                updateBalance.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            // 准备金扣本币款
            try {
                updateBalance.setString(1, "$"+currencyCodeTo);
                updateBalance.setString(2, currencyCodeTo);
                updateBalance.setBigDecimal(3, amount.negate());
                updateBalance.setBigDecimal(4, amount.negate());
                updateBalance.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            // 玩家账户转入本币款
            try {
                updateBalance.setString(1, username);
                updateBalance.setString(2, currencyCodeTo);
                updateBalance.setBigDecimal(3, amount);
                updateBalance.setBigDecimal(4, amount);
                updateBalance.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            // 记录日志
            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append(currencyCodeFrom);
            remarkBuilder.append(exchangeRequire.setScale(4, RoundingMode.DOWN));
            remarkBuilder.append("->");
            remarkBuilder.append(currencyCodeTo);
            remarkBuilder.append(amount.setScale(4,RoundingMode.DOWN));
            String remark = remarkBuilder.toString();
            try {
                insertLog.setString(1, username);
                insertLog.setString(2, "$"+currencyCodeTo);
                insertLog.setInt(3, CURRENCY_EXCHANGE_OUT.ordinal());
                insertLog.setString(4, currencyCodeFrom);
                insertLog.setBigDecimal(5, exchangeRequire.negate());
                insertLog.setString(6, remark);
                insertLog.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            try {
                insertLog.setString(1, username);
                insertLog.setString(2, "$"+currencyCodeTo);
                insertLog.setInt(3, CURRENCY_EXCHANGE_IN.ordinal());
                insertLog.setString(4, currencyCodeTo);
                insertLog.setBigDecimal(5, amount);
                insertLog.setString(6, remark);
                insertLog.executeUpdate();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
            connection.commit();
            return new OperateResult(true, "OK", reserveBalance);
        } catch (SQLException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[exchange SQLException]{0}", e.getMessage());
            return new OperateResult(false, "兑换失败-数据库异常");
        }
    }
}

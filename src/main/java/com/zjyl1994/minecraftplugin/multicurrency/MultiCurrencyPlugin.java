/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.zjyl1994.minecraftplugin.multicurrency;

import org.xjcraft.CommonPlugin;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

/**
 * @author zjyl1994
 */
public class MultiCurrencyPlugin extends CommonPlugin {
    private static MultiCurrencyPlugin instance;
    private DataSource hikari;
    private Random random;

    @Override
    public void onEnable() {
        instance = this;
        getCommand("bank").setExecutor(new MultiCurrencyCommandExecutor(this));
        getCommand("bank").setTabCompleter(new MultiCurrencyCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new MultiCurrencyEventListener(this), this);
        this.random = new Random(System.currentTimeMillis());
        this.saveDefaultConfig();

        hikari = getDataSource("MultiCurrency");
//        hikari.setDataSourceClassName(this.getConfig().getString("datasource.driver"));
//        hikari.addDataSourceProperty("serverName", this.getConfig().getString("datasource.server"));
//        hikari.addDataSourceProperty("port", this.getConfig().getInt("datasource.port"));
//        hikari.addDataSourceProperty("databaseName", this.getConfig().getString("datasource.database"));
//        hikari.addDataSourceProperty("user", this.getConfig().getString("datasource.username"));
//        hikari.addDataSourceProperty("password", this.getConfig().getString("datasource.password"));
//        hikari.setMaxLifetime(this.getConfig().getInt("datasource.hikari.maxLifetime"));
//        hikari.setMaximumPoolSize(this.getConfig().getInt("datasource.hikari.maximumPoolSize"));
//        hikari.setAutoCommit(false);
//        hikari.addDataSourceProperty("useUnicode", "true");
//        hikari.addDataSourceProperty("characterEncoding", "utf8");
        try (Connection connection = hikari.getConnection()) {
            String[] create = {"CREATE TABLE IF NOT EXISTS `mc_account` (`id` INT (11) NOT NULL AUTO_INCREMENT,`username` VARCHAR (50) NOT NULL COMMENT '存款人',`code` CHAR (3) NOT NULL COMMENT '货币代码',`balance` DECIMAL (16,6) NOT NULL DEFAULT 0.000000 COMMENT '账户余额',PRIMARY KEY (`id`),UNIQUE KEY `username_code` (`username`,`code`)) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='存款账户表';",
                    "CREATE TABLE IF NOT EXISTS `mc_currency` (`id` INT (11) NOT NULL AUTO_INCREMENT,`code` CHAR (3) NOT NULL COMMENT '货币代码',`owner` VARCHAR (50) NOT NULL COMMENT '货币发行人',`name` VARCHAR (50) NOT NULL COMMENT '货币常用名',`total` DECIMAL (16,6) NOT NULL DEFAULT 0.000000 COMMENT '货币发行总量',PRIMARY KEY (`id`),UNIQUE KEY `code` (`code`)) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='货币表';",
                    "CREATE TABLE IF NOT EXISTS `mc_exchange_rate` (`id` INT (11) NOT NULL AUTO_INCREMENT,`from` CHAR (3) NOT NULL COMMENT '兑出币种',`to` CHAR (3) NOT NULL COMMENT '兑入币种',`amount` DECIMAL (16,6) NOT NULL DEFAULT 0.000000 COMMENT '兑换比率',PRIMARY KEY (`id`),UNIQUE KEY `from_to` (`from`,`to`)) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='汇率表';",
                    "CREATE TABLE IF NOT EXISTS `mc_tx_log` (`id` INT (11) NOT NULL AUTO_INCREMENT,`username` VARCHAR (50) NOT NULL COMMENT '交易人',`tx_username` VARCHAR (50) NOT NULL COMMENT '交易对方',`tx_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP () COMMENT '交易时间',`tx_type` TINYINT (4) NOT NULL DEFAULT 0 COMMENT '交易类别\\r\\n0：保留类型，不使用\\r\\n1：货币储备新增\\r\\n2：货币储备减少\\r\\n3：电子渠道转入\\r\\n4：电子渠道转出\\r\\n5：商店交易收入\\r\\n6：商店交易扣款\\r\\n7：货币兑换入账\\r\\n8：货币兑换扣款\\r\\n9：实体支票开出\\r\\n10：实体支票入账',`currency_code` CHAR (3) NOT NULL COMMENT '交易币种',`amount` DECIMAL (16,6) NOT NULL DEFAULT 0.000000 COMMENT '交易金额',`remark` VARCHAR (100) NOT NULL DEFAULT '' COMMENT '备注',PRIMARY KEY (`id`),KEY `username` (`username`)) ENGINE=INNODB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8 COMMENT='交易日志表';"};
            for (String s : create) {
                PreparedStatement preparedStatement = connection.prepareStatement(s);
                preparedStatement.execute();
            }


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {

    }

    public static MultiCurrencyPlugin getInstance() {
        return instance;
    }

    public DataSource getHikari() {
        return hikari;
    }

    public Random getRandom() {
        return random;
    }
}

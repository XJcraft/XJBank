/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.zjyl1994.minecraftplugin.multicurrency;

import com.zaxxer.hikari.HikariDataSource;
import java.util.Random;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author zjyl1994
 */
public class MultiCurrencyPlugin extends JavaPlugin {
   private static MultiCurrencyPlugin instance;
   private HikariDataSource hikari;
   private Random random;
    
    @Override
    public void onEnable() {
        instance = this;
        getCommand("bank").setExecutor(new MultiCurrencyCommandExecutor(this));
        getServer().getPluginManager().registerEvents(new MultiCurrencyEventListener(this), this);
        this.random = new Random(System.currentTimeMillis());
        this.saveDefaultConfig();
        
        hikari = new HikariDataSource();
        hikari.setDataSourceClassName(this.getConfig().getString("datasource.driver"));
        hikari.addDataSourceProperty("serverName", this.getConfig().getString("datasource.server"));
        hikari.addDataSourceProperty("port", this.getConfig().getInt("datasource.port"));
        hikari.addDataSourceProperty("databaseName", this.getConfig().getString("datasource.database"));
        hikari.addDataSourceProperty("user", this.getConfig().getString("datasource.username"));
        hikari.addDataSourceProperty("password", this.getConfig().getString("datasource.password"));
        hikari.setMaxLifetime(this.getConfig().getInt("datasource.hikari.maxLifetime"));
        hikari.setMaximumPoolSize(this.getConfig().getInt("datasource.hikari.maximumPoolSize"));
        hikari.setAutoCommit(false);
        hikari.addDataSourceProperty("useUnicode", "true");
        hikari.addDataSourceProperty("characterEncoding", "utf8");
    }
    
    @Override
    public void onDisable() {
        if (hikari != null)
            hikari.close();
    }

    public static MultiCurrencyPlugin getInstance(){
        return instance;
    }

    public HikariDataSource getHikari(){
        return hikari;
    }

    public Random getRandom() {
        return random;
    }
}

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency;

import com.zjyl1994.minecraftplugin.multicurrency.command.AccountCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CheckCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CurrencyCMD;
import com.zjyl1994.minecraftplugin.multicurrency.services.CheckService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.CurrencyEntity;
import java.math.BigDecimal;
import java.util.Optional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 *
 * @author zjyl1994
 */
public class MultiCurrencyCommandExecutor implements CommandExecutor {
    private final MultiCurrencyPlugin plugin;
    private final CurrencyCMD currencyInstance;
    private final CheckCMD checkInstance;
    private final AccountCMD accountInstance;
    
    public MultiCurrencyCommandExecutor(MultiCurrencyPlugin plugin) {
        this.plugin = plugin;
        this.currencyInstance = CurrencyCMD.getInstance();
        this.checkInstance = CheckCMD.getInstance();
        this.accountInstance = AccountCMD.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("此指令仅支持用户使用");
            return false;
        } else if (strings.length == 0) {
            return false;
        } else if (strings.length >= 1) {
            String lowerCaseCMD = strings[0].toLowerCase();
            switch (lowerCaseCMD) {
                case "currency":
                    excuteCurrency(commandSender, command, s, strings);
                    break;
                case "pay":
                    excutePay(commandSender, command, s, strings);
                    break;
                case "test":
                    excuteTest(commandSender, command, s, strings);
                    break;
                default:
                    return false;
            }
            return true;
        }
        return false;
    }
    
    private void excuteTest (CommandSender commandSender, Command command, String s, String[] strings) {
        //commandSender.sendMessage(((Player) commandSender).getInventory().getItemInMainHand().getType().name());
        Player p = (Player) commandSender;
        ItemStack is = CheckService.getCheck(new CurrencyEntity("XJB",new BigDecimal(19745347.7945)), p.getName());
        p.getInventory().setItemInMainHand(is);
        Optional<CurrencyEntity> oce = CheckService.getValue(is);
        if(oce.isPresent()){
            CurrencyEntity ce = oce.get();
            StringBuilder sb = new StringBuilder();
            sb.append("有效支票");
            sb.append(ce.getCurrencyCode());
            sb.append(ce.getAmount().toString());
            p.sendMessage(sb.toString());
        }else{
            p.sendMessage("无效支票");
        }
    }
    
    // 新增货币 /bank currnecy new [货币代码] [货币名称]
    // 增发货币 /bank currency incr [货币代码] [增发货币数量]
    // 减少货币 /bank currency decr [货币代码] [减少货币数量]
    // 重命名货币 /bank currency rename [货币代码] [新货币名称]
    // 准备金提取 /bank currency get [货币代码] [货币数量]
    private void excuteCurrency (CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 4) {
            commandSender.sendMessage ("参数不正确\n新建货币 /bank currency new [货币代码] [货币名称]\n增发货币 /bank currency incr [货币代码] [增发货币数量]\n减少货币 /bank currency decr [货币代码] [减少货币数量]\n重命名货币 /bank currency rename [货币代码] [新货币名称]\n准备金提取 /bank currency get [货币代码] [货币数量]");
            return;
        }
        Player p = (Player) commandSender;
        if (strings[1].equalsIgnoreCase("new"))
            currencyInstance.newCommand(p, strings[2], strings[3]);
        if (strings[1].equalsIgnoreCase("incr"))
            currencyInstance.incrCommand(p, strings[2], strings[3]);
        if (strings[1].equalsIgnoreCase("decr"))
            currencyInstance.decrCommand(p, strings[2], strings[3]);
        if (strings[1].equalsIgnoreCase("rename"))
            currencyInstance.renameCommand(p, strings[2], strings[3]);
        if (strings[1].equalsIgnoreCase("get"))
            accountInstance.reserveTransferOut(p, strings[2], strings[3]);
    }
    
    // 直接转账 /bank pay [对方玩家名] [货币代码] [货币数量]
    private void excutePay(CommandSender commandSender, Command command, String s, String[] strings) {
         if (strings.length != 4) {
            commandSender.sendMessage ("参数不正确\n转账 /bank pay [对方玩家名] [货币代码] [货币数量]");
            return;
        }
        Player p = (Player) commandSender;
        accountInstance.transferToAccount(p, strings[1], strings[2], strings[3]);
    }
}
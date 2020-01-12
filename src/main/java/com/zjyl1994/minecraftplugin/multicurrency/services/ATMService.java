/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.services;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import com.zjyl1994.minecraftplugin.multicurrency.command.AccountCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CheckCMD;
import com.zjyl1994.minecraftplugin.multicurrency.utils.OutOfRangeCanceller;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Sign;
import org.bukkit.conversations.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * ATM 机
 *
 * @author zjyl1994
 */
public class ATMService implements ConversationAbandonedListener {

    private final MultiCurrencyPlugin plugin;

    private Player player; // 操作玩家
    private String feature; // ATM 选择的功能
    private String featureState; // 功能的步骤
    private ArrayList<String> argument; // 参数
    private Boolean operateDone; // 完成

    private final Location signLocation; // 牌子位置

    public ATMService(MultiCurrencyPlugin pluginInstance, Location signLocation) {
        this.plugin = pluginInstance;
        this.signLocation = signLocation;
        this.feature = "none"; // 默认什么都不做
        this.featureState = "none";
        this.argument = new ArrayList<>();
        this.operateDone = false;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent event) {
        if (operateDone) {
            this.player.sendMessage(ChatColor.GOLD + "指示已接纳，请稍等\n");
            Do();
        } else {
            this.player.sendMessage(ChatColor.GOLD + "您已退出ATM机\n");
        }
    }

    public void start(Player p) {
        this.player = p;
        ConversationFactory factory;
        // 检查是否直达功能
        Sign sign = (Sign) this.signLocation.getBlock().getState();
        String signFeature = ChatColor.stripColor(sign.getLine(1).trim());
        switch (signFeature) {
            case "直接转账":
                feature = "pay";
                featureState = "payUsername";
                factory = new ConversationFactory(this.plugin)
                        .withFirstPrompt(new UsernamePrompt())
                        .withConversationCanceller(new OutOfRangeCanceller(this.plugin, signLocation, 5))
                        .withTimeout(10)
                        .withEscapeSequence("exit").addConversationAbandonedListener(this);
                factory.buildConversation(p).begin();
                break;
            case "开出支票":
                String signCurrencyCode = ChatColor.stripColor(sign.getLine(2).trim());
                if (signCurrencyCode.isBlank()) {
                    feature = "check";
                    featureState = "checkCurrencyCode";
                    factory = new ConversationFactory(this.plugin)
                            .withFirstPrompt(new CurrencyCodePrompt())
                            .withConversationCanceller(new OutOfRangeCanceller(this.plugin, signLocation, 5))
                            .withTimeout(10)
                            .withEscapeSequence("exit").addConversationAbandonedListener(this);
                    factory.buildConversation(p).begin();
                } else {
                    feature = "check";
                    featureState = "checkAmount";
                    argument.add(signCurrencyCode.toUpperCase());
                    factory = new ConversationFactory(this.plugin)
                            .withFirstPrompt(new MoneyPrompt())
                            .withConversationCanceller(new OutOfRangeCanceller(this.plugin, signLocation, 5))
                            .withTimeout(10)
                            .withEscapeSequence("exit").addConversationAbandonedListener(this);
                    factory.buildConversation(p).begin();
                }
                break;
            case "兑现支票":
                feature = "cash";
                Do();
                break;
            case "查询余额":
                feature = "balance";
                Do();
                break;
            case "账单查询":
                feature = "log";
                Do();
                break;
            default:
                // 进入UI界面
                factory = new ConversationFactory(this.plugin)
                        .withFirstPrompt(new WelcomePrompt())
                        .withConversationCanceller(new OutOfRangeCanceller(this.plugin, signLocation, 5))
                        .withTimeout(10)
                        .withEscapeSequence("exit").addConversationAbandonedListener(this);
                factory.buildConversation(p).begin();
        }
    }

    public void Do() {
//        player.sendMessage(feature);
//        player.sendMessage(featureState);
//        player.sendMessage(argument.toArray(new String[0]));
        switch (feature) {
            case "pay":
                AccountCMD.getInstance().transferToAccount(player, argument.get(0), argument.get(1), argument.get(2));
                break;
            case "check":
                CheckCMD.getInstance().makeCheck(player, argument.get(0), argument.get(1));
                break;
            case "cash":
                CheckCMD.getInstance().cashCheck(player);
                break;
            case "balance":
                AccountCMD.getInstance().getAccountInfo(player);
                break;
            case "log":
                AccountCMD.getInstance().getAccountTradeLog(player, 1);
                break;
        }
    }

    // 欢迎句
    private class WelcomePrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "\n欢迎使用XJCraft金融管理局ATM机\n您可随时输入\"exit\"退出ATM机！\n10秒无操作或者走太远都会自动退出，请及时操作！";
        }

        @Override
        protected Prompt getNextPrompt(ConversationContext context) {
            return new MenuPrompt();
        }
    }

    private class MenuPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== 请输入业务编号 ===\n");
            sb.append("1.直接转账\n");
            sb.append("2.开出支票\n");
            sb.append("3.兑现支票\n");
            sb.append("4.查询余额\n");
            sb.append("5.账单查询\n");
            return sb.toString();
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() > 0 && input.intValue() < 6;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return ChatColor.GOLD + "所选功能不在本机服务范围内";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (input.intValue()) {
                case 1: // 直接转账
                    feature = "pay";
                    featureState = "payUsername";
                    return new UsernamePrompt();
                case 2: // 开出支票
                    feature = "check";
                    featureState = "checkCurrencyCode";
                    return new CurrencyCodePrompt();
                case 3: // 兑现支票
                    feature = "cash";
                    operateDone = true;
                    return Prompt.END_OF_CONVERSATION;
                case 4: // 查询余额
                    feature = "balance";
                    operateDone = true;
                    return Prompt.END_OF_CONVERSATION;
                case 5: // 账单查询
                    feature = "log";
                    operateDone = true;
                    return Prompt.END_OF_CONVERSATION;
                default: // 无操作
                    feature = "none";
                    operateDone = false;
                    return Prompt.END_OF_CONVERSATION;
            }
        }

    }

    private class UsernamePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "请输入收款人账号";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            switch (featureState) {
                case "payUsername":
                    argument.add(input);
                    featureState = "payCurrencyCode";
                    return new CurrencyCodePrompt();
                default:
                    operateDone = false;
                    return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private class CurrencyCodePrompt extends StringPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "请输入货币代码";
        }

        @Override
        public Prompt acceptInput(ConversationContext context, String input) {
            switch (featureState) {
                case "payCurrencyCode":
                    argument.add(input);
                    featureState = "payAmount";
                    return new MoneyPrompt();
                case "checkCurrencyCode":
                    argument.add(input);
                    featureState = "checkAmount";
                    return new MoneyPrompt();
                default:
                    operateDone = false;
                    return Prompt.END_OF_CONVERSATION;
            }
        }
    }

    private class MoneyPrompt extends NumericPrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "请输入金额";
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.doubleValue() > 0;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return ChatColor.GOLD + "金额必须大于0";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (featureState) {
                case "payAmount":
                case "checkAmount":
                    argument.add(Double.toString(input.doubleValue()));
                    featureState = "none";
                    operateDone = true;
                    return Prompt.END_OF_CONVERSATION;
                default:
                    operateDone = false;
                    return Prompt.END_OF_CONVERSATION;
            }
        }
    }
}

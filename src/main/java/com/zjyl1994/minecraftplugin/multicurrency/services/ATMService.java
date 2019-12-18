/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.services;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.bukkit.conversations.Conversable;
import org.bukkit.conversations.ConversationAbandonedEvent;
import org.bukkit.conversations.ConversationAbandonedListener;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.ConversationPrefix;
import org.bukkit.conversations.ExactMatchConversationCanceller;
import org.bukkit.conversations.MessagePrompt;
import org.bukkit.conversations.NullConversationPrefix;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.entity.Player;

/**
 * ATM 机
 *
 * @author zjyl1994
 */
public class ATMService implements ConversationAbandonedListener {

    private final ConversationFactory factory;
    private final MultiCurrencyPlugin plugin;

    private Player player; // 操作玩家
    private String feature; // ATM 选择的功能
    private String featureState; // 功能的步骤
    private ArrayList<String> argument; // 参数
    private Boolean operateDone; // 完成

    public ATMService(MultiCurrencyPlugin pluginInstance) {
        this.plugin = pluginInstance;
        this.factory = new ConversationFactory(pluginInstance)
                .withPrefix(new NullConversationPrefix())
                .withFirstPrompt(new WelcomePrompt())
                .withEscapeSequence("exit").addConversationAbandonedListener(this);
        this.feature = "none"; // 默认什么都不做
        this.featureState = "none";
        this.argument = new ArrayList<>();
        this.operateDone = false;
    }

    @Override
    public void conversationAbandoned(ConversationAbandonedEvent event) {
        if (operateDone) {
            this.player.sendMessage("正在为您办理业务");
            Do();
        } else {
            this.player.sendMessage("已退出ATM机");
        }
    }

    public void Start(Player p) {
        this.player = p;
        factory.buildConversation((Conversable) p).begin();
    }
    
    public void Do(){
        player.sendMessage(feature);
        player.sendMessage(featureState);
        player.sendMessage(argument.toArray(new String[0]));
    }

    // 欢迎句
    private class WelcomePrompt extends MessagePrompt {

        @Override
        public String getPromptText(ConversationContext context) {
            return ChatColor.GOLD + "欢迎使用XJCraft金融管理局ATM机\n您可通过输入exit随时退出ATM机";
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
            return sb.toString();
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.intValue() > 0 && input.intValue() < 4;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return "所选功能不在本机服务范围内";
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
            return ChatColor.BLUE + "请输入" + ChatColor.GOLD + "收款人" + ChatColor.BLUE + "账号";
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
            return ChatColor.BLUE + "请输入" + ChatColor.GOLD + "货币代码";
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
            return ChatColor.BLUE + "请输入" + ChatColor.GOLD + "金额";
        }

        @Override
        protected boolean isNumberValid(ConversationContext context, Number input) {
            return input.doubleValue() > 0;
        }

        @Override
        protected String getFailedValidationText(ConversationContext context, Number invalidInput) {
            return "金额必须大于0";
        }

        @Override
        protected Prompt acceptValidatedInput(ConversationContext context, Number input) {
            switch (featureState) {
                case "payAmount":
                    argument.add(Double.toString(input.doubleValue()));
                    featureState = "none";
                    operateDone = true;
                    return Prompt.END_OF_CONVERSATION;
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

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency;

import com.zjyl1994.minecraftplugin.multicurrency.command.AccountCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CheckCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.CurrencyCMD;
import com.zjyl1994.minecraftplugin.multicurrency.command.ExchangeCMD;
import com.zjyl1994.minecraftplugin.multicurrency.services.CurrencyService;
import com.zjyl1994.minecraftplugin.multicurrency.utils.BarrelUtil;
import com.zjyl1994.minecraftplugin.multicurrency.utils.MiscUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author zjyl1994
 */
public class MultiCurrencyCommandExecutor implements TabExecutor {

    private final MultiCurrencyPlugin plugin;
    private final CurrencyCMD currencyInstance;
    private final CheckCMD checkInstance;
    private final AccountCMD accountInstance;
    private final ExchangeCMD exchangeInstance;
    private List<String> currencies;

    public MultiCurrencyCommandExecutor(MultiCurrencyPlugin plugin) {
        this.plugin = plugin;
        this.currencyInstance = CurrencyCMD.getInstance();
        this.checkInstance = CheckCMD.getInstance();
        this.accountInstance = AccountCMD.getInstance();
        this.exchangeInstance = ExchangeCMD.getInstance();


    }

    public void updateCurrencies() {
        currencies = CurrencyService.getCurrencies();
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage("此指令仅支持玩家使用");
        } else if (strings.length <= 0) {
            commandSender.sendMessage("缺少子命令"); // 如果有 help 的话，这种情况应该执行 help
        } else {
            String lowerCaseCMD = strings[0].toLowerCase();
            switch (lowerCaseCMD) {
                case "currency":
                    excuteCurrency(commandSender, command, s, strings);
                    break;
                case "pay":
                    excutePay(commandSender, command, s, strings);
                    break;
                case "check":
                    excuteCheck(commandSender, command, s, strings);
                    break;
                case "cash":
                    excuteCash(commandSender, command, s, strings);
                    break;
                case "info":
                    excuteInfo(commandSender, command, s, strings);
                    break;
                case "bluk":
                    excuteBluk(commandSender, command, s, strings);
                    break;
                case "log":
                    excuteLog(commandSender, command, s, strings);
                    break;
                case "exchange":
                    excuteExchange(commandSender, command, s, strings);
                    break;
                case "test":
                    excuteTest(commandSender, command, s, strings);
                    break;
                default:
                    commandSender.sendMessage(String.format("未知的子命令 %s", lowerCaseCMD));
                    break;
            }
        }

        return true; // 这里返回 false 时会输出 plugin.yml 中的 usage
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        //Bukkit.getLogger().info(Arrays.deepToString(args));
        if (args.length == 1) {
            return Stream.of("currency", "pay", "check", "cash", "info", "bluk", "log", "exchange").filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        } else {
            String lastArg = args[args.length - 1];
            switch (args[0]) {
                case "currency":
                    if (args.length == 2) {
                        return Stream.of("new", "incr", "decr", "rename", "get", "balance", "pay").filter(s -> s.startsWith(lastArg)).collect(Collectors.toList());
                    } else {
                        switch (args[1]) {
                            case "new":
                                if (args.length == 3) {
                                    return Collections.singletonList("[货币代码] [货币名称]");
                                } else if (args.length == 4)
                                    return Collections.singletonList("[货币名称]");
                                break;
                            case "incr":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return Collections.singletonList("[增发货币数量]");
                                }
                                break;
                            case "decr":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return Collections.singletonList("[减少货币数量]");
                                }
                                break;
                            case "rename":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return Collections.singletonList("[新货币名称]");
                                }
                                break;
                            case "get":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return Collections.singletonList("[货币数量]");
                                }
                                break;
                            case "balance":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                }
                                break;
                            case "pay":
                                if (args.length == 3) {
                                    return null;
                                } else if (args.length == 4 || args.length == 5) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else {
                                    return Collections.singletonList("[货币数量]");
                                }
                        }
                    }
                    break;
                case "pay":
                    if (args.length == 2) {
                        return null;
                    } else if (args.length == 3) {
                        return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                    } else if (args.length == 4) {
                        return Collections.singletonList("[货币数量]");
                    }
                    break;
                case "check":
                    if (args.length == 2) {
                        return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                    } else if (args.length == 3) {
                        return Collections.singletonList("[货币数量]");
                    }
                    break;
                case "bluk":
                    if (args.length == 2) {
                        return Stream.of("check", "cash").filter(s -> s.startsWith(lastArg)).collect(Collectors.toList());
                    } else {
                        switch (args[1]) {
                            case "check":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return Collections.singletonList("[面值] [数量]");
                                } else {
                                    return Collections.singletonList("[数量]");
                                }
                            case "cash":
                                return Collections.emptyList();
                        }
                    }
                    break;
                case "info":
                    if (args.length == 2)
                        return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                    break;
                case "exchange":
                    if (args.length == 2)
                        return Stream.of("get", "set", "fx").filter(s -> s.startsWith(lastArg)).collect(Collectors.toList());
                    else
                        switch (args[1]) {
                            case "get":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toCollection(() -> Collections.singletonList("")));
                                } else if (args.length == 4) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                }
                                break;
//                                return Arrays.asList("[卖出货币代码] [买入货币代码]");
                            case "set":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 4) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toList());
                                } else if (args.length == 5) {
                                    return Collections.singletonList("[货币汇率]");
                                }
                                break;
//                                return Arrays.asList("[卖出货币代码] [买入货币代码] [货币汇率]");
                            case "fx":
                                if (args.length == 3) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toCollection(() -> new ArrayList<>(Arrays.asList("[卖出货币代码]"))));
                                } else if (args.length == 4) {
                                    return currencies.stream().filter(s -> s.startsWith(lastArg.toUpperCase())).collect(Collectors.toCollection(() -> new ArrayList<>(Arrays.asList("[买入货币代码]"))));
                                } else if (args.length == 5) {
                                    return Collections.singletonList("[买入货币数量]");
                                }
                                break;
//                                return Arrays.asList("[卖出货币代码] [买入货币代码] [买入货币数量]");
                        }
                    break;
                default:
                    return Collections.emptyList();
            }
        }
        return Collections.emptyList();
    }

    private void excuteTest(CommandSender commandSender, Command command, String s, String[] strings) {
        //commandSender.sendMessage(((Player) commandSender).getInventory().getItemInMainHand().getType().name());
        Player p = (Player) commandSender;
        HashSet<Material> transparent = new HashSet<>();
        transparent.add(Material.AIR);
        Block seenBlock = p.getTargetBlock(transparent, 30);
        List<Location> barrels = BarrelUtil.searchBarrel(seenBlock);
        Optional<Location> paperBarrel = Optional.empty();
        if (!barrels.isEmpty()) {
            paperBarrel = BarrelUtil.checkBarrelListItemStack(barrels, new ItemStack(Material.PAPER, 4));
        }
        StringBuilder sb = new StringBuilder();
        sb.append("视线Block:");
        sb.append(MiscUtil.locationToString(seenBlock.getLocation()));
        if (barrels.isEmpty()) {
            sb.append("\n检测不到可关联木桶\n");
        } else {
            sb.append("\n可关联木桶:\n");
            for (Location l : barrels) {
                sb.append(MiscUtil.locationToString(l));
                sb.append("\n");
            }
            sb.append("=>");
            if (paperBarrel.isPresent()) {
                sb.append(MiscUtil.locationToString(paperBarrel.get()));
                sb.append("中有4纸");
            } else {
                sb.append("桶里无4纸");
            }
        }
        p.sendMessage(sb.toString());
    }

    // 新增货币 /bank currnecy new [货币代码] [货币名称]
    // 增发货币 /bank currency incr [货币代码] [增发货币数量]
    // 减少货币 /bank currency decr [货币代码] [减少货币数量]
    // 重命名货币 /bank currency rename [货币代码] [新货币名称]
    // 准备金提取 /bank currency get [货币代码] [货币数量]
    // 查看准备金账户余额 /bank currency balance [货币代码]
    // 准备金付款 /bank currency pay [收款人] [准备金的货币代码] [待支付的货币代码] [货币数量]
    // 货币所有权转移 /bank currency reowner [货币代码] [新所有人游戏ID]
    private void excuteCurrency(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        if (strings.length == 4) {
            if (strings[1].equalsIgnoreCase("new")) {
                currencyInstance.newCommand(p, strings[2], strings[3]);
            }
            if (strings[1].equalsIgnoreCase("incr")) {
                currencyInstance.incrCommand(p, strings[2], strings[3]);
            }
            if (strings[1].equalsIgnoreCase("decr")) {
                currencyInstance.decrCommand(p, strings[2], strings[3]);
            }
            if (strings[1].equalsIgnoreCase("rename")) {
                currencyInstance.renameCommand(p, strings[2], strings[3]);
            }
            if (strings[1].equalsIgnoreCase("get")) {
                accountInstance.reserveTransferOut(p, strings[2], strings[3]);
            }
            if (strings[1].equalsIgnoreCase("reowner")) {
                currencyInstance.reownerCommand(p, strings[2], strings[3]);
            }
            return;
        }
        if (strings.length == 3) {
            if (strings[1].equalsIgnoreCase("balance")) {
                currencyInstance.currencyGetBalanceCommand(p, strings[2]);
            }
            return;
        }
        if (strings.length == 6) {
            if (strings[1].equalsIgnoreCase("pay")) {
                currencyInstance.currencyReservePayCommand(p, strings[2], strings[3], strings[4], strings[5]);
            }
            return;
        }
        commandSender.sendMessage("参数不正确\n新建货币 /bank currency new [货币代码] [货币名称]\n增发货币 /bank currency incr [货币代码] [增发货币数量]\n减少货币 /bank currency decr [货币代码] [减少货币数量]\n重命名货币 /bank currency rename [货币代码] [新货币名称]\n准备金提取 /bank currency get [货币代码] [货币数量]\n查看准备金账户余额 /bank currency balance [货币代码]\n准备金付款 /bank currency pay [收款人] [准备金的货币代码] [待支付的货币代码] [货币数量]");
    }

    // 直接转账 /bank pay [对方玩家名] [货币代码] [货币数量]
    private void excutePay(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 4) {
            commandSender.sendMessage("参数不正确\n转账 /bank pay [对方玩家名] [货币代码] [货币数量]");
            return;
        }
        Player p = (Player) commandSender;
        accountInstance.transferToAccount(p, strings[1], strings[2], strings[3]);
    }

    // 产生支票 /bank check [货币代码] [货币数量]
    private void excuteCheck(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length != 3) {
            commandSender.sendMessage("参数不正确\n开支票 /bank check [货币代码] [货币数量]");
            return;
        }
        Player p = (Player) commandSender;
        checkInstance.makeCheck(p, strings[1], strings[2]);
    }

    // 兑付手中的支票 /bank cash
    private void excuteCash(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        checkInstance.cashCheck(p, "");
    }

    // 批量操作
    private void excuteBluk(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        if (strings.length < 2) {
            p.sendMessage("批量操作参数不正确");
            return;
        }
        // 批量生产支票到背包 /bank bluk check [货币代码] [面值] [数量]
        if (strings[1].equalsIgnoreCase("check")) {
            if (strings.length != 5) {
                p.sendMessage("批量操作参数不正确\n批量生产支票到背包 /bank bluk check [货币代码] [面值] [数量]");
            } else {
                checkInstance.makeBlukCheck(p, strings[2], strings[3], strings[4]);
            }
        }
        // 批量兑现背包的支票 /bank bluk cash
        if (strings[1].equalsIgnoreCase("cash")) {
            checkInstance.cashBlukCheck(p);
        }
    }

    // 查询账户余额或查询货币详情 /bank info (货币代码)
    private void excuteInfo(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        if (strings.length == 2) {
            currencyInstance.currencyInfoCommand(p, strings[1]);
        } else {
            accountInstance.getAccountInfo(p);
        }
    }

    // 查询交易日志 /bank log (可选:币种) (页码)
    private void excuteLog(CommandSender commandSender, Command command, String s, String[] strings) {
        Player p = (Player) commandSender;
        Integer pageNo;
        String currencyCode;
        switch (strings.length) {
            case 2:
                if (strings[1].matches("^[A-Za-z]{3}$")) {
                    currencyCode = strings[1].toUpperCase();
                    pageNo = 1;
                } else {
                    currencyCode = "";
                    pageNo = MiscUtil.getIntegerFromString(strings[1]);
                }
                break;
            case 3:
                if (strings[1].matches("^[A-Za-z]{3}$")) {
                    currencyCode = strings[1].toUpperCase();
                } else {
                    currencyCode = "";
                }
                pageNo = MiscUtil.getIntegerFromString(strings[2]);
                break;
            default:
                currencyCode = "";
                pageNo = 1;
        }
        if (pageNo > 0) {
            accountInstance.getAccountTradeLog(p, pageNo, currencyCode);
        } else {
            p.sendMessage("没有第0页");
        }
    }

    // 货币兑换相关命令
    // 获取汇率 /bank exchange get [卖出货币代码] [买入货币代码]
    // 修改汇率 /bank exchange set [卖出货币代码] [买入货币代码] [货币汇率]
    // 兑换货币 /bank exchange fx [卖出货币代码] [买入货币代码] [买入货币数量]
    private void excuteExchange(CommandSender commandSender, Command command, String s, String[] strings) {
        String helpMessage = ChatColor.GOLD + "货币兑换相关命令" + ChatColor.RESET + "\n获取汇率 /bank exchange get [卖出货币代码] [买入货币代码]\n修改汇率 /bank exchange set [卖出货币代码] [买入货币代码] [货币汇率]\n兑换货币 /bank exchange fx [卖出货币代码] [买入货币代码] [买入货币数量]";
        Player p = (Player) commandSender;
        if (strings.length > 2) {
            if (strings[1].equalsIgnoreCase("get")) {
                if (strings.length == 4) {
                    exchangeInstance.getCommand(p, strings[2], strings[3]);
                } else {
                    p.sendMessage(helpMessage);
                }
            }
            if (strings[1].equalsIgnoreCase("set")) {
                if (strings.length == 5) {
                    exchangeInstance.setCommand(p, strings[2], strings[3], strings[4]);
                } else {
                    p.sendMessage(helpMessage);
                }
            }
            if (strings[1].equalsIgnoreCase("fx")) {
                if (strings.length == 5) {
                    exchangeInstance.exchangeCommand(p, strings[2], strings[3], strings[4]);
                } else {
                    p.sendMessage(helpMessage);
                }
            }
        } else {
            p.sendMessage(helpMessage);
        }
    }
}

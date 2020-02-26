/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.zjyl1994.minecraftplugin.multicurrency.utils;

import com.zjyl1994.minecraftplugin.multicurrency.MultiCurrencyPlugin;
import org.apache.commons.codec.binary.Hex;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;

/**
 * @author zjyl1994
 */
public class CheckUtil {

    // 获得支票价值
    public static Optional<CurrencyEntity> getValue(ItemStack itemStack) {
        if (itemStack == null) {
            return Optional.empty();
        }
        // 滤掉不是书的
        if (!(itemStack.getType() == Material.WRITTEN_BOOK)) {
            return Optional.empty();
        }
        if (!(itemStack.getItemMeta() instanceof BookMeta)) {
            return Optional.empty();
        }
        // 书的数据
        var meta = (BookMeta) itemStack.getItemMeta();
        // 滤掉作者不对的
        if (!"XJCraft金融管理局".equals(meta.getAuthor())) {
            return Optional.empty();
        }
        // 滤掉不是破旧不堪的
        if (meta.getGeneration() != BookMeta.Generation.TATTERED) {
            return Optional.empty();
        }
        // 滤掉不是两页的
        if (meta.getPageCount() != 2) {
            return Optional.empty();
        }
        // 机读页内容(第二页)
        var content = meta.getPage(2);

        // 检查机读区并读取数据
        String[] mrpLines = content.replaceAll("§0", "").split("\n");
        if (mrpLines.length != 6) {
            return Optional.empty();
        }
        for (String mrpLine : mrpLines) {
            if (!mrpLine.startsWith(">>")) {
                return Optional.empty();
            }
        }

        String checkId = mrpLines[0].substring(2).trim();
        String currencyCode = mrpLines[1].substring(2).trim();
        BigDecimal amount = new BigDecimal(mrpLines[2].substring(2).trim());
        String issuer = mrpLines[3].substring(2).trim();
        String dateTimeStr = mrpLines[4].substring(2).trim();
        String checkHash = mrpLines[5].substring(2).trim();
        // 检查数字签名
        String calcHash = generateCheckHash(checkId, currencyCode, amount, issuer, dateTimeStr);
        if (calcHash.equals(checkHash)) {
            return Optional.of(new CurrencyEntity(currencyCode, amount, issuer));
        } else {
            return Optional.empty();
        }
    }

    // 生成一本支票
    public static ItemStack getCheck(CurrencyEntity ce, String issuer) {
        // 获取相关信息
        BigDecimal roundAmount = ce.getAmount().setScale(4, RoundingMode.DOWN);
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        String dateTimeStr = dtf.format(now);
        String moneyString = ce.getCurrencyCode() + " " + roundAmount.stripTrailingZeros().toPlainString();
        String checkId = randomID();
        String checkHash = generateCheckHash(checkId, ce.getCurrencyCode(), roundAmount, issuer, dateTimeStr);
        // 生成可视书页内容
        List<String> content = new ArrayList<>();
        StringBuilder viewableBuilder = new StringBuilder();
        viewableBuilder.append("XJCraft 电子现金支票\n=====\n\n编号:");
        viewableBuilder.append(checkId);
        viewableBuilder.append("\n币种:");
        viewableBuilder.append(ce.getCurrencyCode());
        viewableBuilder.append("\n金额:");
        viewableBuilder.append(roundAmount.stripTrailingZeros().toPlainString());
        viewableBuilder.append("\n签发:");
        viewableBuilder.append(issuer);
        viewableBuilder.append("\n日期:");
        viewableBuilder.append(dateTimeStr, 0, 10);
        viewableBuilder.append("\n时间:");
        viewableBuilder.append(dateTimeStr, 11, 19);
        viewableBuilder.append("\n\n§8§nXJCraft金融管理局监制\n§7§k");
        viewableBuilder.append(checkHash);
        content.add(viewableBuilder.toString());
        // 生成机读页面内容
        StringBuilder mrpBuilder = new StringBuilder();
        mrpBuilder.append(">>");
        mrpBuilder.append(checkId);
        mrpBuilder.append("\n>>");
        mrpBuilder.append(ce.getCurrencyCode());
        mrpBuilder.append("\n>>");
        mrpBuilder.append(roundAmount.toPlainString());
        mrpBuilder.append("\n>>");
        mrpBuilder.append(issuer);
        mrpBuilder.append("\n>>");
        mrpBuilder.append(dateTimeStr);
        mrpBuilder.append("\n>>");
        mrpBuilder.append(checkHash);
        content.add(mrpBuilder.toString());
        // 制作支票书
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.setDisplayName(ChatColor.DARK_PURPLE + moneyString);
        meta.setTitle(moneyString);
        meta.setAuthor("XJCraft金融管理局");
        meta.setPages(content);
        meta.setGeneration(BookMeta.Generation.TATTERED);
        book.setItemMeta(meta);
        return book;
    }

    // 生成支票签名哈希
    // id 支票编号，currencyCode 支票币种，amount 金额，issuer 签发人，dateTime 签法时间
    private static String generateCheckHash(String id, String currencyCode, BigDecimal amount, String issuer, String dateTime) {
        String signContent = id +
                currencyCode +
                amount.toString() +
                issuer +
                dateTime +
                MultiCurrencyPlugin.getInstance().getConfig().getString("secert");
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(signContent.getBytes());
            String hashString = Hex.encodeHexString(md.digest());
            return hashString.substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            MultiCurrencyPlugin.getInstance().getLogger().log(Level.WARNING, "[generateCheckHash NoSuchAlgorithmException]{0}", e.getMessage());
            return "8F00B204E9800998"; // 空字串的 MD5
        }
    }

    // 生成随机支票号码
    private static String randomID() {
        int max = 999999999;
        int min = 100000000;
        int r = MultiCurrencyPlugin.getInstance().getRandom().nextInt(max);
        int s = r % (max - min + 1) + min;
        return Integer.toString(s);
    }
}

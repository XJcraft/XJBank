-- --------------------------------------------------------
-- 主机:                           127.0.0.1
-- 服务器版本:                        10.5.0-MariaDB - mariadb.org binary distribution
-- 服务器OS:                        Win64
-- HeidiSQL 版本:                  10.2.0.5599
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;


-- Dumping database structure for multicurrency
CREATE DATABASE IF NOT EXISTS `multicurrency` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `multicurrency`;

-- Dumping structure for table multicurrency.account
CREATE TABLE IF NOT EXISTS `mc_account`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '存款人',
  `code` char(3) NOT NULL COMMENT '货币代码',
  `balance` decimal(16,6) NOT NULL DEFAULT 0.000000 COMMENT '账户余额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_code` (`username`,`code`)
) ENGINE=InnoDB AUTO_INCREMENT=128 DEFAULT CHARSET=utf8 COMMENT='存款账户表';

-- Data exporting was unselected.

-- Dumping structure for table multicurrency.currency
CREATE TABLE IF NOT EXISTS `mc_currency`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `code` char(3) NOT NULL COMMENT '货币代码',
  `owner` varchar(50) NOT NULL COMMENT '货币发行人',
  `name` varchar(50) NOT NULL COMMENT '货币常用名',
  `total` decimal(16,6) NOT NULL DEFAULT 0.000000 COMMENT '货币发行总量',
  PRIMARY KEY (`id`),
  UNIQUE KEY `code` (`code`)
) ENGINE=InnoDB AUTO_INCREMENT=12 DEFAULT CHARSET=utf8 COMMENT='货币表';

-- Data exporting was unselected.

-- Dumping structure for table multicurrency.exchange_rate
CREATE TABLE IF NOT EXISTS `mc_exchange_rate`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `from` char(3) NOT NULL COMMENT '兑出币种',
  `to` char(3) NOT NULL COMMENT '兑入币种',
  `amount` decimal(16,6) NOT NULL DEFAULT 0.000000 COMMENT '兑换比率',
  PRIMARY KEY (`id`),
  UNIQUE KEY `from_to` (`from`,`to`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 COMMENT='汇率表';

-- Data exporting was unselected.

-- Dumping structure for table multicurrency.tx_log
CREATE TABLE IF NOT EXISTS `mc_tx_log`
(
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '交易人',
  `tx_username` varchar(50) NOT NULL COMMENT '交易对方',
  `tx_time` datetime NOT NULL DEFAULT current_timestamp() COMMENT '交易时间',
  `tx_type` tinyint(4) NOT NULL DEFAULT 0 COMMENT '交易类别\r\n0：保留类型，不使用\r\n1：货币储备新增\r\n2：货币储备减少\r\n3：电子渠道转入\r\n4：电子渠道转出\r\n5：商店交易收入\r\n6：商店交易扣款\r\n7：货币兑换入账\r\n8：货币兑换扣款\r\n9：实体支票开出\r\n10：实体支票入账',
  `currency_code` char(3) NOT NULL COMMENT '交易币种',
  `amount` decimal(16,6) NOT NULL DEFAULT 0.000000 COMMENT '交易金额',
  `remark` varchar(100) NOT NULL DEFAULT '' COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=198 DEFAULT CHARSET=utf8 COMMENT='交易日志表';

-- Data exporting was unselected.

/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IF(@OLD_FOREIGN_KEY_CHECKS IS NULL, 1, @OLD_FOREIGN_KEY_CHECKS) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;

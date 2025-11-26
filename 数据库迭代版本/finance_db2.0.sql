-- MySQL dump 10.13  Distrib 8.0.34, for Win64 (x86_64)
--
-- Host: localhost    Database: finance_db
-- ------------------------------------------------------
-- Server version	8.0.29

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ai_model_config`
--

DROP TABLE IF EXISTS `ai_model_config`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_model_config` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型配置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `model_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称(DeepSeek/GPT-4/文心/通义)',
  `model_provider` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务提供商',
  `api_key` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥',
  `api_endpoint` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API端点URL',
  `model_type` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'chat' COMMENT '模型类型:chat/completion',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认:0-否,1-是',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-启用',
  `max_tokens` int DEFAULT '2000' COMMENT '最大Token数',
  `temperature` decimal(3,2) DEFAULT '0.70' COMMENT '温度参数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `ai_model_config_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ai_prompt`
--

DROP TABLE IF EXISTS `ai_prompt`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_prompt` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提示词ID',
  `user_id` bigint DEFAULT NULL COMMENT '创建用户ID(NULL表示系统默认)',
  `title` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词内容',
  `category` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '分类:股票推荐/买卖建议/风险评估',
  `version` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'v1.0' COMMENT '版本号',
  `is_active` tinyint DEFAULT '1' COMMENT '是否启用:0-否,1-是',
  `is_system` tinyint DEFAULT '0' COMMENT '是否系统默认:0-否,1-是',
  `usage_count` int DEFAULT '0' COMMENT '使用次数',
  `avg_satisfaction` decimal(3,2) DEFAULT NULL COMMENT '平均满意度(0-5分)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_category` (`category`),
  CONSTRAINT `ai_prompt_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `backtest_result`
--

DROP TABLE IF EXISTS `backtest_result`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `backtest_result` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回测ID',
  `advice_id` bigint NOT NULL COMMENT '投资建议ID',
  `prompt_id` bigint DEFAULT NULL COMMENT '使用的提示词ID',
  `total_return` decimal(10,4) DEFAULT NULL COMMENT '总收益率(%)',
  `annualized_return` decimal(10,4) DEFAULT NULL COMMENT '年化收益率(%)',
  `max_drawdown` decimal(10,4) DEFAULT NULL COMMENT '最大回撤(%)',
  `sharpe_ratio` decimal(10,4) DEFAULT NULL COMMENT '夏普比率',
  `win_rate` decimal(5,2) DEFAULT NULL COMMENT '胜率(%)',
  `volatility` decimal(10,4) DEFAULT NULL COMMENT '波动率(%)',
  `backtest_start_date` date NOT NULL COMMENT '回测开始日期',
  `backtest_end_date` date NOT NULL COMMENT '回测结束日期',
  `backtest_duration` int DEFAULT NULL COMMENT '回测天数',
  `is_success` tinyint DEFAULT '0' COMMENT '是否达标:0-未达标,1-达标',
  `failure_reason` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '未达标原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_advice_id` (`advice_id`),
  KEY `idx_advice_created` (`advice_id`,`created_at` DESC),
  KEY `prompt_id` (`prompt_id`),
  CONSTRAINT `backtest_result_ibfk_1` FOREIGN KEY (`advice_id`) REFERENCES `investment_advice` (`id`) ON DELETE CASCADE,
  CONSTRAINT `backtest_result_ibfk_2` FOREIGN KEY (`prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回测结果表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `company_profile`
--

DROP TABLE IF EXISTS `company_profile`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `company_profile` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公司ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `company_name` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '公司全称',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '公司简介',
  `industry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属行业',
  `sub_industry` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '细分行业',
  `market_cap` decimal(20,2) DEFAULT NULL COMMENT '总市值',
  `pe_ratio` decimal(10,4) DEFAULT NULL COMMENT '市盈率',
  `pb_ratio` decimal(10,4) DEFAULT NULL COMMENT '市净率',
  `eps` decimal(10,4) DEFAULT NULL COMMENT '每股收益',
  `roe` decimal(10,4) DEFAULT NULL COMMENT '净资产收益率(%)',
  `revenue` decimal(20,2) DEFAULT NULL COMMENT '营业收入',
  `profit` decimal(20,2) DEFAULT NULL COMMENT '净利润',
  `address` text COLLATE utf8mb4_unicode_ci COMMENT '公司地址',
  `ceo` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT 'CEO姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stock_id` (`stock_code`),
  CONSTRAINT `company_profile_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司概况表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `db_version`
--

DROP TABLE IF EXISTS `db_version`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `db_version` (
  `id` int NOT NULL AUTO_INCREMENT,
  `version` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `applied_at` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库版本管理表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `investment_advice`
--

DROP TABLE IF EXISTS `investment_advice`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `investment_advice` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '建议ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `prompt_id` bigint DEFAULT NULL COMMENT '使用的提示词ID',
  `model_config_id` bigint DEFAULT NULL COMMENT '使用的AI模型ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议标题',
  `content` text COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议内容(JSON格式存储推荐股票列表)',
  `reasoning` text COLLATE utf8mb4_unicode_ci COMMENT '建议理由',
  `risk_assessment` text COLLATE utf8mb4_unicode_ci COMMENT '风险评估',
  `recommended_stocks` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '推荐股票代码列表(逗号分隔)',
  `target_return_rate` decimal(10,4) DEFAULT NULL COMMENT '目标收益率(%)',
  `is_valid` tinyint DEFAULT '1' COMMENT '是否有效:0-无效,1-有效',
  `is_read` tinyint DEFAULT '0' COMMENT '是否已读:0-未读,1-已读',
  `user_rating` tinyint DEFAULT NULL COMMENT '用户评分(1-5)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_user_created` (`user_id`,`created_at` DESC),
  KEY `prompt_id` (`prompt_id`),
  KEY `model_config_id` (`model_config_id`),
  CONSTRAINT `investment_advice_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `investment_advice_ibfk_2` FOREIGN KEY (`prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL,
  CONSTRAINT `investment_advice_ibfk_3` FOREIGN KEY (`model_config_id`) REFERENCES `ai_model_config` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资建议表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_prompt_usage_increment` AFTER INSERT ON `investment_advice` FOR EACH ROW BEGIN
    IF NEW.prompt_id IS NOT NULL THEN
        UPDATE ai_prompt
        SET usage_count = usage_count + 1
        WHERE id = NEW.prompt_id;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `investment_preference`
--

DROP TABLE IF EXISTS `investment_preference`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `investment_preference` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `risk_tolerance_level` tinyint NOT NULL COMMENT '风险承受能力:1-保守,2-稳健,3-平衡,4-积极,5-激进',
  `investment_horizon_type` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'preset' COMMENT '期限类型:preset-预设(短期/中期/长期),custom-自定义',
  `investment_horizon_preset` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '预设投资期限:short-短期(1-3月),medium-中期(3-12月),long-长期(1年以上)',
  `investment_horizon_custom_days` int DEFAULT NULL COMMENT '自定义投资期限(天数)',
  `investment_horizon_custom_months` int DEFAULT NULL COMMENT '自定义投资期限(月数)',
  `investment_horizon_custom_years` int DEFAULT NULL COMMENT '自定义投资期限(年数)',
  `investment_horizon_display` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '投资期限显示文本(如"6个月"、"2年")',
  `capital_amount` decimal(15,2) DEFAULT NULL COMMENT '投资金额',
  `preferred_asset_classes` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '偏好资产类别',
  `preferred_industry` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '偏好行业',
  `min_expected_return` decimal(6,2) DEFAULT NULL COMMENT '最低预期收益率(%)',
  `max_acceptable_loss` decimal(6,2) DEFAULT NULL COMMENT '最大可接受亏损(%)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`),
  CONSTRAINT `investment_preference_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `investment_preference_chk_1` CHECK ((`risk_tolerance_level` between 1 and 5)),
  CONSTRAINT `investment_preference_chk_2` CHECK ((((`investment_horizon_type` = _utf8mb4'preset') and (`investment_horizon_preset` is not null)) or ((`investment_horizon_type` = _utf8mb4'custom') and ((`investment_horizon_custom_days` is not null) or (`investment_horizon_custom_months` is not null) or (`investment_horizon_custom_years` is not null)))))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资偏好表-支持预设和自定义投资期限';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `prompt_iteration_log`
--

DROP TABLE IF EXISTS `prompt_iteration_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `prompt_iteration_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '迭代日志ID',
  `advice_id` bigint NOT NULL COMMENT '投资建议ID',
  `backtest_id` bigint DEFAULT NULL COMMENT '回测结果ID',
  `iteration_round` tinyint NOT NULL COMMENT '迭代轮次(1-5)',
  `original_prompt_id` bigint DEFAULT NULL COMMENT '原始提示词ID',
  `optimized_prompt_content` text COLLATE utf8mb4_unicode_ci COMMENT '优化后的提示词内容',
  `backtest_return_rate` decimal(10,4) DEFAULT NULL COMMENT '回测收益率(%)',
  `target_return_rate` decimal(10,4) DEFAULT NULL COMMENT '目标收益率阈值(%)',
  `is_success` tinyint DEFAULT '0' COMMENT '是否达标:0-未达标,1-达标',
  `optimization_strategy` varchar(100) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '优化策略',
  `failure_analysis` text COLLATE utf8mb4_unicode_ci COMMENT '失败原因分析',
  `improvement_points` text COLLATE utf8mb4_unicode_ci COMMENT '改进要点',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_advice_id` (`advice_id`),
  KEY `idx_iteration_round` (`iteration_round`),
  KEY `backtest_id` (`backtest_id`),
  KEY `original_prompt_id` (`original_prompt_id`),
  CONSTRAINT `prompt_iteration_log_ibfk_1` FOREIGN KEY (`advice_id`) REFERENCES `investment_advice` (`id`) ON DELETE CASCADE,
  CONSTRAINT `prompt_iteration_log_ibfk_2` FOREIGN KEY (`backtest_id`) REFERENCES `backtest_result` (`id`) ON DELETE SET NULL,
  CONSTRAINT `prompt_iteration_log_ibfk_3` FOREIGN KEY (`original_prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词迭代日志表-支持AI自动优化闭环';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shareholder`
--

DROP TABLE IF EXISTS `shareholder`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shareholder` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '股东ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `shareholder_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股东名称',
  `holding_shares` bigint NOT NULL COMMENT '持股数量',
  `holding_percentage` decimal(10,4) NOT NULL COMMENT '持股比例(%)',
  `change_shares` bigint DEFAULT NULL COMMENT '变动股数',
  `change_percentage` decimal(10,4) DEFAULT NULL COMMENT '变动比例(%)',
  `report_date` date NOT NULL COMMENT '报告期',
  `is_institutional` tinyint DEFAULT '0' COMMENT '是否机构投资者:0-否,1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_stock_report` (`stock_code`,`report_date`),
  CONSTRAINT `shareholder_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股东信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `shareholding_structure`
--

DROP TABLE IF EXISTS `shareholding_structure`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shareholding_structure` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结构ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `total_shares` bigint NOT NULL COMMENT '总股本',
  `float_shares` bigint DEFAULT NULL COMMENT '流通股本',
  `major_shareholder_count` int DEFAULT NULL COMMENT '主要股东数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stock_id` (`stock_code`),
  CONSTRAINT `shareholding_structure_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持股结构表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_history`
--

DROP TABLE IF EXISTS `stock_history`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_history` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史数据ID',
  `stock_id` bigint NOT NULL COMMENT '股票ID',
  `stock_code` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '股票代码',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `trade_time` time DEFAULT NULL COMMENT '交易时间(用于分钟级数据)',
  `timeframe` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT 'D' COMMENT '时间周期:1m,5m,15m,30m,60m,D,M,Q,Y',
  `open_price` decimal(10,2) NOT NULL COMMENT '开盘价',
  `high_price` decimal(10,2) NOT NULL COMMENT '最高价',
  `low_price` decimal(10,2) NOT NULL COMMENT '最低价',
  `close_price` decimal(10,2) NOT NULL COMMENT '收盘价',
  `volume` bigint DEFAULT NULL COMMENT '成交量(股)',
  `amount` decimal(20,2) DEFAULT NULL COMMENT '成交额(元)',
  `change_amount` decimal(10,2) DEFAULT NULL COMMENT '涨跌额',
  `change_percent` decimal(6,2) DEFAULT NULL COMMENT '涨跌幅(%)',
  `ma5` decimal(10,2) DEFAULT NULL COMMENT '5周期均线',
  `ma10` decimal(10,2) DEFAULT NULL COMMENT '10日均线',
  `ma20` decimal(10,2) DEFAULT NULL COMMENT '20日均线',
  `ma60` decimal(10,2) DEFAULT NULL COMMENT '60日均线',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_date_time` (`stock_id`,`trade_date`,`trade_time`,`timeframe`),
  KEY `idx_trade_date` (`trade_date`),
  KEY `idx_stock_trade_date` (`stock_id`,`trade_date` DESC),
  KEY `idx_timeframe` (`timeframe`),
  KEY `idx_stock_timeframe_date` (`stock_id`,`timeframe`,`trade_date` DESC),
  KEY `idx_stock_code` (`stock_code`),
  KEY `idx_stock_code_date` (`stock_code`,`trade_date` DESC),
  CONSTRAINT `stock_history_ibfk_1` FOREIGN KEY (`stock_id`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=10567 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票历史数据表-扩展版';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `stock_info`
--

DROP TABLE IF EXISTS `stock_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `stock_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '股票ID',
  `stock_code` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股票代码(如sh600000)',
  `stock_symbol` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股票简称(如600000)',
  `stock_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股票名称(如浦发银行)',
  `exchange` varchar(10) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易所:SH-上交所,SZ-深交所',
  `industry` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属行业',
  `sector` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '所属板块',
  `listing_date` date DEFAULT NULL COMMENT '上市日期',
  `current_price` decimal(10,2) DEFAULT NULL COMMENT '当前价格',
  `yesterday_close` decimal(10,2) DEFAULT NULL COMMENT '昨收价',
  `change_percent` decimal(6,2) DEFAULT NULL COMMENT '涨跌幅(%)',
  `market_value` decimal(20,2) DEFAULT NULL COMMENT '总市值(元)',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-停牌,1-正常交易',
  `last_update_time` datetime DEFAULT NULL COMMENT '最后更新时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `stock_code` (`stock_code`),
  KEY `idx_stock_name` (`stock_name`),
  KEY `idx_exchange` (`exchange`),
  KEY `idx_industry` (`industry`)
) ENGINE=InnoDB AUTO_INCREMENT=5169 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `technical_indicators`
--

DROP TABLE IF EXISTS `technical_indicators`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `technical_indicators` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '指标ID',
  `stock_id` bigint NOT NULL COMMENT '股票ID',
  `indicator_date` date NOT NULL COMMENT '指标日期',
  `macd` decimal(10,4) DEFAULT NULL COMMENT 'MACD指标',
  `rsi` decimal(10,4) DEFAULT NULL COMMENT 'RSI指标',
  `kdj_k` decimal(10,4) DEFAULT NULL COMMENT 'KDJ-K值',
  `kdj_d` decimal(10,4) DEFAULT NULL COMMENT 'KDJ-D值',
  `kdj_j` decimal(10,4) DEFAULT NULL COMMENT 'KDJ-J值',
  `boll_upper` decimal(10,2) DEFAULT NULL COMMENT '布林线上轨',
  `boll_middle` decimal(10,2) DEFAULT NULL COMMENT '布林线中轨',
  `boll_lower` decimal(10,2) DEFAULT NULL COMMENT '布林线下轨',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_stock_indicator_date` (`stock_id`,`indicator_date`),
  CONSTRAINT `technical_indicators_ibfk_1` FOREIGN KEY (`stock_id`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术指标表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_favorite`
--

DROP TABLE IF EXISTS `user_favorite`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_favorite` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自选ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stock_id` bigint NOT NULL COMMENT '股票ID',
  `sort_order` int DEFAULT '0' COMMENT '排序序号',
  `remark` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_stock` (`user_id`,`stock_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `stock_id` (`stock_id`),
  CONSTRAINT `user_favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE,
  CONSTRAINT `user_favorite_ibfk_2` FOREIGN KEY (`stock_id`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自选股表';
/*!40101 SET character_set_client = @saved_cs_client */;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = utf8mb4 */ ;
/*!50003 SET character_set_results = utf8mb4 */ ;
/*!50003 SET collation_connection  = utf8mb4_0900_ai_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'STRICT_TRANS_TABLES,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER `tr_favorite_sort_order` BEFORE INSERT ON `user_favorite` FOR EACH ROW BEGIN
    DECLARE v_max_order INT;

    SELECT COALESCE(MAX(sort_order), 0) INTO v_max_order
    FROM user_favorite
    WHERE user_id = NEW.user_id;

    IF NEW.sort_order IS NULL OR NEW.sort_order = 0 THEN
        SET NEW.sort_order = v_max_order + 1;
    END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;

--
-- Table structure for table `user_info`
--

DROP TABLE IF EXISTS `user_info`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_info` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `email` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `phone_number` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号',
  `avatar_url` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像URL',
  `status` tinyint DEFAULT '1' COMMENT '状态:0-禁用,1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  UNIQUE KEY `email` (`email`),
  KEY `idx_username` (`username`),
  KEY `idx_email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Temporary view structure for view `v_backtest_summary`
--

DROP TABLE IF EXISTS `v_backtest_summary`;
/*!50001 DROP VIEW IF EXISTS `v_backtest_summary`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_backtest_summary` AS SELECT 
 1 AS `backtest_id`,
 1 AS `advice_id`,
 1 AS `advice_title`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `total_return`,
 1 AS `annualized_return`,
 1 AS `max_drawdown`,
 1 AS `sharpe_ratio`,
 1 AS `win_rate`,
 1 AS `backtest_start_date`,
 1 AS `backtest_end_date`,
 1 AS `backtest_duration`,
 1 AS `is_success`,
 1 AS `created_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_investment_advice_detail`
--

DROP TABLE IF EXISTS `v_investment_advice_detail`;
/*!50001 DROP VIEW IF EXISTS `v_investment_advice_detail`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_investment_advice_detail` AS SELECT 
 1 AS `advice_id`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `title`,
 1 AS `content`,
 1 AS `reasoning`,
 1 AS `recommended_stocks`,
 1 AS `target_return_rate`,
 1 AS `prompt_title`,
 1 AS `model_name`,
 1 AS `user_rating`,
 1 AS `is_read`,
 1 AS `created_at`*/;
SET character_set_client = @saved_cs_client;

--
-- Temporary view structure for view `v_user_favorite_detail`
--

DROP TABLE IF EXISTS `v_user_favorite_detail`;
/*!50001 DROP VIEW IF EXISTS `v_user_favorite_detail`*/;
SET @saved_cs_client     = @@character_set_client;
/*!50503 SET character_set_client = utf8mb4 */;
/*!50001 CREATE VIEW `v_user_favorite_detail` AS SELECT 
 1 AS `favorite_id`,
 1 AS `user_id`,
 1 AS `username`,
 1 AS `stock_id`,
 1 AS `stock_code`,
 1 AS `stock_name`,
 1 AS `current_price`,
 1 AS `yesterday_close`,
 1 AS `change_percent`,
 1 AS `industry`,
 1 AS `sort_order`,
 1 AS `remark`,
 1 AS `added_date`*/;
SET character_set_client = @saved_cs_client;

--
-- Final view structure for view `v_backtest_summary`
--

/*!50001 DROP VIEW IF EXISTS `v_backtest_summary`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_backtest_summary` AS select `br`.`id` AS `backtest_id`,`br`.`advice_id` AS `advice_id`,`ia`.`title` AS `advice_title`,`ia`.`user_id` AS `user_id`,`u`.`username` AS `username`,`br`.`total_return` AS `total_return`,`br`.`annualized_return` AS `annualized_return`,`br`.`max_drawdown` AS `max_drawdown`,`br`.`sharpe_ratio` AS `sharpe_ratio`,`br`.`win_rate` AS `win_rate`,`br`.`backtest_start_date` AS `backtest_start_date`,`br`.`backtest_end_date` AS `backtest_end_date`,`br`.`backtest_duration` AS `backtest_duration`,`br`.`is_success` AS `is_success`,`br`.`created_at` AS `created_at` from ((`backtest_result` `br` join `investment_advice` `ia` on((`br`.`advice_id` = `ia`.`id`))) join `user_info` `u` on((`ia`.`user_id` = `u`.`id`))) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_investment_advice_detail`
--

/*!50001 DROP VIEW IF EXISTS `v_investment_advice_detail`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_investment_advice_detail` AS select `ia`.`id` AS `advice_id`,`ia`.`user_id` AS `user_id`,`u`.`username` AS `username`,`ia`.`title` AS `title`,`ia`.`content` AS `content`,`ia`.`reasoning` AS `reasoning`,`ia`.`recommended_stocks` AS `recommended_stocks`,`ia`.`target_return_rate` AS `target_return_rate`,`p`.`title` AS `prompt_title`,`m`.`model_name` AS `model_name`,`ia`.`user_rating` AS `user_rating`,`ia`.`is_read` AS `is_read`,`ia`.`created_at` AS `created_at` from (((`investment_advice` `ia` join `user_info` `u` on((`ia`.`user_id` = `u`.`id`))) left join `ai_prompt` `p` on((`ia`.`prompt_id` = `p`.`id`))) left join `ai_model_config` `m` on((`ia`.`model_config_id` = `m`.`id`))) where (`ia`.`is_valid` = 1) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;

--
-- Final view structure for view `v_user_favorite_detail`
--

/*!50001 DROP VIEW IF EXISTS `v_user_favorite_detail`*/;
/*!50001 SET @saved_cs_client          = @@character_set_client */;
/*!50001 SET @saved_cs_results         = @@character_set_results */;
/*!50001 SET @saved_col_connection     = @@collation_connection */;
/*!50001 SET character_set_client      = utf8mb4 */;
/*!50001 SET character_set_results     = utf8mb4 */;
/*!50001 SET collation_connection      = utf8mb4_0900_ai_ci */;
/*!50001 CREATE ALGORITHM=UNDEFINED */
/*!50013 DEFINER=`root`@`localhost` SQL SECURITY DEFINER */
/*!50001 VIEW `v_user_favorite_detail` AS select `uf`.`id` AS `favorite_id`,`uf`.`user_id` AS `user_id`,`u`.`username` AS `username`,`uf`.`stock_id` AS `stock_id`,`s`.`stock_code` AS `stock_code`,`s`.`stock_name` AS `stock_name`,`s`.`current_price` AS `current_price`,`s`.`yesterday_close` AS `yesterday_close`,`s`.`change_percent` AS `change_percent`,`s`.`industry` AS `industry`,`uf`.`sort_order` AS `sort_order`,`uf`.`remark` AS `remark`,`uf`.`created_at` AS `added_date` from ((`user_favorite` `uf` join `user_info` `u` on((`uf`.`user_id` = `u`.`id`))) join `stock_info` `s` on((`uf`.`stock_id` = `s`.`id`))) where ((`u`.`status` = 1) and (`s`.`status` = 1)) */;
/*!50001 SET character_set_client      = @saved_cs_client */;
/*!50001 SET character_set_results     = @saved_cs_results */;
/*!50001 SET collation_connection      = @saved_col_connection */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-11-26 11:44:52

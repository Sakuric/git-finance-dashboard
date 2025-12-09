/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80029
 Source Host           : localhost:3306
 Source Schema         : finance_db

 Target Server Type    : MySQL
 Target Server Version : 80029
 File Encoding         : 65001

 Date: 02/12/2025 10:19:47
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for ai_model_config
-- ----------------------------
DROP TABLE IF EXISTS `ai_model_config`;
CREATE TABLE `ai_model_config`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '模型配置ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `model_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模型名称(DeepSeek/GPT-4/文心/通义)',
  `model_provider` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '服务提供商',
  `api_key` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API密钥',
  `api_endpoint` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT 'API端点URL',
  `model_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'chat' COMMENT '模型类型:chat/completion',
  `is_default` tinyint NULL DEFAULT 0 COMMENT '是否默认:0-否,1-是',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
  `max_tokens` int NULL DEFAULT 2000 COMMENT '最大Token数',
  `temperature` decimal(3, 2) NULL DEFAULT 0.70 COMMENT '温度参数',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `ai_model_config_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI模型配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for ai_prompt
-- ----------------------------
DROP TABLE IF EXISTS `ai_prompt`;
CREATE TABLE `ai_prompt`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '提示词ID',
  `user_id` bigint NULL DEFAULT NULL COMMENT '创建用户ID(NULL表示系统默认)',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '提示词内容',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '分类:股票推荐/买卖建议/风险评估',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'v1.0' COMMENT '版本号',
  `is_active` tinyint NULL DEFAULT 1 COMMENT '是否启用:0-否,1-是',
  `is_system` tinyint NULL DEFAULT 0 COMMENT '是否系统默认:0-否,1-是',
  `usage_count` int NULL DEFAULT 0 COMMENT '使用次数',
  `avg_satisfaction` decimal(3, 2) NULL DEFAULT NULL COMMENT '平均满意度(0-5分)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_category`(`category` ASC) USING BTREE,
  CONSTRAINT `ai_prompt_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = 'AI提示词表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for backtest_result
-- ----------------------------
DROP TABLE IF EXISTS `backtest_result`;
CREATE TABLE `backtest_result`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '回测ID',
  `advice_id` bigint NOT NULL COMMENT '投资建议ID',
  `prompt_id` bigint NULL DEFAULT NULL COMMENT '使用的提示词ID',
  `total_return` decimal(10, 4) NULL DEFAULT NULL COMMENT '总收益率(%)',
  `annualized_return` decimal(10, 4) NULL DEFAULT NULL COMMENT '年化收益率(%)',
  `max_drawdown` decimal(10, 4) NULL DEFAULT NULL COMMENT '最大回撤(%)',
  `sharpe_ratio` decimal(10, 4) NULL DEFAULT NULL COMMENT '夏普比率',
  `win_rate` decimal(5, 2) NULL DEFAULT NULL COMMENT '胜率(%)',
  `volatility` decimal(10, 4) NULL DEFAULT NULL COMMENT '波动率(%)',
  `backtest_start_date` date NOT NULL COMMENT '回测开始日期',
  `backtest_end_date` date NOT NULL COMMENT '回测结束日期',
  `backtest_duration` int NULL DEFAULT NULL COMMENT '回测天数',
  `is_success` tinyint NULL DEFAULT 0 COMMENT '是否达标:0-未达标,1-达标',
  `failure_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '未达标原因',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_advice_id`(`advice_id` ASC) USING BTREE,
  INDEX `idx_advice_created`(`advice_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `prompt_id`(`prompt_id` ASC) USING BTREE,
  CONSTRAINT `backtest_result_ibfk_1` FOREIGN KEY (`advice_id`) REFERENCES `investment_advice` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `backtest_result_ibfk_2` FOREIGN KEY (`prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '回测结果表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for company_profile
-- ----------------------------
DROP TABLE IF EXISTS `company_profile`;
CREATE TABLE `company_profile`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '公司ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `company_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '公司全称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公司简介',
  `industry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属行业',
  `sub_industry` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '细分行业',
  `market_cap` decimal(20, 2) NULL DEFAULT NULL COMMENT '总市值',
  `pe_ratio` decimal(10, 4) NULL DEFAULT NULL COMMENT '市盈率',
  `pb_ratio` decimal(10, 4) NULL DEFAULT NULL COMMENT '市净率',
  `eps` decimal(10, 4) NULL DEFAULT NULL COMMENT '每股收益',
  `roe` decimal(10, 4) NULL DEFAULT NULL COMMENT '净资产收益率(%)',
  `revenue` decimal(20, 2) NULL DEFAULT NULL COMMENT '营业收入',
  `profit` decimal(20, 2) NULL DEFAULT NULL COMMENT '净利润',
  `address` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '公司地址',
  `ceo` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT 'CEO姓名',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `stock_id`(`stock_code` ASC) USING BTREE,
  CONSTRAINT `company_profile_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '公司概况表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for db_version
-- ----------------------------
DROP TABLE IF EXISTS `db_version`;
CREATE TABLE `db_version`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `applied_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '数据库版本管理表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for investment_advice
-- ----------------------------
DROP TABLE IF EXISTS `investment_advice`;
CREATE TABLE `investment_advice`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '建议ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `prompt_id` bigint NULL DEFAULT NULL COMMENT '使用的提示词ID',
  `model_config_id` bigint NULL DEFAULT NULL COMMENT '使用的AI模型ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '建议内容(JSON格式存储推荐股票列表)',
  `reasoning` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '建议理由',
  `risk_assessment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '风险评估',
  `recommended_stocks` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '推荐股票代码列表(逗号分隔)',
  `target_return_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '目标收益率(%)',
  `is_valid` tinyint NULL DEFAULT 1 COMMENT '是否有效:0-无效,1-有效',
  `is_read` tinyint NULL DEFAULT 0 COMMENT '是否已读:0-未读,1-已读',
  `user_rating` tinyint NULL DEFAULT NULL COMMENT '用户评分(1-5)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `idx_user_created`(`user_id` ASC, `created_at` DESC) USING BTREE,
  INDEX `prompt_id`(`prompt_id` ASC) USING BTREE,
  INDEX `model_config_id`(`model_config_id` ASC) USING BTREE,
  CONSTRAINT `investment_advice_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `investment_advice_ibfk_2` FOREIGN KEY (`prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `investment_advice_ibfk_3` FOREIGN KEY (`model_config_id`) REFERENCES `ai_model_config` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投资建议表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for investment_preference
-- ----------------------------
DROP TABLE IF EXISTS `investment_preference`;
CREATE TABLE `investment_preference`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '偏好ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `risk_tolerance_level` tinyint NOT NULL COMMENT '风险承受能力:1-保守,2-稳健,3-平衡,4-积极,5-激进',
  `investment_horizon_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'preset' COMMENT '期限类型:preset-预设(短期/中期/长期),custom-自定义',
  `investment_horizon_preset` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '预设投资期限:short-短期(1-3月),medium-中期(3-12月),long-长期(1年以上)',
  `investment_horizon_custom_days` int NULL DEFAULT NULL COMMENT '自定义投资期限(天数)',
  `investment_horizon_custom_months` int NULL DEFAULT NULL COMMENT '自定义投资期限(月数)',
  `investment_horizon_custom_years` int NULL DEFAULT NULL COMMENT '自定义投资期限(年数)',
  `investment_horizon_display` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '投资期限显示文本(如\"6个月\"、\"2年\")',
  `capital_amount` decimal(15, 2) NULL DEFAULT NULL COMMENT '投资金额',
  `preferred_asset_classes` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '偏好资产类别',
  `preferred_industry` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '偏好行业',
  `min_expected_return` decimal(6, 2) NULL DEFAULT NULL COMMENT '最低预期收益率(%)',
  `max_acceptable_loss` decimal(6, 2) NULL DEFAULT NULL COMMENT '最大可接受亏损(%)',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `investment_preference_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `investment_preference_chk_1` CHECK (`risk_tolerance_level` between 1 and 5),
  CONSTRAINT `investment_preference_chk_2` CHECK (((`investment_horizon_type` = _utf8mb4'preset') and (`investment_horizon_preset` is not null)) or ((`investment_horizon_type` = _utf8mb4'custom') and ((`investment_horizon_custom_days` is not null) or (`investment_horizon_custom_months` is not null) or (`investment_horizon_custom_years` is not null))))
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '投资偏好表-支持预设和自定义投资期限' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for prompt_iteration_log
-- ----------------------------
DROP TABLE IF EXISTS `prompt_iteration_log`;
CREATE TABLE `prompt_iteration_log`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '迭代日志ID',
  `advice_id` bigint NOT NULL COMMENT '投资建议ID',
  `backtest_id` bigint NULL DEFAULT NULL COMMENT '回测结果ID',
  `iteration_round` tinyint NOT NULL COMMENT '迭代轮次(1-5)',
  `original_prompt_id` bigint NULL DEFAULT NULL COMMENT '原始提示词ID',
  `optimized_prompt_content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '优化后的提示词内容',
  `backtest_return_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '回测收益率(%)',
  `target_return_rate` decimal(10, 4) NULL DEFAULT NULL COMMENT '目标收益率阈值(%)',
  `is_success` tinyint NULL DEFAULT 0 COMMENT '是否达标:0-未达标,1-达标',
  `optimization_strategy` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '优化策略',
  `failure_analysis` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '失败原因分析',
  `improvement_points` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '改进要点',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_advice_id`(`advice_id` ASC) USING BTREE,
  INDEX `idx_iteration_round`(`iteration_round` ASC) USING BTREE,
  INDEX `backtest_id`(`backtest_id` ASC) USING BTREE,
  INDEX `original_prompt_id`(`original_prompt_id` ASC) USING BTREE,
  CONSTRAINT `prompt_iteration_log_ibfk_1` FOREIGN KEY (`advice_id`) REFERENCES `investment_advice` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `prompt_iteration_log_ibfk_2` FOREIGN KEY (`backtest_id`) REFERENCES `backtest_result` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `prompt_iteration_log_ibfk_3` FOREIGN KEY (`original_prompt_id`) REFERENCES `ai_prompt` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '提示词迭代日志表-支持AI自动优化闭环' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for shareholder
-- ----------------------------
DROP TABLE IF EXISTS `shareholder`;
CREATE TABLE `shareholder`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '股东ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `shareholder_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股东名称',
  `holding_shares` bigint NOT NULL COMMENT '持股数量',
  `holding_percentage` decimal(10, 4) NOT NULL COMMENT '持股比例(%)',
  `change_shares` bigint NULL DEFAULT NULL COMMENT '变动股数',
  `change_percentage` decimal(10, 4) NULL DEFAULT NULL COMMENT '变动比例(%)',
  `report_date` date NOT NULL COMMENT '报告期',
  `is_institutional` tinyint NULL DEFAULT 0 COMMENT '是否机构投资者:0-否,1-是',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_stock_report`(`stock_code` ASC, `report_date` ASC) USING BTREE,
  CONSTRAINT `shareholder_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '股东信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for shareholding_structure
-- ----------------------------
DROP TABLE IF EXISTS `shareholding_structure`;
CREATE TABLE `shareholding_structure`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '结构ID',
  `stock_code` bigint NOT NULL COMMENT '股票ID',
  `total_shares` bigint NOT NULL COMMENT '总股本',
  `float_shares` bigint NULL DEFAULT NULL COMMENT '流通股本',
  `major_shareholder_count` int NULL DEFAULT NULL COMMENT '主要股东数量',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `stock_id`(`stock_code` ASC) USING BTREE,
  CONSTRAINT `shareholding_structure_ibfk_1` FOREIGN KEY (`stock_code`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '持股结构表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for stock_history
-- ----------------------------
DROP TABLE IF EXISTS `stock_history`;
CREATE TABLE `stock_history`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史数据ID',
  `stock_id` bigint NOT NULL COMMENT '股票ID',
  `stock_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '股票代码',
  `trade_date` date NOT NULL COMMENT '交易日期',
  `trade_time` time NULL DEFAULT NULL COMMENT '交易时间(用于分钟级数据)',
  `timeframe` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'D' COMMENT '时间周期:1m,5m,15m,30m,60m,D,M,Q,Y',
  `open_price` decimal(10, 2) NOT NULL COMMENT '开盘价',
  `high_price` decimal(10, 2) NOT NULL COMMENT '最高价',
  `low_price` decimal(10, 2) NOT NULL COMMENT '最低价',
  `close_price` decimal(10, 2) NOT NULL COMMENT '收盘价',
  `volume` bigint NULL DEFAULT NULL COMMENT '成交量(股)',
  `amount` decimal(20, 2) NULL DEFAULT NULL COMMENT '成交额(元)',
  `change_amount` decimal(10, 2) NULL DEFAULT NULL COMMENT '涨跌额',
  `change_percent` decimal(6, 2) NULL DEFAULT NULL COMMENT '涨跌幅(%)',
  `ma5` decimal(10, 2) NULL DEFAULT NULL COMMENT '5周期均线',
  `ma10` decimal(10, 2) NULL DEFAULT NULL COMMENT '10日均线',
  `ma20` decimal(10, 2) NULL DEFAULT NULL COMMENT '20日均线',
  `ma60` decimal(10, 2) NULL DEFAULT NULL COMMENT '60日均线',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_stock_date_time`(`stock_id` ASC, `trade_date` ASC, `trade_time` ASC, `timeframe` ASC) USING BTREE,
  INDEX `idx_trade_date`(`trade_date` ASC) USING BTREE,
  INDEX `idx_stock_trade_date`(`stock_id` ASC, `trade_date` DESC) USING BTREE,
  INDEX `idx_timeframe`(`timeframe` ASC) USING BTREE,
  INDEX `idx_stock_timeframe_date`(`stock_id` ASC, `timeframe` ASC, `trade_date` DESC) USING BTREE,
  INDEX `idx_stock_code`(`stock_code` ASC) USING BTREE,
  INDEX `idx_stock_code_date`(`stock_code` ASC, `trade_date` DESC) USING BTREE,
  CONSTRAINT `stock_history_ibfk_1` FOREIGN KEY (`stock_id`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 10098 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '股票历史数据表-扩展版' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for stock_info
-- ----------------------------
DROP TABLE IF EXISTS `stock_info`;
CREATE TABLE `stock_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '股票ID',
  `stock_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股票代码(如sh600000)',
  `stock_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '股票名称(如浦发银行)',
  `exchange` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '交易所:SH-上交所,SZ-深交所',
  `industry` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属行业',
  `sector` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '所属板块',
  `listing_date` date NULL DEFAULT NULL COMMENT '上市日期',
  `current_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '当前价格',
  `yesterday_close` decimal(10, 2) NULL DEFAULT NULL COMMENT '昨收价',
  `change_percent` decimal(6, 2) NULL DEFAULT NULL COMMENT '涨跌幅(%)',
  `total_market_cap` decimal(20, 4) NULL DEFAULT NULL COMMENT '总市值(亿元)',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:0-停牌,1-正常交易',
  `last_update_time` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `stock_code`(`stock_code` ASC) USING BTREE,
  INDEX `idx_stock_name`(`stock_name` ASC) USING BTREE,
  INDEX `idx_exchange`(`exchange` ASC) USING BTREE,
  INDEX `idx_industry`(`industry` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10099 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '股票信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_favorite
-- ----------------------------
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '自选ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `stock_id` bigint NOT NULL COMMENT '股票ID',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序序号',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_stock`(`user_id` ASC, `stock_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_created_at`(`created_at` ASC) USING BTREE,
  INDEX `stock_id`(`stock_id` ASC) USING BTREE,
  CONSTRAINT `user_favorite_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `user_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `user_favorite_ibfk_2` FOREIGN KEY (`stock_id`) REFERENCES `stock_info` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户自选股表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Table structure for user_info
-- ----------------------------
DROP TABLE IF EXISTS `user_info`;
CREATE TABLE `user_info`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码(BCrypt加密)',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱',
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar_url` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户信息表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- View structure for v_backtest_summary
-- ----------------------------
DROP VIEW IF EXISTS `v_backtest_summary`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_backtest_summary` AS select `br`.`id` AS `backtest_id`,`br`.`advice_id` AS `advice_id`,`ia`.`title` AS `advice_title`,`ia`.`user_id` AS `user_id`,`u`.`username` AS `username`,`br`.`total_return` AS `total_return`,`br`.`annualized_return` AS `annualized_return`,`br`.`max_drawdown` AS `max_drawdown`,`br`.`sharpe_ratio` AS `sharpe_ratio`,`br`.`win_rate` AS `win_rate`,`br`.`backtest_start_date` AS `backtest_start_date`,`br`.`backtest_end_date` AS `backtest_end_date`,`br`.`backtest_duration` AS `backtest_duration`,`br`.`is_success` AS `is_success`,`br`.`created_at` AS `created_at` from ((`backtest_result` `br` join `investment_advice` `ia` on((`br`.`advice_id` = `ia`.`id`))) join `user_info` `u` on((`ia`.`user_id` = `u`.`id`)));

-- ----------------------------
-- View structure for v_investment_advice_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_investment_advice_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_investment_advice_detail` AS select `ia`.`id` AS `advice_id`,`ia`.`user_id` AS `user_id`,`u`.`username` AS `username`,`ia`.`title` AS `title`,`ia`.`content` AS `content`,`ia`.`reasoning` AS `reasoning`,`ia`.`recommended_stocks` AS `recommended_stocks`,`ia`.`target_return_rate` AS `target_return_rate`,`p`.`title` AS `prompt_title`,`m`.`model_name` AS `model_name`,`ia`.`user_rating` AS `user_rating`,`ia`.`is_read` AS `is_read`,`ia`.`created_at` AS `created_at` from (((`investment_advice` `ia` join `user_info` `u` on((`ia`.`user_id` = `u`.`id`))) left join `ai_prompt` `p` on((`ia`.`prompt_id` = `p`.`id`))) left join `ai_model_config` `m` on((`ia`.`model_config_id` = `m`.`id`))) where (`ia`.`is_valid` = 1);

-- ----------------------------
-- View structure for v_user_favorite_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_user_favorite_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_user_favorite_detail` AS select `uf`.`id` AS `favorite_id`,`uf`.`user_id` AS `user_id`,`u`.`username` AS `username`,`uf`.`stock_id` AS `stock_id`,`s`.`stock_code` AS `stock_code`,`s`.`stock_name` AS `stock_name`,`s`.`current_price` AS `current_price`,`s`.`yesterday_close` AS `yesterday_close`,`s`.`change_percent` AS `change_percent`,`s`.`industry` AS `industry`,`uf`.`sort_order` AS `sort_order`,`uf`.`remark` AS `remark`,`uf`.`created_at` AS `added_date` from ((`user_favorite` `uf` join `user_info` `u` on((`uf`.`user_id` = `u`.`id`))) join `stock_info` `s` on((`uf`.`stock_id` = `s`.`id`))) where ((`u`.`status` = 1) and (`s`.`status` = 1));

-- ----------------------------
-- Procedure structure for sp_add_favorite
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_add_favorite`;
delimiter ;;
CREATE PROCEDURE `sp_add_favorite`(IN p_user_id BIGINT,
    IN p_stock_code VARCHAR(20))
BEGIN
    DECLARE v_stock_id BIGINT;
    DECLARE v_count INT;

    -- 查询股票ID
    SELECT id INTO v_stock_id FROM stock_info WHERE stock_code = p_stock_code AND status = 1;

    IF v_stock_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '股票不存在或已停牌';
    END IF;

    -- 检查是否已添加
    SELECT COUNT(*) INTO v_count FROM user_favorite WHERE user_id = p_user_id AND stock_id = v_stock_id;

    IF v_count > 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '该股票已在自选股列表中';
    END IF;

    -- 检查自选股数量限制（最多50只）
    SELECT COUNT(*) INTO v_count FROM user_favorite WHERE user_id = p_user_id;

    IF v_count >= 50 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '自选股数量已达上限(50只)';
    END IF;

    -- 添加自选股
    INSERT INTO user_favorite (user_id, stock_id, sort_order)
    VALUES (p_user_id, v_stock_id, v_count + 1);

    SELECT '添加成功' AS message;
END
;;
delimiter ;

-- ----------------------------
-- Procedure structure for sp_get_latest_kline
-- ----------------------------
DROP PROCEDURE IF EXISTS `sp_get_latest_kline`;
delimiter ;;
CREATE PROCEDURE `sp_get_latest_kline`(IN p_stock_code VARCHAR(20),
    IN p_days INT)
BEGIN
    DECLARE v_stock_id BIGINT;

    SELECT id INTO v_stock_id FROM stock_info WHERE stock_code = p_stock_code;

    IF v_stock_id IS NULL THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = '股票不存在';
    END IF;

    SELECT
        trade_date,
        trade_time,
        timeframe,
        open_price,
        high_price,
        low_price,
        close_price,
        volume,
        amount,
        change_percent,
        ma5
    FROM stock_history
    WHERE stock_id = v_stock_id AND timeframe = 'D'
    ORDER BY trade_date DESC
    LIMIT p_days;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table investment_advice
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_prompt_usage_increment`;
delimiter ;;
CREATE TRIGGER `tr_prompt_usage_increment` AFTER INSERT ON `investment_advice` FOR EACH ROW BEGIN
    IF NEW.prompt_id IS NOT NULL THEN
        UPDATE ai_prompt
        SET usage_count = usage_count + 1
        WHERE id = NEW.prompt_id;
    END IF;
END
;;
delimiter ;

-- ----------------------------
-- Triggers structure for table user_favorite
-- ----------------------------
DROP TRIGGER IF EXISTS `tr_favorite_sort_order`;
delimiter ;;
CREATE TRIGGER `tr_favorite_sort_order` BEFORE INSERT ON `user_favorite` FOR EACH ROW BEGIN
    DECLARE v_max_order INT;

    SELECT COALESCE(MAX(sort_order), 0) INTO v_max_order
    FROM user_favorite
    WHERE user_id = NEW.user_id;

    IF NEW.sort_order IS NULL OR NEW.sort_order = 0 THEN
        SET NEW.sort_order = v_max_order + 1;
    END IF;
END
;;
delimiter ;

SET FOREIGN_KEY_CHECKS = 1;

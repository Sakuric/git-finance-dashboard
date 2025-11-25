-- ============================================================
-- 量融智能金融平台数据库初始化脚本 (合并版)
-- 数据库名称: finance_db
-- 创建日期: 2024-11-24
-- 版本: v1.1 (合并了新浪财经API所需字段)
-- 说明: 包含11张核心表，并对 stock_history 表进行了扩展
-- ============================================================

-- 删除已存在的数据库（谨慎使用！生产环境请注释此行）
-- DROP DATABASE IF EXISTS finance_db;

-- 创建数据库
CREATE DATABASE IF NOT EXISTS finance_db
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE finance_db;

-- ============================================================
-- 1. 用户管理模块（1张表）
-- ============================================================

-- 用户表
DROP TABLE IF EXISTS user_info;
CREATE TABLE user_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(255) NOT NULL COMMENT '密码(BCrypt加密)',
    email VARCHAR(100) NOT NULL UNIQUE COMMENT '邮箱',
    phone_number VARCHAR(20) COMMENT '手机号',
    avatar_url VARCHAR(200) COMMENT '头像URL',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-正常',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户信息表';

-- ============================================================
-- 2. 股票数据模块（6张表）
-- ============================================================

-- 股票信息表
DROP TABLE IF EXISTS stock_info;
CREATE TABLE stock_info (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '股票ID',
    stock_code VARCHAR(20) NOT NULL UNIQUE COMMENT '股票代码(如sh600000)',
    stock_symbol VARCHAR(10) NOT NULL COMMENT '股票简称(如600000)',
    stock_name VARCHAR(100) NOT NULL COMMENT '股票名称(如浦发银行)',
    exchange VARCHAR(10) NOT NULL COMMENT '交易所:SH-上交所,SZ-深交所',
    industry VARCHAR(50) COMMENT '所属行业',
    sector VARCHAR(50) COMMENT '所属板块',
    listing_date DATE COMMENT '上市日期',
    current_price DECIMAL(10,2) COMMENT '当前价格',
    yesterday_close DECIMAL(10,2) COMMENT '昨收价',
    change_percent DECIMAL(6,2) COMMENT '涨跌幅(%)',
    market_value DECIMAL(20,2) COMMENT '总市值(元)',
    status TINYINT DEFAULT 1 COMMENT '状态:0-停牌,1-正常交易',
    last_update_time DATETIME COMMENT '最后更新时间',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_stock_code (stock_code),
    INDEX idx_stock_name (stock_name),
    INDEX idx_exchange (exchange),
    INDEX idx_industry (industry)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票信息表';

-- 股票历史数据表(K线数据) - 扩展版
DROP TABLE IF EXISTS stock_history;
CREATE TABLE stock_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '历史数据ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    stock_code VARCHAR(20) COMMENT '股票代码',
    trade_date DATE NOT NULL COMMENT '交易日期',
    trade_time TIME COMMENT '交易时间(用于分钟级数据)',
    timeframe VARCHAR(10) DEFAULT 'D' COMMENT '时间周期:1m,5m,15m,30m,60m,D,M,Q,Y',
    open_price DECIMAL(10,2) NOT NULL COMMENT '开盘价',
    high_price DECIMAL(10,2) NOT NULL COMMENT '最高价',
    low_price DECIMAL(10,2) NOT NULL COMMENT '最低价',
    close_price DECIMAL(10,2) NOT NULL COMMENT '收盘价',
    volume BIGINT COMMENT '成交量(股)',
    amount DECIMAL(20,2) COMMENT '成交额(元)',
    change_amount DECIMAL(10,2) COMMENT '涨跌额',
    change_percent DECIMAL(6,2) COMMENT '涨跌幅(%)',
    ma5 DECIMAL(10,2) COMMENT '5周期均线',
    ma10 DECIMAL(10,2) COMMENT '10日均线',
    ma20 DECIMAL(10,2) COMMENT '20日均线',
    ma60 DECIMAL(10,2) COMMENT '60日均线',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stock_date_time (stock_id, trade_date, trade_time, timeframe),
    INDEX idx_trade_date (trade_date),
    INDEX idx_stock_trade_date (stock_id, trade_date DESC),
    INDEX idx_timeframe (timeframe),
    INDEX idx_stock_timeframe_date (stock_id, timeframe, trade_date DESC),
    INDEX idx_stock_code (stock_code),
    INDEX idx_stock_code_date (stock_code, trade_date DESC),
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股票历史数据表-扩展版';

-- 更新现有数据的 stock_code（从 stock_info 表同步, 仅在旧数据存在时有效）
UPDATE stock_history sh
JOIN stock_info si ON sh.stock_id = si.id
SET sh.stock_code = si.stock_code
WHERE sh.stock_code IS NULL OR sh.stock_code = '';


-- 技术指标表
DROP TABLE IF EXISTS technical_indicators;
CREATE TABLE technical_indicators (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指标ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    indicator_date DATE NOT NULL COMMENT '指标日期',
    macd DECIMAL(10,4) COMMENT 'MACD指标',
    rsi DECIMAL(10,4) COMMENT 'RSI指标',
    kdj_k DECIMAL(10,4) COMMENT 'KDJ-K值',
    kdj_d DECIMAL(10,4) COMMENT 'KDJ-D值',
    kdj_j DECIMAL(10,4) COMMENT 'KDJ-J值',
    boll_upper DECIMAL(10,2) COMMENT '布林线上轨',
    boll_middle DECIMAL(10,2) COMMENT '布林线中轨',
    boll_lower DECIMAL(10,2) COMMENT '布林线下轨',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_stock_indicator_date (stock_id, indicator_date),
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='技术指标表';

-- 公司概况表
DROP TABLE IF EXISTS company_profile;
CREATE TABLE company_profile (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '公司ID',
    stock_id BIGINT NOT NULL UNIQUE COMMENT '股票ID',
    company_name VARCHAR(200) COMMENT '公司全称',
    description TEXT COMMENT '公司简介',
    industry VARCHAR(100) COMMENT '所属行业',
    sub_industry VARCHAR(100) COMMENT '细分行业',
    market_cap DECIMAL(20,2) COMMENT '总市值',
    pe_ratio DECIMAL(10,4) COMMENT '市盈率',
    pb_ratio DECIMAL(10,4) COMMENT '市净率',
    eps DECIMAL(10,4) COMMENT '每股收益',
    roe DECIMAL(10,4) COMMENT '净资产收益率(%)',
    revenue DECIMAL(20,2) COMMENT '营业收入',
    profit DECIMAL(20,2) COMMENT '净利润',
    address TEXT COMMENT '公司地址',
    ceo VARCHAR(100) COMMENT 'CEO姓名',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公司概况表';

-- 股东信息表
DROP TABLE IF EXISTS shareholder;
CREATE TABLE shareholder (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '股东ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    shareholder_name VARCHAR(200) NOT NULL COMMENT '股东名称',
    holding_shares BIGINT NOT NULL COMMENT '持股数量',
    holding_percentage DECIMAL(10,4) NOT NULL COMMENT '持股比例(%)',
    change_shares BIGINT COMMENT '变动股数',
    change_percentage DECIMAL(10,4) COMMENT '变动比例(%)',
    report_date DATE NOT NULL COMMENT '报告期',
    is_institutional TINYINT DEFAULT 0 COMMENT '是否机构投资者:0-否,1-是',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_stock_report (stock_id, report_date),
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='股东信息表';

-- 持股结构表
DROP TABLE IF EXISTS shareholding_structure;
CREATE TABLE shareholding_structure (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '结构ID',
    stock_id BIGINT NOT NULL UNIQUE COMMENT '股票ID',
    total_shares BIGINT NOT NULL COMMENT '总股本',
    float_shares BIGINT COMMENT '流通股本',
    major_shareholder_count INT COMMENT '主要股东数量',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='持股结构表';

-- ============================================================
-- 3. 用户投资偏好与自选股模块（2张表）
-- ============================================================

-- 投资偏好表 - 增强版：支持用户自定义投资期限
DROP TABLE IF EXISTS investment_preference;
CREATE TABLE investment_preference (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '偏好ID',
    user_id BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    risk_tolerance_level TINYINT NOT NULL CHECK (risk_tolerance_level BETWEEN 1 AND 5) COMMENT '风险承受能力:1-保守,2-稳健,3-平衡,4-积极,5-激进',
    investment_horizon_type VARCHAR(20) NOT NULL DEFAULT 'preset' COMMENT '期限类型:preset-预设(短期/中期/长期),custom-自定义',
    investment_horizon_preset VARCHAR(20) COMMENT '预设投资期限:short-短期(1-3月),medium-中期(3-12月),long-长期(1年以上)',
    investment_horizon_custom_days INT COMMENT '自定义投资期限(天数)',
    investment_horizon_custom_months INT COMMENT '自定义投资期限(月数)',
    investment_horizon_custom_years INT COMMENT '自定义投资期限(年数)',
    investment_horizon_display VARCHAR(50) COMMENT '投资期限显示文本(如"6个月"、"2年")',
    capital_amount DECIMAL(15,2) COMMENT '投资金额',
    preferred_asset_classes VARCHAR(255) COMMENT '偏好资产类别',
    preferred_industry VARCHAR(255) COMMENT '偏好行业',
    min_expected_return DECIMAL(6,2) COMMENT '最低预期收益率(%)',
    max_acceptable_loss DECIMAL(6,2) COMMENT '最大可接受亏损(%)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE,
    CHECK (
        (investment_horizon_type = 'preset' AND investment_horizon_preset IS NOT NULL) OR
        (investment_horizon_type = 'custom' AND (investment_horizon_custom_days IS NOT NULL OR investment_horizon_custom_months IS NOT NULL OR investment_horizon_custom_years IS NOT NULL))
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资偏好表-支持预设和自定义投资期限';

-- 用户自选股表
DROP TABLE IF EXISTS user_favorite;
CREATE TABLE user_favorite (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '自选ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    stock_id BIGINT NOT NULL COMMENT '股票ID',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
    UNIQUE KEY uk_user_stock (user_id, stock_id),
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE,
    FOREIGN KEY (stock_id) REFERENCES stock_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自选股表';

-- ============================================================
-- 4. AI模型与提示词管理模块（2张表）
-- ============================================================

-- AI模型配置表（核心创新功能）
DROP TABLE IF EXISTS ai_model_config;
CREATE TABLE ai_model_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '模型配置ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    model_name VARCHAR(50) NOT NULL COMMENT '模型名称(DeepSeek/GPT-4/文心/通义)',
    model_provider VARCHAR(50) NOT NULL COMMENT '服务提供商',
    api_key VARCHAR(200) NOT NULL COMMENT 'API密钥',
    api_endpoint VARCHAR(200) NOT NULL COMMENT 'API端点URL',
    model_type VARCHAR(20) DEFAULT 'chat' COMMENT '模型类型:chat/completion',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认:0-否,1-是',
    status TINYINT DEFAULT 1 COMMENT '状态:0-禁用,1-启用',
    max_tokens INT DEFAULT 2000 COMMENT '最大Token数',
    temperature DECIMAL(3,2) DEFAULT 0.70 COMMENT '温度参数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI模型配置表';

-- AI提示词表
DROP TABLE IF EXISTS ai_prompt;
CREATE TABLE ai_prompt (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '提示词ID',
    user_id BIGINT COMMENT '创建用户ID(NULL表示系统默认)',
    title VARCHAR(100) NOT NULL COMMENT '提示词标题',
    content TEXT NOT NULL COMMENT '提示词内容',
    category VARCHAR(50) COMMENT '分类:股票推荐/买卖建议/风险评估',
    version VARCHAR(20) DEFAULT 'v1.0' COMMENT '版本号',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用:0-否,1-是',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统默认:0-否,1-是',
    usage_count INT DEFAULT 0 COMMENT '使用次数',
    avg_satisfaction DECIMAL(3,2) COMMENT '平均满意度(0-5分)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_category (category),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI提示词表';

-- ============================================================
-- 5. 投资建议与回测模块（3张表）
-- ============================================================

-- 投资建议表
DROP TABLE IF EXISTS investment_advice;
CREATE TABLE investment_advice (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '建议ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    prompt_id BIGINT COMMENT '使用的提示词ID',
    model_config_id BIGINT COMMENT '使用的AI模型ID',
    title VARCHAR(200) NOT NULL COMMENT '建议标题',
    content TEXT NOT NULL COMMENT '建议内容(JSON格式存储推荐股票列表)',
    reasoning TEXT COMMENT '建议理由',
    risk_assessment TEXT COMMENT '风险评估',
    recommended_stocks VARCHAR(500) COMMENT '推荐股票代码列表(逗号分隔)',
    target_return_rate DECIMAL(10,4) COMMENT '目标收益率(%)',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效:0-无效,1-有效',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读:0-未读,1-已读',
    user_rating TINYINT COMMENT '用户评分(1-5)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '生成时间',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_user_id (user_id),
    INDEX idx_created_at (created_at),
    INDEX idx_user_created (user_id, created_at DESC),
    FOREIGN KEY (user_id) REFERENCES user_info(id) ON DELETE CASCADE,
    FOREIGN KEY (prompt_id) REFERENCES ai_prompt(id) ON DELETE SET NULL,
    FOREIGN KEY (model_config_id) REFERENCES ai_model_config(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='投资建议表';

-- 回测结果表
DROP TABLE IF EXISTS backtest_result;
CREATE TABLE backtest_result (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '回测ID',
    advice_id BIGINT NOT NULL COMMENT '投资建议ID',
    prompt_id BIGINT COMMENT '使用的提示词ID',
    total_return DECIMAL(10,4) COMMENT '总收益率(%)',
    annualized_return DECIMAL(10,4) COMMENT '年化收益率(%)',
    max_drawdown DECIMAL(10,4) COMMENT '最大回撤(%)',
    sharpe_ratio DECIMAL(10,4) COMMENT '夏普比率',
    win_rate DECIMAL(5,2) COMMENT '胜率(%)',
    volatility DECIMAL(10,4) COMMENT '波动率(%)',
    backtest_start_date DATE NOT NULL COMMENT '回测开始日期',
    backtest_end_date DATE NOT NULL COMMENT '回测结束日期',
    backtest_duration INT COMMENT '回测天数',
    is_success TINYINT DEFAULT 0 COMMENT '是否达标:0-未达标,1-达标',
    failure_reason VARCHAR(200) COMMENT '未达标原因',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_advice_id (advice_id),
    INDEX idx_advice_created (advice_id, created_at DESC),
    FOREIGN KEY (advice_id) REFERENCES investment_advice(id) ON DELETE CASCADE,
    FOREIGN KEY (prompt_id) REFERENCES ai_prompt(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='回测结果表';

-- 提示词迭代日志表（核心创新功能-自动优化闭环）
DROP TABLE IF EXISTS prompt_iteration_log;
CREATE TABLE prompt_iteration_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '迭代日志ID',
    advice_id BIGINT NOT NULL COMMENT '投资建议ID',
    backtest_id BIGINT COMMENT '回测结果ID',
    iteration_round TINYINT NOT NULL COMMENT '迭代轮次(1-5)',
    original_prompt_id BIGINT COMMENT '原始提示词ID',
    optimized_prompt_content TEXT COMMENT '优化后的提示词内容',
    backtest_return_rate DECIMAL(10,4) COMMENT '回测收益率(%)',
    target_return_rate DECIMAL(10,4) COMMENT '目标收益率阈值(%)',
    is_success TINYINT DEFAULT 0 COMMENT '是否达标:0-未达标,1-达标',
    optimization_strategy VARCHAR(100) COMMENT '优化策略',
    failure_analysis TEXT COMMENT '失败原因分析',
    improvement_points TEXT COMMENT '改进要点',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_advice_id (advice_id),
    INDEX idx_iteration_round (iteration_round),
    FOREIGN KEY (advice_id) REFERENCES investment_advice(id) ON DELETE CASCADE,
    FOREIGN KEY (backtest_id) REFERENCES backtest_result(id) ON DELETE SET NULL,
    FOREIGN KEY (original_prompt_id) REFERENCES ai_prompt(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='提示词迭代日志表-支持AI自动优化闭环';

-- ============================================================
-- 6. 初始化数据
-- ============================================================

-- 初始化测试用户（密码为 123456 的BCrypt加密）
INSERT INTO user_info (username, password, email, phone_number, status) VALUES
('testuser', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', 'test@liangro.com', '13800138001', 1);

-- 初始化系统默认AI提示词
INSERT INTO ai_prompt (user_id, title, content, category, version, is_active, is_system) VALUES
(NULL, '稳健型股票推荐', '你是一位专业的投资顾问。请根据用户的投资偏好，推荐5-10只适合稳健型投资者的A股股票。要求：1) 选择行业龙头或业绩稳定的蓝筹股 2) 市盈率在15-30之间 3) 近3年ROE>10% 4) 负债率<60% 5) 行业分散，避免过度集中。请以JSON格式返回，包含股票代码、名称、推荐理由、风险提示。', '股票推荐', 'v1.0', 1, 1),
(NULL, '成长型股票推荐', '你是一位专业的投资顾问。请根据用户的投资偏好，推荐5-10只具有成长潜力的A股股票。要求：1) 选择新兴行业或高成长行业 2) 营收增长率>20% 3) 净利润增长率>15% 4) 研发投入占比>5% 5) 适当控制估值风险。请以JSON格式返回，包含股票代码、名称、推荐理由、风险提示。', '股票推荐', 'v1.0', 1, 1),
(NULL, '价值型股票推荐', '你是一位专业的投资顾问。请根据用户的投资偏好，推荐5-10只被低估的价值型A股股票。要求：1) 市净率<2 2) 市盈率<15 3) 股息率>3% 4) 经营现金流稳定 5) 所处行业竞争格局稳定。请以JSON格式返回，包含股票代码、名称、推荐理由、风险提示。', '股票推荐', 'v1.0', 1, 1);

-- ============================================================
-- 7. 创建视图（方便查询）
-- ============================================================

-- 用户自选股详情视图
CREATE OR REPLACE VIEW v_user_favorite_detail AS
SELECT
    uf.id AS favorite_id,
    uf.user_id,
    u.username,
    uf.stock_id,
    s.stock_code,
    s.stock_name,
    s.current_price,
    s.yesterday_close,
    s.change_percent,
    s.industry,
    uf.sort_order,
    uf.remark,
    uf.created_at AS added_date
FROM user_favorite uf
JOIN user_info u ON uf.user_id = u.id
JOIN stock_info s ON uf.stock_id = s.id
WHERE u.status = 1 AND s.status = 1;

-- 投资建议详情视图
CREATE OR REPLACE VIEW v_investment_advice_detail AS
SELECT
    ia.id AS advice_id,
    ia.user_id,
    u.username,
    ia.title,
    ia.content,
    ia.reasoning,
    ia.recommended_stocks,
    ia.target_return_rate,
    p.title AS prompt_title,
    m.model_name,
    ia.user_rating,
    ia.is_read,
    ia.created_at
FROM investment_advice ia
JOIN user_info u ON ia.user_id = u.id
LEFT JOIN ai_prompt p ON ia.prompt_id = p.id
LEFT JOIN ai_model_config m ON ia.model_config_id = m.id
WHERE ia.is_valid = 1;

-- 回测结果汇总视图
CREATE OR REPLACE VIEW v_backtest_summary AS
SELECT
    br.id AS backtest_id,
    br.advice_id,
    ia.title AS advice_title,
    ia.user_id,
    u.username,
    br.total_return,
    br.annualized_return,
    br.max_drawdown,
    br.sharpe_ratio,
    br.win_rate,
    br.backtest_start_date,
    br.backtest_end_date,
    br.backtest_duration,
    br.is_success,
    br.created_at
FROM backtest_result br
JOIN investment_advice ia ON br.advice_id = ia.id
JOIN user_info u ON ia.user_id = u.id;

-- ============================================================
-- 8. 创建存储过程（常用操作）
-- ============================================================

-- 添加自选股存储过程
DELIMITER $$
CREATE PROCEDURE sp_add_favorite(
    IN p_user_id BIGINT,
    IN p_stock_code VARCHAR(20)
)
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
END$$
DELIMITER ;

-- 获取股票最新K线数据存储过程
DELIMITER $$
CREATE PROCEDURE sp_get_latest_kline(
    IN p_stock_code VARCHAR(20),
    IN p_days INT
)
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
END$$
DELIMITER ;

-- ============================================================
-- 9. 创建触发器（自动化操作）
-- ============================================================

-- AI提示词使用次数自动更新触发器
DELIMITER $$
CREATE TRIGGER tr_prompt_usage_increment
AFTER INSERT ON investment_advice
FOR EACH ROW
BEGIN
    IF NEW.prompt_id IS NOT NULL THEN
        UPDATE ai_prompt
        SET usage_count = usage_count + 1
        WHERE id = NEW.prompt_id;
    END IF;
END$$
DELIMITER ;

-- 用户自选股排序自动调整触发器
DELIMITER $$
CREATE TRIGGER tr_favorite_sort_order
BEFORE INSERT ON user_favorite
FOR EACH ROW
BEGIN
    DECLARE v_max_order INT;

    SELECT COALESCE(MAX(sort_order), 0) INTO v_max_order
    FROM user_favorite
    WHERE user_id = NEW.user_id;

    IF NEW.sort_order IS NULL OR NEW.sort_order = 0 THEN
        SET NEW.sort_order = v_max_order + 1;
    END IF;
END$$
DELIMITER ;

-- ============================================================
-- 10. 数据库版本管理
-- ============================================================

-- 创建数据库版本管理表
CREATE TABLE IF NOT EXISTS db_version (
    id INT PRIMARY KEY AUTO_INCREMENT,
    version VARCHAR(20) NOT NULL,
    description VARCHAR(200),
    applied_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据库版本管理表';

INSERT INTO db_version (version, description) VALUES
('1.0.0', '初始化数据库-精简版(11张核心表)'),
('1.1.0', '增强版-添加新浪财经API所需字段');

-- ============================================================
-- 脚本执行完成提示
-- ============================================================
SELECT '========================================' AS '';
SELECT '✅ 数据库 finance_db 初始化完成 (合并版)！' AS '状态';
SELECT '========================================' AS '';
SELECT '包含表数量: 11张核心表' AS '📊 统计信息';
SELECT '包含视图: 3个' AS '';
SELECT '包含存储过程: 2个' AS '';
SELECT '包含触发器: 2个' AS '';
SELECT '初始化用户: 1个测试用户' AS '';
SELECT '初始化提示词: 3个系统默认提示词' AS '';
SELECT '========================================' AS '';
SELECT '测试用户登录信息：' AS '🔑 登录信息';
SELECT '用户名: testuser' AS '';
SELECT '密码: 123456' AS '';
SELECT '========================================' AS '';
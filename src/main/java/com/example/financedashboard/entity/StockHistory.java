package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票历史数据实体类（K线数据）
 * 对应数据库表: stock_history
 * 用于存储股票的历史交易数据和技术指标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockHistory {
    private Long id;                    // 历史数据ID，主键
    private String stockCode;           // 股票代码（新增，方便直接查询）
    private Long stockId;               // 股票ID，外键关联stock_info表
    private LocalDate tradeDate;        // 交易日期
    private BigDecimal openPrice;       // 开盘价
    private BigDecimal highPrice;       // 最高价
    private BigDecimal lowPrice;        // 最低价
    private BigDecimal closePrice;      // 收盘价
    private Long volume;                // 成交量(股)
    private BigDecimal amount;          // 成交额(元)，原turnover改名为amount
    private BigDecimal changeAmount;    // 涨跌额（新增）
    private BigDecimal changePercent;   // 涨跌幅(%)
    private BigDecimal ma5;             // 5日均线
    private BigDecimal ma10;            // 10日均线
    private BigDecimal ma20;            // 20日均线
    private BigDecimal ma60;            // 60日均线
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间（新增）
}
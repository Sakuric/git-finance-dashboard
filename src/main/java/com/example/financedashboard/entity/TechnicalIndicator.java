package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 技术指标实体类
 * 对应数据库表: technical_indicators
 * 用于存储股票的技术分析指标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TechnicalIndicator {
    private Long id;                    // 指标ID，主键
    private Long stockId;               // 股票ID，外键关联stock_info表
    private LocalDate indicatorDate;    // 指标日期
    private BigDecimal macd;            // MACD指标
    private BigDecimal rsi;             // RSI指标
    private BigDecimal kdjK;            // KDJ-K值
    private BigDecimal kdjD;            // KDJ-D值
    private BigDecimal kdjJ;            // KDJ-J值
    private BigDecimal bollUpper;       // 布林线上轨
    private BigDecimal bollMiddle;      // 布林线中轨
    private BigDecimal bollLower;       // 布林线下轨
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股票信息实体类
 * 对应数据库表: stock_info
 * 用于存储股票的基本信息和实时交易数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockInfo {
    private Long id;                      // 股票ID，主键
    private String stockCode;             // 股票代码（如sh600000）
    private String stockSymbol;           // 股票简称（如600000）
    private String stockName;             // 股票名称（如浦发银行）
    private String exchange;              // 交易所：SH-上交所，SZ-深交所
    private String industry;              // 所属行业
    private String sector;                // 所属板块
    private LocalDate listingDate;        // 上市日期
    private BigDecimal currentPrice;      // 当前价格
    private BigDecimal yesterdayClose;    // 昨收价
    private BigDecimal changePercent;     // 涨跌幅(%)
    private BigDecimal marketValue;       // 总市值(元)
    private Integer status;               // 状态：0-停牌，1-正常交易
    private LocalDateTime lastUpdateTime; // 最后更新时间
    private LocalDateTime createdAt;      // 创建时间
    private LocalDateTime updatedAt;      // 更新时间
}
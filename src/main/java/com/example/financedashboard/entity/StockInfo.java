package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 股票信息实体类
 * 用于存储股票的基本信息和实时交易数据
 * Stock（股票）是指股份有限公司的所有权凭证，是股份公司为筹集资金而发行给各个股东作为持股凭证并借以取得股息和红利的一种有价证券
 */
@Data  // Lombok注解，自动生成getter、setter等方法
@AllArgsConstructor  // Lombok注解，自动生成全参数构造函数
@NoArgsConstructor   // Lombok注解，自动生成无参构造函数
public class StockInfo {
    private Long id;                    // 股票信息ID，主键，自增长
    private String stockCode;            // 股票代码，如"000001"、"600000"等，是股票在交易所中的唯一标识
    private String stockName;            // 股票名称，如"平安银行"、"浦发银行"等，是公司的简称
    private String exchange;             // 交易所，如"SSE"（上海证券交易所）、"SZSE"（深圳证券交易所）等
    private String industry;             // 所属行业，如"银行"、"房地产"、"科技"等，用于分类和筛选
    private BigDecimal currentPrice;     // 当前价格，股票的最新交易价格，使用BigDecimal确保精度
    private BigDecimal openPrice;        // 开盘价，当日股票的开盘价格
    private BigDecimal highPrice;        // 最高价，当日股票的最高交易价格
    private BigDecimal lowPrice;         // 最低价，当日股票的最低交易价格
    private BigDecimal preClose;        // 昨收价，前一交易日的收盘价格，用于计算涨跌幅
    private Long volume;                // 成交量，当日股票的交易数量，单位通常是"股"或"手"
    private BigDecimal amount;          // 成交额，当日股票的交易金额，单位通常是"元"，使用BigDecimal确保精度
    private LocalDateTime updateTime;    // 更新时间，记录该股票信息最后一次更新的时间
}
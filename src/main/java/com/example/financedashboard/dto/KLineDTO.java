package com.example.financedashboard.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * K线数据DTO
 * 用于接收和传递从API获取的K线数据
 */
@Data
public class KLineDTO {
    /** 股票代码 */
    private String stockCode;
    
    /** 交易日期 */
    private LocalDate tradeDate;
    
    /** 开盘价 */
    private BigDecimal openPrice;
    
    /** 收盘价 */
    private BigDecimal closePrice;
    
    /** 最高价 */
    private BigDecimal highPrice;
    
    /** 最低价 */
    private BigDecimal lowPrice;
    
    /** 成交量(股) */
    private Long volume;
    
    /** 成交额(元) */
    private BigDecimal amount;
    
    /** 振幅(%) */
    private BigDecimal amplitude;
    
    /** 涨跌幅(%) */
    private BigDecimal changePercent;
    
    /** 涨跌额 */
    private BigDecimal changeAmount;
    
    /** 换手率(%) */
    private BigDecimal turnoverRate;
    
    /** 5日均线 */
    private BigDecimal ma5;
    
    /** 10日均线 */
    private BigDecimal ma10;
    
    /** 20日均线 */
    private BigDecimal ma20;
    
    /** 60日均线 */
    private BigDecimal ma60;
}
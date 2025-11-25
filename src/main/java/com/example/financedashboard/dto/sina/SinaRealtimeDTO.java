package com.example.financedashboard.dto.sina;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 新浪实时行情数据DTO
 * 用于接收和传递从新浪财经API获取的实时股票数据
 */
@Data
public class SinaRealtimeDTO {
    
    /** 股票代码（不含市场前缀，如：600519） */
    private String stockCode;
    
    /** 股票名称 */
    private String stockName;
    
    /** 今日开盘价 */
    private BigDecimal openPrice;
    
    /** 昨日收盘价 */
    private BigDecimal preClosePrice;
    
    /** 当前价格 */
    private BigDecimal currentPrice;
    
    /** 今日最高价 */
    private BigDecimal highPrice;
    
    /** 今日最低价 */
    private BigDecimal lowPrice;
    
    /** 成交量（股） */
    private Long volume;
    
    /** 成交额（元） */
    private BigDecimal amount;
    
    /** 涨跌额 */
    private BigDecimal change;
    
    /** 涨跌幅（%） */
    private BigDecimal changePercent;
    
    /** 数据时间 */
    private LocalDateTime dataTime;
    
    /** 原始数据（用于调试和日志） */
    private String rawData;
}
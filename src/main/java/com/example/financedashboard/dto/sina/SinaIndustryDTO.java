package com.example.financedashboard.dto.sina;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 新浪/东方财富行业板块信息DTO
 * 用于存储股票的详细信息
 */
@Data
public class SinaIndustryDTO {
    
    /**
     * 股票代码
     */
    private String stockCode;
    
    /**
     * 股票名称
     */
    private String stockName;
    
    /**
     * 所属行业
     */
    private String industry;
    
    /**
     * 所属板块/概念
     */
    private String sector;
    
    /**
     * 总市值（单位：元）
     */
    private BigDecimal marketValue;
    
    /**
     * 上市日期
     */
    private LocalDate listingDate;
    
    /**
     * 原始数据（用于调试）
     */
    private String rawData;
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公司概况实体类
 * 对应数据库表: company_profile
 * 用于存储上市公司的基本信息和财务指标
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CompanyProfile {
    private Long id;                    // 公司ID，主键
    private String stockCode;           // 股票代码
    private String companyName;         // 公司全称
    private String description;         // 公司简介
    private String industry;            // 所属行业
    private BigDecimal marketCap;       // 总市值
    private BigDecimal peRatio;         // 市盈率
    private BigDecimal pbRatio;         // 市净率
    private BigDecimal eps;             // 每股收益
    private BigDecimal roe;             // 净资产收益率(%)
    private BigDecimal revenue;         // 营业收入
    private BigDecimal profit;          // 净利润
    private String address;             // 公司地址
    private String ceo;                 // CEO姓名
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 股东信息实体类
 * 对应数据库表: shareholder
 * 用于存储上市公司的股东持股信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Shareholder {
    private Long id;                        // 股东ID，主键
    private Long stockId;                   // 股票ID，外键关联stock_info表
    private String shareholderName;         // 股东名称
    private Long holdingShares;             // 持股数量
    private BigDecimal holdingPercentage;   // 持股比例(%)
    private Long changeShares;              // 变动股数
    private BigDecimal changePercentage;    // 变动比例(%)
    private LocalDate reportDate;           // 报告期
    private Integer isInstitutional;        // 是否机构投资者：0-否，1-是
    private LocalDateTime createdAt;        // 创建时间
}
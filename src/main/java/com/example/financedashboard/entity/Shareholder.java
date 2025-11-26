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
    private String stockCode;               // 股票代码
    private String shareholderName;         // 股东名称
    private Long holdingQuantity;           // 持股数量
    private BigDecimal holdingRatio;        // 持股比例(%)
    private String change;                  // 变动情况
    private String shareholderType;         // 股东类型
    private LocalDate reportDate;           // 报告期
    private LocalDateTime createdAt;        // 创建时间
    private LocalDateTime updatedAt;        // 更新时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 持股结构实体类
 * 对应数据库表: shareholding_structure
 * 用于存储上市公司的股本结构信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShareholdingStructure {
    private Long id;                        // 结构ID，主键
    private String stockCode;               // 股票代码
    private Long totalShares;               // 总股本
    private Long circulatingShares;         // 流通股本
    private Integer majorShareholderCount;  // 主要股东数量
    private LocalDateTime createdAt;        // 创建时间
    private LocalDateTime updatedAt;        // 更新时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户自选股实体类
 * 对应数据库表: user_favorite
 * 用于存储用户收藏的股票列表
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserFavorite {
    private Long id;
    private Long userId;
    private Long stockId;
    private Integer sortOrder;
    private String remark;
    private LocalDateTime createdAt;

    private String stockCode;
    private String stockName;
    private String exchange;
    private Double currentPrice;
    private Double changePercent;
    private Long volume;
}
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
    private Long id;                    // 自选ID，主键
    private Long userId;                // 用户ID，外键关联user_info表
    private Long stockId;               // 股票ID，外键关联stock_info表
    private Integer sortOrder;          // 排序序号
    private String remark;              // 备注
    private LocalDateTime createdAt;    // 添加时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * AI提示词实体类
 * 对应数据库表: ai_prompt
 * 用于存储用户自定义或系统默认的AI提示词模板
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AiPrompt {
    private Long id;                    // 提示词ID，主键
    private Long userId;                // 创建用户ID（NULL表示系统默认）
    private String title;               // 提示词标题
    private String content;             // 提示词内容
    private String category;            // 分类：股票推荐/买卖建议/风险评估
    private String version;             // 版本号
    private Integer isActive;           // 是否启用：0-否，1-是
    private Integer isSystem;           // 是否系统默认：0-否，1-是
    private Integer usageCount;         // 使用次数
    private BigDecimal avgSatisfaction; // 平均满意度（0-5分）
    private LocalDateTime createdAt;    // 创建时间
    private LocalDateTime updatedAt;    // 更新时间
}
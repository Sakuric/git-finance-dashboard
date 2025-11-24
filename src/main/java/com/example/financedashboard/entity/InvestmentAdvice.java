package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资建议实体类
 * 对应数据库表: investment_advice
 * 用于存储AI生成的投资建议信息
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentAdvice {
    private Long id;                        // 建议ID，主键
    private Long userId;                    // 用户ID，外键关联user_info表
    private Long promptId;                  // 使用的提示词ID，外键关联ai_prompt表
    private Long modelConfigId;             // 使用的AI模型ID，外键关联ai_model_config表
    private String title;                   // 建议标题
    private String content;                 // 建议内容（JSON格式存储推荐股票列表）
    private String reasoning;               // 建议理由
    private String riskAssessment;          // 风险评估
    private String recommendedStocks;       // 推荐股票代码列表（逗号分隔）
    private BigDecimal targetReturnRate;    // 目标收益率(%)
    private Integer isValid;                // 是否有效：0-无效，1-有效
    private Integer isRead;                 // 是否已读：0-未读，1-已读
    private Integer userRating;             // 用户评分（1-5）
    private LocalDateTime createdAt;        // 生成时间
    private LocalDateTime updatedAt;        // 更新时间
}
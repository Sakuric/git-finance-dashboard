package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 投资偏好实体类
 * 对应数据库表: investment_preference
 * 用于存储用户的投资偏好设置和风险承受能力
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InvestmentPreference {
    private Long id;                        // 偏好ID，主键
    private Long userId;                    // 用户ID，外键关联user_info表，唯一
    private Integer riskToleranceLevel;     // 风险承受能力：1-保守，2-稳健，3-平衡，4-积极，5-激进
    private String investmentHorizon;       // 投资期限：短期/中期/长期
    private BigDecimal capitalAmount;       // 投资金额
    private String preferredAssetClasses;   // 偏好资产类别
    private String preferredIndustry;       // 偏好行业
    private BigDecimal minExpectedReturn;   // 最低预期收益率(%)
    private BigDecimal maxAcceptableLoss;   // 最大可接受亏损(%)
    private LocalDateTime createdAt;        // 创建时间
    private LocalDateTime updatedAt;        // 更新时间
}
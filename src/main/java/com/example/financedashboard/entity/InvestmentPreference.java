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
    private Long id;
    private Long userId;
    private Integer riskToleranceLevel;
    private String investmentHorizonType;
    private String investmentHorizonPreset;
    private Integer investmentHorizonCustomDays;
    private Integer investmentHorizonCustomMonths;
    private Integer investmentHorizonCustomYears;
    private String investmentHorizonDisplay;
    private BigDecimal capitalAmount;
    private String preferredAssetClasses;
    private String preferredIndustry;
    private BigDecimal minExpectedReturn;
    private BigDecimal maxAcceptableLoss;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
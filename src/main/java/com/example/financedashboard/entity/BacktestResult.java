package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 回测结果实体类
 * 对应数据库表: backtest_result
 * 用于存储投资建议的回测结果数据
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BacktestResult {
    private Long id;                        // 回测ID，主键
    private Long adviceId;                  // 投资建议ID，外键关联investment_advice表
    private Long promptId;                  // 使用的提示词ID，外键关联ai_prompt表
    private BigDecimal totalReturn;         // 总收益率(%)
    private BigDecimal annualizedReturn;    // 年化收益率(%)
    private BigDecimal maxDrawdown;         // 最大回撤(%)
    private BigDecimal sharpeRatio;         // 夏普比率
    private BigDecimal winRate;             // 胜率(%)
    private BigDecimal volatility;          // 波动率(%)
    private LocalDate backtestStartDate;    // 回测开始日期
    private LocalDate backtestEndDate;      // 回测结束日期
    private Integer backtestDuration;       // 回测天数
    private Integer isSuccess;              // 是否达标：0-未达标，1-达标
    private String failureReason;           // 未达标原因
    private LocalDateTime createdAt;        // 创建时间
}
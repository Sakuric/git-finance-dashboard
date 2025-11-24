package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 提示词迭代日志实体类
 * 对应数据库表: prompt_iteration_log
 * 用于存储AI提示词自动优化迭代的日志记录
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromptIterationLog {
    private Long id;                            // 迭代日志ID，主键
    private Long adviceId;                      // 投资建议ID，外键关联investment_advice表
    private Long backtestId;                    // 回测结果ID，外键关联backtest_result表
    private Integer iterationRound;             // 迭代轮次（1-5）
    private Long originalPromptId;              // 原始提示词ID，外键关联ai_prompt表
    private String optimizedPromptContent;      // 优化后的提示词内容
    private BigDecimal backtestReturnRate;      // 回测收益率(%)
    private BigDecimal targetReturnRate;        // 目标收益率阈值(%)
    private Integer isSuccess;                  // 是否达标：0-未达标，1-达标
    private String optimizationStrategy;        // 优化策略
    private String failureAnalysis;             // 失败原因分析
    private String improvementPoints;           // 改进要点
    private LocalDateTime createdAt;            // 创建时间
}
package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockBacktestDetail {
    private Long id;
    private Long backtestId;
    private Long adviceId;
    private String backtestPeriod;
    private String stockCode;
    private String stockName;
    private String suggestedAction;
    private BigDecimal totalReturn;
    private BigDecimal maxDrawdown;
    private Integer tradeCount;
    private Integer winCount;
    private BigDecimal entryPrice;
    private BigDecimal exitPrice;
    private String exitReason;
    private String noTradeReason;
    private String riskLevel;
    private LocalDateTime createdAt;
}

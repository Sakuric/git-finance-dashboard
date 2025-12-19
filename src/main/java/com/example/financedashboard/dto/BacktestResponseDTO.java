package com.example.financedashboard.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class BacktestResponseDTO {
    private Long backtestId;
    private PeriodResult trainPeriod;
    private PeriodResult testPeriod;
    private OverfittingDetection overfitting;
    private List<TradeDetail> trades;
    private List<EquityPoint> equityCurve;

    @Data
    public static class PeriodResult {
        private String period;
        private BigDecimal totalReturn;
        private BigDecimal annualReturn;
        private BigDecimal maxDrawdown;
        private BigDecimal sharpeRatio;
        private BigDecimal winRate;
        private Integer tradeCount;
        private List<EquityPoint> equityCurve;
    }

    @Data
    public static class OverfittingDetection {
        private Boolean isOverfitted;
        private BigDecimal performanceDegradation;
        private BigDecimal stabilityScore;
        private String warning;
    }

    @Data
    public static class TradeDetail {
        private String date;
        private String stockCode;
        private String stockName;
        private String action;
        private BigDecimal price;
        private Integer quantity;
        private BigDecimal returnRate;
        private String reason;
    }

    @Data
    public static class EquityPoint {
        private String date;
        private BigDecimal value;
    }
}

package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StructuredAdvice {
    private Long id;
    private Long adviceId;
    private String stockCode;
    private String stockName;
    private String suggestedAction;
    private String thesis;
    private BigDecimal entryPriceStart;
    private BigDecimal entryPriceEnd;
    private BigDecimal takeProfitPrice;
    private BigDecimal stopLossPrice;
    private Integer adviceEffectiveDays;
    private LocalDateTime createdAt;
}

package com.example.financedashboard.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class BacktestRequestDTO {
    private Long adviceId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String splitMethod = "train-test";  // train-test, walk-forward
    private BigDecimal trainRatio = new BigDecimal("0.7");
    private BigDecimal initialCapital = new BigDecimal("100000");
}

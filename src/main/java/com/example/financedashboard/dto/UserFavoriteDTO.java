package com.example.financedashboard.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class UserFavoriteDTO {
    private Long id;
    private Long userId;
    private String stockCode;
    private String stockName;
    private String exchange;
    private String industry;
    private BigDecimal currentPrice;
    private BigDecimal changePercent;
    private BigDecimal amount;
    private String remark;
    private LocalDateTime createdAt;
}

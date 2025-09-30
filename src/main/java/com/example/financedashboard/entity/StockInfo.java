package com.example.financedashboard.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StockInfo {
    private long id;
    private String stock_num;
    private String companyName;
    private BigDecimal currentPrice;
    private Date updateAt;
}

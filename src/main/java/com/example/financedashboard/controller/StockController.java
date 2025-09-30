package com.example.financedashboard.controller;


import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {
    @Autowired
    private StockService stockService;
    @RequestMapping
    public List<StockInfo> getAllStockInfo() {
        return stockService.getAllStockInfo();
    }
}

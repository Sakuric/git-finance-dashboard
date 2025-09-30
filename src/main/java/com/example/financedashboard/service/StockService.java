package com.example.financedashboard.service;

import com.example.financedashboard.dto.StockQueryDTO;
import com.example.financedashboard.entity.StockInfo;

import java.util.List;

public interface StockService {
    List<StockInfo> getAllStockInfo();
    StockInfo getStockByCode(String stockCode);
    boolean addStock(StockInfo stockInfo);
    boolean batchAddStocks(List<StockInfo> stockInfos);
    boolean batchUpsertStocks(List<StockInfo> stockInfos);
    boolean updateStock(StockInfo stockInfo);
    boolean deleteStock(String stockCode);
    List<StockInfo> queryStocks(StockQueryDTO queryDTO);
}

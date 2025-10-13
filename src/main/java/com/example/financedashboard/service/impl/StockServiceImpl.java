package com.example.financedashboard.service.impl;

import com.example.financedashboard.dto.StockQueryDTO;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.mapper.StockInfoMapper;
import com.example.financedashboard.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockServiceImpl implements StockService {
    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Override
    public List<StockInfo> getAllStockInfo() {
        return stockInfoMapper.findAll();
    }

    @Override
    public StockInfo getStockByCode(String stockCode) {
        return stockInfoMapper.findByStockCode(stockCode);
    }

    @Override
    public boolean addStock(StockInfo stockInfo) {
        stockInfo.setUpdateTime(LocalDateTime.now());
        return stockInfoMapper.insert(stockInfo) > 0;
    }

    @Override
    public boolean batchAddStocks(List<StockInfo> stockInfos) {
        if (stockInfos == null || stockInfos.isEmpty()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        // 检查重复的股票代码
        List<String> duplicateCodes = new java.util.ArrayList<>();
        List<String> existingCodes = new java.util.ArrayList<>();
        
        for (StockInfo stock : stockInfos) {
            stock.setUpdateTime(now);
            
            // 检查数据库中是否已存在
            StockInfo existingStock = stockInfoMapper.findByStockCode(stock.getStockCode());
            if (existingStock != null) {
                existingCodes.add(stock.getStockCode());
            }
            
            // 检查本次添加列表中是否有重复
            long count = stockInfos.stream()
                    .filter(s -> s.getStockCode().equals(stock.getStockCode()))
                    .count();
            if (count > 1) {
                duplicateCodes.add(stock.getStockCode());
            }
        }
        
        // 如果有重复，返回false
        if (!existingCodes.isEmpty() || !duplicateCodes.isEmpty()) {
            System.err.println("重复的股票代码 - 数据库中已存在: " + existingCodes);
            System.err.println("重复的股票代码 - 本次添加中重复: " + duplicateCodes);
            return false;
        }
        
        // 执行批量插入
        try {
            return stockInfoMapper.batchInsert(stockInfos) > 0;
        } catch (Exception e) {
            System.err.println("批量添加股票失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean updateStock(StockInfo stockInfo) {
        stockInfo.setUpdateTime(LocalDateTime.now());
        return stockInfoMapper.update(stockInfo) > 0;
    }

    @Override
    public boolean deleteStock(String stockCode) {
        return stockInfoMapper.deleteByStockCode(stockCode) > 0;
    }

    @Override
    public List<StockInfo> queryStocks(StockQueryDTO queryDTO) {
        List<StockInfo> allStocks = stockInfoMapper.findAll();

        // 简单的过滤逻辑
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().toLowerCase();
            allStocks.removeIf(stock ->
                    !stock.getStockCode().toLowerCase().contains(keyword) &&
                            !stock.getStockName().toLowerCase().contains(keyword));
        }

        if (StringUtils.hasText(queryDTO.getIndustry())) {
            allStocks.removeIf(stock ->
                    !queryDTO.getIndustry().equals(stock.getIndustry()));
        }

        return allStocks;
    }
    
    @Override
    public boolean batchUpsertStocks(List<StockInfo> stockInfos) {
        if (stockInfos == null || stockInfos.isEmpty()) {
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        int successCount = 0;
        int totalCount = stockInfos.size();
        
        for (StockInfo stock : stockInfos) {
            stock.setUpdateTime(now);
            
            try {
                // 检查股票是否已存在
                StockInfo existingStock = stockInfoMapper.findByStockCode(stock.getStockCode());
                
                if (existingStock != null) {
                    // 如果存在，更新
                    if (stockInfoMapper.update(stock) > 0) {
                        successCount++;
                    }
                } else {
                    // 如果不存在，插入
                    if (stockInfoMapper.insert(stock) > 0) {
                        successCount++;
                    }
                }
            } catch (Exception e) {
                System.err.println("处理股票 " + stock.getStockCode() + " 时出错: " + e.getMessage());
            }
        }
        
        System.out.println("批量upsert完成: 成功 " + successCount + "/" + totalCount);
        return successCount > 0;
    }
}

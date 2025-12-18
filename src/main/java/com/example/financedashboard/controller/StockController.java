package com.example.financedashboard.controller;

import com.example.financedashboard.dto.StockQueryDTO;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.service.StockService;
import com.example.financedashboard.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stocks")
public class StockController {

    @Autowired
    private StockService stockService;

    @GetMapping
    public Result<List<StockInfo>> getAllStockInfo() {
        List<StockInfo> stockInfos = stockService.getAllStockInfo();
        return Result.success(stockInfos);
    }

    @GetMapping("/{stockCode}")
    public Result<StockInfo> getStockByCode(@PathVariable String stockCode, HttpServletRequest request) {
        StockInfo stockInfo = stockService.getStockByCode(stockCode);
        if (stockInfo == null) {
            stockInfo = new StockInfo();
            stockInfo.setStockCode(stockCode);
            stockInfo.setStockName(stockCode);
        }
        return Result.success(stockInfo);
    }

    @PostMapping
    public Result<Boolean> addStock(@RequestBody StockInfo stockInfo) {
        boolean success = stockService.addStock(stockInfo);
        return success ? Result.success(true) : Result.error(500, "添加失败");
    }

    @PostMapping("/batch")
    public Result<Boolean> batchAddStocks(@RequestBody List<StockInfo> stockInfos) {
        boolean success = stockService.batchAddStocks(stockInfos);
        return success ? Result.success(true) : Result.error(500, "批量添加失败，可能存在重复的股票代码");
    }
    
    @PostMapping("/batch-upsert")
    public Result<Boolean> batchUpsertStocks(@RequestBody List<StockInfo> stockInfos) {
        boolean success = stockService.batchUpsertStocks(stockInfos);
        return success ? Result.success(true) : Result.error(500, "批量更新失败");
    }

    @PutMapping
    public Result<Boolean> updateStock(@RequestBody StockInfo stockInfo) {
        boolean success = stockService.updateStock(stockInfo);
        return success ? Result.success(true) : Result.error(500, "更新失败");
    }

    @DeleteMapping("/{stockCode}")
    public Result<Boolean> deleteStock(@PathVariable String stockCode) {
        boolean success = stockService.deleteStock(stockCode);
        return success ? Result.success(true) : Result.error(500, "删除失败");
    }

    @PostMapping("/query")
    public Result<List<StockInfo>> queryStocks(@RequestBody StockQueryDTO queryDTO) {
        List<StockInfo> stockInfos = stockService.queryStocks(queryDTO);
        return Result.success(stockInfos);
    }
}
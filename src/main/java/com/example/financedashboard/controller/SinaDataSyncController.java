package com.example.financedashboard.controller;

import com.example.financedashboard.service.sina.SinaDataPersistenceService;
import com.example.financedashboard.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 新浪数据同步控制器
 * 提供手动触发数据同步到数据库的接口
 */
@Slf4j
@RestController
@RequestMapping("/api/sina/sync")
public class SinaDataSyncController {

    @Autowired
    private SinaDataPersistenceService persistenceService;

    /**
     * 同步单个股票数据到数据库
     * 
     * @param stockCode 股票代码（如：600519）
     * @return 同步结果
     */
    @PostMapping("/single/{stockCode}")
    public Result<Map<String, Object>> syncSingleStock(@PathVariable String stockCode) {
        log.info("收到同步单个股票请求: {}", stockCode);

        try {
            long startTime = System.currentTimeMillis();
            boolean success = persistenceService.syncStockToDatabase(stockCode);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> data = new HashMap<>();
            data.put("stockCode", stockCode);
            data.put("success", success);
            data.put("duration", duration + "ms");
            data.put("message", success ? "同步成功" : "同步失败");

            if (success) {
                return Result.success(data);
            } else {
                return Result.error(500, "同步失败");
            }

        } catch (Exception e) {
            log.error("同步股票数据失败: {}", stockCode, e);
            return Result.error(500, "同步失败: " + e.getMessage());
        }
    }

    /**
     * 批量同步股票数据到数据库
     * 
     * @param stockCodes 股票代码列表
     * @return 同步结果
     */
    @PostMapping("/batch")
    public Result<Map<String, Object>> syncBatchStocks(@RequestBody List<String> stockCodes) {
        log.info("收到批量同步请求，数量: {}", stockCodes.size());

        try {
            long startTime = System.currentTimeMillis();
            int successCount = persistenceService.batchSyncStocksToDatabase(stockCodes);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> data = new HashMap<>();
            data.put("total", stockCodes.size());
            data.put("success", successCount);
            data.put("failed", stockCodes.size() - successCount);
            data.put("duration", duration + "ms");
            data.put("message", "批量同步完成");

            return Result.success(data);

        } catch (Exception e) {
            log.error("批量同步失败", e);
            return Result.error(500, "批量同步失败: " + e.getMessage());
        }
    }

    /**
     * 快速测试：同步几只常见股票
     * 
     * @return 同步结果
     */
    @PostMapping("/quick-test")
    public Result<Map<String, Object>> quickTest() {
        log.info("收到快速测试请求");

        // 测试几只常见股票
        List<String> testStocks = Arrays.asList(
                "600519", // 贵州茅台
                "000001", // 平安银行
                "600036"  // 招商银行
        );

        try {
            long startTime = System.currentTimeMillis();
            int successCount = persistenceService.batchSyncStocksToDatabase(testStocks);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> data = new HashMap<>();
            data.put("testStocks", testStocks);
            data.put("total", testStocks.size());
            data.put("success", successCount);
            data.put("failed", testStocks.size() - successCount);
            data.put("duration", duration + "ms");
            data.put("message", "快速测试完成");

            return Result.success(data);

        } catch (Exception e) {
            log.error("快速测试失败", e);
            return Result.error(500, "快速测试失败: " + e.getMessage());
        }
    }

    /**
     * 同步数据库中所有股票的最新数据
     * 
     * @return 同步结果
     */
    @PostMapping("/all")
    public Result<Map<String, Object>> syncAllStocks() {
        log.info("收到同步所有股票请求");

        try {
            // 获取数据库中的所有股票代码
            List<String> stockCodes = persistenceService.getAllStockCodesFromDatabase();

            if (stockCodes.isEmpty()) {
                return Result.error(404, "数据库中没有股票数据");
            }

            long startTime = System.currentTimeMillis();
            int successCount = persistenceService.batchSyncStocksToDatabase(stockCodes);
            long duration = System.currentTimeMillis() - startTime;

            Map<String, Object> data = new HashMap<>();
            data.put("total", stockCodes.size());
            data.put("success", successCount);
            data.put("failed", stockCodes.size() - successCount);
            data.put("duration", duration + "ms");
            data.put("message", "同步所有股票完成");

            return Result.success(data);

        } catch (Exception e) {
            log.error("同步所有股票失败", e);
            return Result.error(500, "同步所有股票失败: " + e.getMessage());
        }
    }

    /**
     * 获取数据库中的股票数量
     * 
     * @return 股票数量
     */
    @GetMapping("/count")
    public Result<Map<String, Object>> getStockCount() {
        try {
            List<String> stockCodes = persistenceService.getAllStockCodesFromDatabase();

            Map<String, Object> data = new HashMap<>();
            data.put("count", stockCodes.size());
            data.put("stockCodes", stockCodes);
            data.put("message", "查询成功");

            return Result.success(data);

        } catch (Exception e) {
            log.error("查询股票数量失败", e);
            return Result.error(500, "查询失败: " + e.getMessage());
        }
    }
}
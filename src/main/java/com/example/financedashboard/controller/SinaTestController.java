package com.example.financedashboard.controller;

import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.service.sina.SinaDataSyncService;
import com.example.financedashboard.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

/**
 * 新浪API测试控制器
 * 用于测试新浪财经API的各项功能
 * 
 * 注意：这是测试接口，生产环境建议删除或添加权限控制
 */
@Slf4j
@RestController
@RequestMapping("/api/test/sina")
public class SinaTestController {

    @Autowired
    private SinaDataSyncService sinaDataSyncService;

    /**
     * 测试API连接
     * 
     * GET /api/test/sina/connection
     */
    @GetMapping("/connection")
    public Result<String> testConnection() {
        log.info("测试新浪API连接");
        
        boolean available = sinaDataSyncService.testApiAvailability();
        
        if (available) {
            return Result.success("新浪API连接正常");
        } else {
            return Result.error(500, "新浪API连接失败");
        }
    }

    /**
     * 获取单个股票实时数据
     * 
     * GET /api/test/sina/realtime/{stockCode}
     * 
     * 示例：
     * GET /api/test/sina/realtime/600519  (贵州茅台)
     * GET /api/test/sina/realtime/000001  (平安银行)
     */
    @GetMapping("/realtime/{stockCode}")
    public Result<SinaRealtimeDTO> getRealtimeData(@PathVariable String stockCode) {
        log.info("获取股票实时数据: {}", stockCode);
        
        try {
            SinaRealtimeDTO data = sinaDataSyncService.fetchAndCleanRealtimeData(stockCode);
            
            if (data != null) {
                return Result.success(data);
            } else {
                return Result.error(404, "获取数据失败，请检查股票代码是否正确");
            }
            
        } catch (Exception e) {
            log.error("获取实时数据异常", e);
            return Result.error(500, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 批量获取多个股票实时数据
     * 
     * POST /api/test/sina/realtime/batch
     * 
     * 请求体示例：
     * {
     *   "stockCodes": ["600519", "000001", "600036"]
     * }
     */
    @PostMapping("/realtime/batch")
    public Result<List<SinaRealtimeDTO>> getBatchRealtimeData(
            @RequestBody BatchRequest request) {
        
        log.info("批量获取股票实时数据，数量: {}", request.getStockCodes().size());
        
        try {
            List<SinaRealtimeDTO> dataList = sinaDataSyncService
                    .fetchAndCleanBatchRealtimeData(request.getStockCodes());
            
            return Result.success(dataList);
            
        } catch (Exception e) {
            log.error("批量获取实时数据异常", e);
            return Result.error(500, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 快速测试 - 获取热门股票数据
     * 
     * GET /api/test/sina/quick-test
     */
    @GetMapping("/quick-test")
    public Result<List<SinaRealtimeDTO>> quickTest() {
        log.info("快速测试 - 获取热门股票数据");
        
        // 测试几只热门股票
        List<String> testStocks = Arrays.asList(
                "600519",  // 贵州茅台
                "000001",  // 平安银行
                "600036",  // 招商银行
                "000858",  // 五粮液
                "600887"   // 伊利股份
        );
        
        try {
            List<SinaRealtimeDTO> dataList = sinaDataSyncService
                    .fetchAndCleanBatchRealtimeData(testStocks);
            
            return Result.success(dataList);
            
        } catch (Exception e) {
            log.error("快速测试异常", e);
            return Result.error(500, "系统异常: " + e.getMessage());
        }
    }

    /**
     * 批量请求DTO
     */
    @lombok.Data
    public static class BatchRequest {
        private List<String> stockCodes;
    }
}
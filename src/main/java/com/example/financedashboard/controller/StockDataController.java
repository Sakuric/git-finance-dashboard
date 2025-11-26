package com.example.financedashboard.controller;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.service.realtime.StockDataRealtimeService;
import com.example.financedashboard.service.realtime.TechnicalIndicatorService;
import com.example.financedashboard.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票数据查询Controller
 * 提供实时K线数据和技术指标查询接口
 */
@RestController
@RequestMapping("/api/stock-data")
public class StockDataController {

    @Autowired
    private StockDataRealtimeService stockDataRealtimeService;

    @Autowired
    private TechnicalIndicatorService technicalIndicatorService;

    /**
     * 获取股票历史K线数据
     * 
     * @param code 股票代码
     * @param days 天数(默认250)
     * @return K线数据列表
     */
    @GetMapping("/kline/{code}")
    public Result<List<KLineDTO>> getKLineData(
            @PathVariable String code,
            @RequestParam(required = false, defaultValue = "250") Integer days) {
        
        List<KLineDTO> klineList = stockDataRealtimeService.getStockHistory(code, days);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        return Result.success(klineList);
    }

    /**
     * 获取股票指定日期范围的K线数据
     * 
     * @param code 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线数据列表
     */
    @GetMapping("/kline/{code}/range")
    public Result<List<KLineDTO>> getKLineDataByRange(
            @PathVariable String code,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        List<KLineDTO> klineList = stockDataRealtimeService.getKLinesByDateRange(code, startDate, endDate);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        return Result.success(klineList);
    }

    /**
     * 获取股票最近N天的K线数据
     * 
     * @param code 股票代码
     * @param days 天数
     * @return K线数据列表
     */
    @GetMapping("/kline/{code}/recent")
    public Result<List<KLineDTO>> getRecentKLineData(
            @PathVariable String code,
            @RequestParam(defaultValue = "30") Integer days) {
        
        List<KLineDTO> klineList = stockDataRealtimeService.getRecentKLines(code, days);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        return Result.success(klineList);
    }

    /**
     * 获取股票技术指标
     * 
     * @param code 股票代码
     * @param days 计算天数(默认250)
     * @return 技术指标数据
     */
    @GetMapping("/indicators/{code}")
    public Result<Map<String, Object>> getTechnicalIndicators(
            @PathVariable String code,
            @RequestParam(required = false, defaultValue = "250") Integer days) {
        
        // 获取K线数据
        List<KLineDTO> klineList = stockDataRealtimeService.getStockHistory(code, days);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        // 计算技术指标
        Map<String, Object> indicators = technicalIndicatorService.calculateAllIndicators(klineList);
        
        return Result.success(indicators);
    }

    /**
     * 获取股票完整数据(K线+技术指标)
     * 
     * @param code 股票代码
     * @param days 天数(默认250)
     * @return 完整数据
     */
    @GetMapping("/complete/{code}")
    public Result<Map<String, Object>> getCompleteData(
            @PathVariable String code,
            @RequestParam(required = false, defaultValue = "250") Integer days) {
        
        // 获取K线数据
        List<KLineDTO> klineList = stockDataRealtimeService.getStockHistory(code, days);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        // 计算技术指标
        Map<String, Object> indicators = technicalIndicatorService.calculateAllIndicators(klineList);
        
        // 组装返回数据
        Map<String, Object> result = new HashMap<>();
        result.put("stockCode", code);
        result.put("klineData", klineList);
        result.put("indicators", indicators);
        result.put("dataCount", klineList.size());
        
        return Result.success(result);
    }

    /**
     * 获取最新的技术指标(基于最近30天数据)
     * 
     * @param code 股票代码
     * @return 最新技术指标
     */
    @GetMapping("/indicators/{code}/latest")
    public Result<Map<String, Object>> getLatestIndicators(@PathVariable String code) {
        
        // 获取最近30天K线数据
        List<KLineDTO> klineList = stockDataRealtimeService.getRecentKLines(code, 30);
        
        if (klineList.isEmpty()) {
            return Result.error(404, "未找到股票数据");
        }
        
        // 计算技术指标
        Map<String, Object> indicators = technicalIndicatorService.getLatestIndicators(klineList);
        
        // 添加最新K线信息
        KLineDTO latestKLine = klineList.get(klineList.size() - 1);
        Map<String, Object> result = new HashMap<>();
        result.put("stockCode", code);
        result.put("latestDate", latestKLine.getTradeDate());
        result.put("latestPrice", latestKLine.getClosePrice());
        result.put("changePercent", latestKLine.getChangePercent());
        result.put("indicators", indicators);
        
        return Result.success(result);
    }

    /**
     * 批量获取多个股票的最新数据
     * 
     * @param codes 股票代码列表
     * @return 批量数据
     */
    @PostMapping("/batch/latest")
    public Result<Map<String, Object>> getBatchLatestData(@RequestBody List<String> codes) {
        
        Map<String, Object> batchResult = new HashMap<>();
        
        for (String code : codes) {
            try {
                List<KLineDTO> klineList = stockDataRealtimeService.getRecentKLines(code, 30);
                
                if (!klineList.isEmpty()) {
                    KLineDTO latestKLine = klineList.get(klineList.size() - 1);
                    Map<String, Object> indicators = technicalIndicatorService.getLatestIndicators(klineList);
                    
                    Map<String, Object> stockData = new HashMap<>();
                    stockData.put("latestDate", latestKLine.getTradeDate());
                    stockData.put("latestPrice", latestKLine.getClosePrice());
                    stockData.put("changePercent", latestKLine.getChangePercent());
                    stockData.put("indicators", indicators);
                    
                    batchResult.put(code, stockData);
                }
            } catch (Exception e) {
                // 单个股票失败不影响其他股票
                batchResult.put(code, Map.of("error", "获取数据失败"));
            }
        }
        
        return Result.success(batchResult);
    }

    /**
     * 获取API使用说明
     */
    @GetMapping("/help")
    public Result<Map<String, String>> getHelp() {
        Map<String, String> help = new HashMap<>();
        help.put("GET /api/stock-data/kline/{code}", "获取股票K线数据,参数: days(默认250)");
        help.put("GET /api/stock-data/kline/{code}/range", "获取指定日期范围K线,参数: startDate, endDate");
        help.put("GET /api/stock-data/kline/{code}/recent", "获取最近N天K线,参数: days(默认30)");
        help.put("GET /api/stock-data/indicators/{code}", "获取技术指标,参数: days(默认250)");
        help.put("GET /api/stock-data/complete/{code}", "获取完整数据(K线+指标),参数: days(默认250)");
        help.put("GET /api/stock-data/indicators/{code}/latest", "获取最新技术指标(基于30天)");
        help.put("POST /api/stock-data/batch/latest", "批量获取多个股票最新数据,Body: [\"000001\",\"000002\"]");
        help.put("说明", "所有数据实时获取,不存储到数据库,响应时间2-3秒");
        
        return Result.success(help);
    }
}
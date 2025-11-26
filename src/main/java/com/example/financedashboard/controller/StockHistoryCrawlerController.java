package com.example.financedashboard.controller;

import com.example.financedashboard.service.crawler.StockHistoryCrawlerService;
import com.example.financedashboard.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 股票历史数据爬虫Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/history-crawler")
public class StockHistoryCrawlerController {

    @Autowired
    private StockHistoryCrawlerService crawlerService;

    /**
     * 爬取单个股票的历史数据
     *
     * @param code 股票代码
     * @param startDate 开始日期(可选,默认为一年前)
     * @param endDate 结束日期(可选,默认为今天)
     * @return 爬取结果
     */
    @PostMapping("/stock/{code}")
    public Result<String> crawlSingleStock(
            @PathVariable String code,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("收到爬取单个股票历史数据请求: {}, 日期范围: {} 至 {}", code, startDate, endDate);
        
        // 设置默认日期范围
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusYears(1);
        }
        
        try {
            int rows = crawlerService.updateStockHistory(code, startDate, endDate);
            if (rows > 0) {
                return Result.success("成功爬取并更新 " + rows + " 条历史数据: " + code);
            } else {
                return Result.error(500, "爬取失败或无数据: " + code);
            }
        } catch (Exception e) {
            log.error("爬取历史数据失败: {}", code, e);
            return Result.error(500, "爬取失败: " + e.getMessage());
        }
    }

    /**
     * 批量爬取指定股票的历史数据
     *
     * @param codes 股票代码列表
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 爬取结果统计
     */
    @PostMapping("/stock/batch")
    public Result<Map<String, Object>> crawlBatchStocks(
            @RequestBody List<String> codes,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("收到批量爬取历史数据请求，股票数量: {}", codes.size());
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusYears(1);
        }
        
        Map<String, Object> result = new HashMap<>();
        int success = 0;
        int failed = 0;
        int totalRecords = 0;
        
        for (String code : codes) {
            try {
                int rows = crawlerService.updateStockHistory(code, startDate, endDate);
                if (rows > 0) {
                    success++;
                    totalRecords += rows;
                } else {
                    failed++;
                }
                // 添加延迟避免请求过快
                Thread.sleep(2000);
            } catch (Exception e) {
                log.error("爬取股票 {} 失败", code, e);
                failed++;
            }
        }
        
        result.put("total", codes.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("records", totalRecords);
        
        return Result.success(result);
    }

    /**
     * 爬取所有股票的历史数据
     *
     * @param startDate 开始日期(可选)
     * @param endDate 结束日期(可选)
     * @return 爬取结果统计
     */
    @PostMapping("/stock/all")
    public Result<Map<String, Integer>> crawlAllStocks(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {
        
        log.info("收到爬取所有股票历史数据请求");
        
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        if (startDate == null) {
            startDate = endDate.minusMonths(3); // 默认爬取最近3个月
        }
        
        try {
            Map<String, Integer> stats = crawlerService.updateAllStockHistory(startDate, endDate);
            return Result.success(stats);
        } catch (Exception e) {
            log.error("批量爬取历史数据失败", e);
            return Result.error(500, "批量爬取失败: " + e.getMessage());
        }
    }

    /**
     * 获取使用帮助
     */
    @GetMapping("/help")
    public Result<Map<String, String>> getHelp() {
        Map<String, String> help = new HashMap<>();
        help.put("单个股票", "POST /api/history-crawler/stock/{code}?startDate=2024-01-01&endDate=2024-12-31");
        help.put("批量爬取", "POST /api/history-crawler/stock/batch?startDate=2024-01-01&endDate=2024-12-31 (请求体: [\"000001\",\"000002\"])");
        help.put("全部股票", "POST /api/history-crawler/stock/all?startDate=2024-01-01&endDate=2024-12-31");
        help.put("说明", "日期参数可选，默认爬取最近一年数据。批量爬取建议分批进行，避免时间过长。");
        return Result.success(help);
    }
}
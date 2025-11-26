package com.example.financedashboard.controller;

import com.example.financedashboard.service.crawler.SinaCrawlerService;
import com.example.financedashboard.utils.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 爬虫控制器
 * 用于触发股票详细信息的爬取任务
 */
@Slf4j
@RestController
@RequestMapping("/api/crawler")
public class CrawlerController {

    @Autowired
    private SinaCrawlerService crawlerService;

    /**
     * 爬取单个股票的详细信息
     * 
     * @param stockCode 股票代码
     * @return 爬取结果
     */
    @PostMapping("/stock/{stockCode}")
    public Result<String> crawlSingleStock(@PathVariable String stockCode) {
        log.info("收到爬取单个股票详细信息请求: {}", stockCode);
        
        try {
            boolean success = crawlerService.updateStockInfo(stockCode);
            
            if (success) {
                return Result.success("成功爬取并更新股票 " + stockCode + " 的详细信息");
            } else {
                return Result.error(400, "爬取失败，股票可能不存在或未获取到有效信息");
            }
            
        } catch (Exception e) {
            log.error("爬取股票详细信息失败: {}", stockCode, e);
            return Result.error(500, "爬取失败: " + e.getMessage());
        }
    }

    /**
     * 批量爬取指定股票列表的详细信息
     * 
     * @param stockCodes 股票代码列表
     * @return 爬取结果统计
     */
    @PostMapping("/stock/batch")
    public Result<Map<String, Integer>> crawlBatchStocks(@RequestBody List<String> stockCodes) {
        log.info("收到批量爬取详细信息请求，数量: {}", stockCodes.size());
        
        try {
            Map<String, Integer> stats = new java.util.HashMap<>();
            stats.put("total", stockCodes.size());
            stats.put("success", 0);
            stats.put("failed", 0);
            
            for (String stockCode : stockCodes) {
                boolean success = crawlerService.updateStockInfo(stockCode);
                if (success) {
                    stats.put("success", stats.get("success") + 1);
                } else {
                    stats.put("failed", stats.get("failed") + 1);
                }
                
                // 添加延迟避免被封IP
                try {
                    Thread.sleep(5000 + new java.util.Random().nextInt(5000));
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("批量爬取详细信息失败", e);
            return Result.error(500, "批量爬取失败: " + e.getMessage());
        }
    }

    /**
     * 爬取所有股票的详细信息
     * ⚠️ 警告：此操作可能需要数小时完成！
     * 
     * @return 爬取结果统计
     */
    @PostMapping("/stock/all")
    public Result<Map<String, Integer>> crawlAllStocks() {
        log.warn("⚠️ 收到爬取所有股票详细信息请求，此操作可能需要数小时完成！");
        
        try {
            Map<String, Integer> stats = crawlerService.updateAllStockInfo();
            return Result.success(stats);
            
        } catch (Exception e) {
            log.error("爬取所有股票详细信息失败", e);
            return Result.error(500, "爬取失败: " + e.getMessage());
        }
    }

    /**
     * 获取爬虫使用说明
     * 
     * @return 使用说明
     */
    @GetMapping("/help")
    public Result<Map<String, Object>> getHelp() {
        Map<String, Object> help = new java.util.HashMap<>();
        help.put("description", "新浪财经网页爬虫服务");
        help.put("features", java.util.Arrays.asList(
            "爬取股票的行业、板块、市值、上市日期等详细信息",
            "随机延迟5-10秒，避免IP被封",
            "完整的浏览器伪装",
            "失败自动重试最多3次"
        ));
        help.put("endpoints", java.util.Arrays.asList(
            "POST /api/crawler/stock/{stockCode} - 爬取单个股票",
            "POST /api/crawler/stock/batch - 批量爬取（传入股票代码数组）",
            "POST /api/crawler/stock/all - 爬取所有股票（⚠️ 需要数小时）"
        ));
        help.put("warning", "爬取所有股票可能需要数小时，建议分批次爬取或在非工作时间运行");
        
        return Result.success(help);
    }
}
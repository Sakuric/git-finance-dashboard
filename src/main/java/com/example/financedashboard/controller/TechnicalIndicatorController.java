package com.example.financedashboard.controller;

import com.example.financedashboard.service.realtime.TechnicalIndicatorService;
import com.example.financedashboard.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 技术指标Controller
 * 提供技术指标的实时计算API接口（不涉及数据库存储）
 */
@RestController
@RequestMapping("/api/technical-indicators")
public class TechnicalIndicatorController {

    @Autowired
    private TechnicalIndicatorService technicalIndicatorService;

    /**
     * 实时计算指定股票的技术指标
     * 
     * @param stockCode 股票代码
     * @param days 计算所需的历史天数(默认250天)
     * @return 技术指标数据
     */
    @GetMapping("/calculate/{stockCode}")
    public Result<Map<String, Object>> calculateIndicators(
            @PathVariable String stockCode,
            @RequestParam(required = false, defaultValue = "250") Integer days) {
        
        try {
            Map<String, Object> indicators = technicalIndicatorService.calculateIndicatorsForStock(stockCode, days);
            
            if (indicators.isEmpty()) {
                return Result.error(404, "未找到股票数据或数据不足");
            }
            
            return Result.success(indicators);
        } catch (Exception e) {
            return Result.error(500, "计算技术指标失败: " + e.getMessage());
        }
    }

    /**
     * 批量计算多个股票的技术指标
     * 
     * @param stockCodes 股票代码列表
     * @param days 计算所需的历史天数
     * @return 批量技术指标数据
     */
    @PostMapping("/calculate/batch")
    public Result<Map<String, Map<String, Object>>> batchCalculateIndicators(
            @RequestBody List<String> stockCodes,
            @RequestParam(required = false, defaultValue = "250") Integer days) {
        
        try {
            if (stockCodes == null || stockCodes.isEmpty()) {
                return Result.error(400, "股票代码列表不能为空");
            }
            
            Map<String, Map<String, Object>> batchResult = 
                technicalIndicatorService.batchCalculateIndicators(stockCodes, days);
            return Result.success(batchResult);
        } catch (Exception e) {
            return Result.error(500, "批量计算失败: " + e.getMessage());
        }
    }

    /**
     * 获取API使用说明
     */
    @GetMapping("/help")
    public Result<Map<String, String>> getHelp() {
        Map<String, String> help = Map.of(
            "GET /api/technical-indicators/calculate/{stockCode}", "实时计算技术指标,参数: days(默认250)",
            "POST /api/technical-indicators/calculate/batch", "批量计算技术指标,Body: [股票代码列表], 参数: days(默认250)",
            "说明1", "所有技术指标实时计算，不存储到数据库",
            "说明2", "支持的指标: MACD、RSI、KDJ、BOLL",
            "说明3", "建议使用250天数据以确保所有指标都能正确计算",
            "示例", "GET /api/technical-indicators/calculate/000001?days=250"
        );
        
        return Result.success(help);
    }
}
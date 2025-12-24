package com.example.financedashboard.controller;

import com.example.financedashboard.service.PortfolioAnalysisService;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/portfolio-analysis")
@RequiredArgsConstructor
public class PortfolioAnalysisController {

    private final PortfolioAnalysisService portfolioAnalysisService;

    @GetMapping("/balance/{userId}")
    public Result<Map<String, Object>> getPortfolioBalance(@PathVariable Long userId) {
        try {
            Map<String, Object> balanceData = portfolioAnalysisService.calculatePortfolioBalance(userId);
            return Result.success(balanceData);
        } catch (Exception e) {
            return Result.error(500, "获取配置均衡度失败: " + e.getMessage());
        }
    }
}

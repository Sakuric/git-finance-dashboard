package com.example.financedashboard.controller;

import com.example.financedashboard.dto.BacktestRequestDTO;
import com.example.financedashboard.dto.BacktestResponseDTO;
import com.example.financedashboard.entity.BacktestResult;
import com.example.financedashboard.service.AdvancedBacktestService;
import com.example.financedashboard.service.BacktestService;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestService backtestService;
    private final AdvancedBacktestService advancedBacktestService;

    @GetMapping("/{adviceId}")
    public Result<BacktestResult> getBacktest(@PathVariable Long adviceId) {
        BacktestResult result = backtestService.getBacktestByAdviceId(adviceId);
        if (result == null) {
            return Result.error(404, "未找到该建议的回测结果");
        }
        return Result.success(result);
    }

    @PostMapping("/run")
    public Result<BacktestResponseDTO> runAdvancedBacktest(@RequestBody BacktestRequestDTO request) {
        try {
            BacktestResponseDTO result = advancedBacktestService.runAdvancedBacktest(request);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "回测失败: " + e.getMessage());
        }
    }

    @PostMapping("/analyze")
    public Result<String> analyzeBacktest(@RequestBody BacktestResponseDTO data) {
        try {
            String analysis = advancedBacktestService.generateDeepAnalysis(data);
            return Result.success(analysis);
        } catch (Exception e) {
            return Result.error(500, "AI分析失败: " + e.getMessage());
        }
    }
}

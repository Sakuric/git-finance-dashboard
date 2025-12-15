package com.example.financedashboard.controller;

import com.example.financedashboard.entity.BacktestResult;
import com.example.financedashboard.service.BacktestService;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/backtest")
@RequiredArgsConstructor
public class BacktestController {

    private final BacktestService backtestService;

    @GetMapping("/{adviceId}")
    public Result<BacktestResult> getBacktest(@PathVariable Long adviceId) {
        BacktestResult result = backtestService.getBacktestByAdviceId(adviceId);
        if (result == null) {
            return Result.error(404, "未找到该建议的回测结果");
        }
        return Result.success(result);
    }
}

package com.example.financedashboard.controller;

import com.example.financedashboard.entity.StockBacktestDetail;
import com.example.financedashboard.mapper.StockBacktestDetailMapper;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/backtest/detail")
@RequiredArgsConstructor
public class StockBacktestDetailController {

    private final StockBacktestDetailMapper detailMapper;

    @GetMapping("/advice/{adviceId}")
    public Result<List<StockBacktestDetail>> getDetailsByAdviceId(@PathVariable Long adviceId) {
        List<StockBacktestDetail> details = detailMapper.findByAdviceId(adviceId);
        if (details.isEmpty()) {
            return Result.error(404, "未找到该建议的股票回测详情");
        }
        return Result.success(details);
    }

    @GetMapping("/backtest/{backtestId}")
    public Result<List<StockBacktestDetail>> getDetailsByBacktestId(@PathVariable Long backtestId) {
        List<StockBacktestDetail> details = detailMapper.findByBacktestId(backtestId);
        if (details.isEmpty()) {
            return Result.error(404, "未找到该回测的股票详情");
        }
        return Result.success(details);
    }
}

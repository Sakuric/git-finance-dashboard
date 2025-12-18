package com.example.financedashboard.controller;

import com.example.financedashboard.entity.InvestmentAdvice;
import com.example.financedashboard.service.InvestmentAdviceService;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/advice")
@RequiredArgsConstructor
public class InvestmentAdviceController {

    private final InvestmentAdviceService adviceService;

    @PostMapping("/generate")
    public Result<InvestmentAdvice> generateAdvice(@RequestParam Long userId) {
        try {
            InvestmentAdvice advice = adviceService.generateAdvice(userId);
            return Result.success(advice);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @GetMapping("/latest")
    public Result<InvestmentAdvice> getLatestAdvice(@RequestParam Long userId) {
        InvestmentAdvice advice = adviceService.getLatestAdvice(userId);
        return advice != null ? Result.success(advice) : Result.error(404, "暂无投资建议");
    }
    @GetMapping("/list")
    public Result<?> getAdviceList(@RequestParam Long userId) {
        try {
            return Result.success(adviceService.getUserAdviceList(userId));
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

}

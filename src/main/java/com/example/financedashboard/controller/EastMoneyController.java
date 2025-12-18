package com.example.financedashboard.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.financedashboard.service.eastmoney.EastMoneyApiService;
import com.example.financedashboard.utils.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/eastmoney")
@RequiredArgsConstructor
public class EastMoneyController {

    private final EastMoneyApiService eastMoneyApiService;

    @GetMapping("/quote/{stockCode}")
    public Result<Map<String, Object>> getStockQuote(@PathVariable String stockCode) {
        String response = eastMoneyApiService.fetchStockQuote(stockCode);
        if (response == null || response.isEmpty()) {
            return Result.error(500, "获取行情失败");
        }

        try {
            JSONObject json = JSON.parseObject(response);
            JSONObject data = json.getJSONObject("data");
            if (data == null || data.isEmpty()) {
                return Result.error(404, "未找到股票数据");
            }

            Map<String, Object> result = new HashMap<>();
            result.put("turnoverRate", data.getDoubleValue("f168") / 100.0);
            result.put("pe", data.getDoubleValue("f162") / 100.0);
            result.put("totalMarketCap", data.getDoubleValue("f116"));
            result.put("circulationMarketCap", data.getDoubleValue("f117"));

            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, "解析数据失败: " + e.getMessage());
        }
    }
}

package com.example.financedashboard.controller;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.service.MarketIndexService;
import com.example.financedashboard.utils.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/market")
public class MarketIndexController {

    @Autowired
    private MarketIndexService marketIndexService;

    @GetMapping("/indices")
    public Result<List<SinaRealtimeDTO>> getMarketIndices() {
        List<SinaRealtimeDTO> indices = marketIndexService.getMainIndices();
        return Result.success(indices);
    }

    @GetMapping("/indices/{indexCode}")
    public Result<SinaRealtimeDTO> getIndexDetail(@PathVariable String indexCode) {
        SinaRealtimeDTO index = marketIndexService.getIndexByCode(indexCode);
        if (index != null) {
            return Result.success(index);
        }
        return Result.error(404, "指数不存在");
    }

    @GetMapping("/indices/{indexCode}/kline")
    public Result<List<KLineDTO>> getIndexKLine(
            @PathVariable String indexCode,
            @RequestParam(defaultValue = "250") Integer days) {
        List<KLineDTO> klineData = marketIndexService.getIndexKLine(indexCode, days);
        return Result.success(klineData);
    }

    @GetMapping("/overview")
    public Result<Map<String, Object>> getMarketOverview() {
        Map<String, Object> overview = marketIndexService.getMarketOverview();
        return Result.success(overview);
    }
}

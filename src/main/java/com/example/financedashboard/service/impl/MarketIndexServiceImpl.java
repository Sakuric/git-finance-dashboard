package com.example.financedashboard.service.impl;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.mapper.StockInfoMapper;
import com.example.financedashboard.service.MarketIndexService;
import com.example.financedashboard.service.realtime.StockDataRealtimeService;
import com.example.financedashboard.service.sina.SinaDataSyncService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class MarketIndexServiceImpl implements MarketIndexService {

    @Autowired
    private SinaDataSyncService sinaDataSyncService;

    @Autowired
    private StockDataRealtimeService stockDataRealtimeService;

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Override
    public List<SinaRealtimeDTO> getMainIndices() {
        try {
            List<String> codes = Arrays.asList("sh000001", "sz399001", "sz399006");
            return sinaDataSyncService.fetchAndCleanBatchRealtimeData(codes);
        } catch (Exception e) {
            log.error("获取主要指数失败", e);
            return Collections.emptyList();
        }
    }

    @Override
    public SinaRealtimeDTO getIndexByCode(String indexCode) {
        try {
            return sinaDataSyncService.fetchAndCleanRealtimeData(indexCode);
        } catch (Exception e) {
            log.error("获取指数{}失败", indexCode, e);
            return null;
        }
    }

    @Override
    public List<KLineDTO> getIndexKLine(String indexCode, Integer days) {
        try {
            return stockDataRealtimeService.getStockHistory(indexCode, days);
        } catch (Exception e) {
            log.error("获取指数K线数据失败: {}", indexCode, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getMarketOverview() {
        Map<String, Object> overview = new HashMap<>();

        try {
            List<StockInfo> allStocks = stockInfoMapper.findAll();

            int upCount = 0;
            int downCount = 0;
            int limitUpCount = 0;
            int limitDownCount = 0;
            BigDecimal totalVolume = BigDecimal.ZERO;

            for (StockInfo stock : allStocks) {
                if (stock.getChangePercent() != null) {
                    BigDecimal change = stock.getChangePercent();

                    if (change.compareTo(BigDecimal.ZERO) > 0) {
                        upCount++;
                        if (change.compareTo(new BigDecimal("9.9")) >= 0) {
                            limitUpCount++;
                        }
                    } else if (change.compareTo(BigDecimal.ZERO) < 0) {
                        downCount++;
                        if (change.compareTo(new BigDecimal("-9.9")) <= 0) {
                            limitDownCount++;
                        }
                    }
                }
            }

            overview.put("upCount", upCount);
            overview.put("downCount", downCount);
            overview.put("limitUpCount", limitUpCount);
            overview.put("limitDownCount", limitDownCount);
            overview.put("totalVolume", formatVolume(totalVolume));
            overview.put("turnoverRate", "--");
            overview.put("updateTime", new Date());

        } catch (Exception e) {
            log.error("获取市场概况失败", e);
            overview.put("upCount", "--");
            overview.put("downCount", "--");
            overview.put("limitUpCount", "--");
            overview.put("limitDownCount", "--");
            overview.put("totalVolume", "--");
            overview.put("turnoverRate", "--");
            overview.put("updateTime", new Date());
        }

        return overview;
    }

    private String formatVolume(BigDecimal volume) {
        if (volume.compareTo(BigDecimal.valueOf(100000000)) >= 0) {
            return volume.divide(BigDecimal.valueOf(100000000), 2, RoundingMode.HALF_UP) + "亿";
        }
        return volume.toString();
    }
}

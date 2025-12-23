package com.example.financedashboard.service.impl;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.entity.StockHistory;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.mapper.StockHistoryMapper;
import com.example.financedashboard.mapper.StockInfoMapper;
import com.example.financedashboard.service.MarketIndexService;
import com.example.financedashboard.service.eastmoney.EastMoneyApiService;
import com.example.financedashboard.service.sina.SinaDataSyncService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private EastMoneyApiService eastMoneyApiService;

    @Autowired
    private StockHistoryMapper stockHistoryMapper;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private volatile Map<String, Object> cachedOverview = null;
    private volatile long lastSyncTime = 0;
    private static final long CACHE_DURATION = 5 * 60 * 1000; // 5分钟缓存

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
            String json = eastMoneyApiService.fetchIndexKLine(indexCode, days);
            if (json == null) return Collections.emptyList();

            JsonNode root = objectMapper.readTree(json);
            JsonNode klines = root.path("data").path("klines");
            if (!klines.isArray()) return Collections.emptyList();

            List<KLineDTO> result = new ArrayList<>();
            for (JsonNode kline : klines) {
                String[] parts = kline.asText().split(",");
                if (parts.length < 6) continue;

                KLineDTO dto = new KLineDTO();
                dto.setTradeDate(java.time.LocalDate.parse(parts[0]));
                dto.setOpenPrice(new BigDecimal(parts[1]));
                dto.setClosePrice(new BigDecimal(parts[2]));
                dto.setHighPrice(new BigDecimal(parts[3]));
                dto.setLowPrice(new BigDecimal(parts[4]));
                dto.setVolume(Long.parseLong(parts[5]));
                result.add(dto);
            }
            return result;
        } catch (Exception e) {
            log.error("解析东方财富K线失败: {}", indexCode, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Map<String, Object> getMarketOverview() {
        long currentTime = System.currentTimeMillis();

        if (cachedOverview != null && (currentTime - lastSyncTime) < CACHE_DURATION) {
            log.info("返回缓存的市场概况数据");
            return cachedOverview;
        }

        return fetchRealtimeMarketOverview();
    }

    private Map<String, Object> fetchRealtimeMarketOverview() {
        Map<String, Object> overview = new HashMap<>();
        try {
            List<String> sampleCodes = Arrays.asList(
                "sh600519", "sh600036", "sh601318", "sh600276", "sh601166",
                "sz000858", "sz002594", "sz000333", "sz002475", "sz300750"
            );

            List<SinaRealtimeDTO> realtimeData = sinaDataSyncService.fetchAndCleanBatchRealtimeData(sampleCodes);

            int upCount = 0;
            int downCount = 0;
            int limitUpCount = 0;
            int limitDownCount = 0;
            BigDecimal totalAmount = BigDecimal.ZERO;

            for (SinaRealtimeDTO stock : realtimeData) {
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
                if (stock.getAmount() != null) {
                    totalAmount = totalAmount.add(stock.getAmount());
                }
            }

            int ratio = realtimeData.isEmpty() ? 1 : 500;
            overview.put("upCount", upCount * ratio);
            overview.put("downCount", downCount * ratio);
            overview.put("limitUpCount", limitUpCount * ratio);
            overview.put("limitDownCount", limitDownCount * ratio);
            overview.put("totalVolume", formatVolume(totalAmount.multiply(BigDecimal.valueOf(ratio))));
            overview.put("turnoverRate", "--");
            overview.put("updateTime", new Date());

            log.info("使用实时API数据估算市场概况");
            return overview;

        } catch (Exception e) {
            log.error("获取实时市场概况失败", e);
            overview.put("upCount", "--");
            overview.put("downCount", "--");
            overview.put("limitUpCount", "--");
            overview.put("limitDownCount", "--");
            overview.put("totalVolume", "--");
            overview.put("turnoverRate", "--");
            overview.put("updateTime", new Date());
            return overview;
        }
    }

    private String formatVolume(BigDecimal volume) {
        if (volume.compareTo(BigDecimal.valueOf(100000000)) >= 0) {
            return volume.divide(BigDecimal.valueOf(100000000), 2, RoundingMode.HALF_UP) + "亿";
        }
        return volume.toString();
    }
}

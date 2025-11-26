package com.example.financedashboard.service.realtime;

import com.example.financedashboard.dto.KLineDTO;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票数据实时查询服务
 * 不存储历史数据,所有数据实时从API获取
 */
@Service
public class StockDataRealtimeService {

    private static final Logger logger = LoggerFactory.getLogger(StockDataRealtimeService.class);

    @Autowired
    private OkHttpClient okHttpClient;

    @Autowired
    private ObjectMapper objectMapper;

    private static final String KLINE_API_URL = "http://push2his.eastmoney.com/api/qt/stock/kline/get";

    /**
     * 获取股票历史K线数据(实时)
     * 
     * @param stockCode 股票代码(如 000001)
     * @param days 获取天数(默认250,约1年)
     * @return K线数据列表(包含均线)
     */
    public List<KLineDTO> getStockHistory(String stockCode, Integer days) {
        if (days == null || days <= 0) {
            days = 250; // 默认1年
        }

        try {
            // 构建市场代码
            String marketCode = getMarketCode(stockCode);
            String fullCode = marketCode + "." + stockCode;

            // 构建请求URL - 注意:东方财富API可能需要特定的字段组合
            String url = String.format(
                "%s?secid=%s&fields1=f1,f2,f3,f4,f5,f6" +
                "&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61" +
                "&klt=101&fqt=1&lmt=%d&beg=0&end=20500000",
                KLINE_API_URL, fullCode, days
            );

            logger.info("获取股票 {} 的K线数据,URL: {}", stockCode, url);

            Request request = new Request.Builder()
                    .url(url)
                    .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();

            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.error("获取K线数据失败,HTTP状态码: {}", response.code());
                    return new ArrayList<>();
                }

                String responseBody = response.body().string();
                logger.info("API响应: {}", responseBody.substring(0, Math.min(500, responseBody.length())));
                
                JsonNode rootNode = objectMapper.readTree(responseBody);
                JsonNode dataNode = rootNode.path("data");

                if (dataNode.isMissingNode()) {
                    logger.warn("股票 {} 没有K线数据,响应: {}", stockCode, responseBody);
                    return new ArrayList<>();
                }

                // 解析K线数据
                JsonNode klinesNode = dataNode.path("klines");
                if (!klinesNode.isArray()) {
                    logger.warn("K线数据格式错误,dataNode: {}", dataNode.toString());
                    return new ArrayList<>();
                }

                List<KLineDTO> klineList = new ArrayList<>();
                for (JsonNode klineNode : klinesNode) {
                    String klineStr = klineNode.asText();
                    KLineDTO kline = parseKLine(stockCode, klineStr);
                    if (kline != null) {
                        klineList.add(kline);
                    }
                }

                // 只保留最近的N条数据
                int totalSize = klineList.size();
                if (klineList.size() > days) {
                    klineList = klineList.subList(klineList.size() - days, klineList.size());
                }

                // 计算均线
                calculateMA(klineList);

                logger.info("成功获取股票 {} 的 {} 条K线数据(总共{}条,返回最近{}条)",
                    stockCode, klineList.size(), totalSize, days);
                return klineList;

            }
        } catch (Exception e) {
            logger.error("获取股票 {} 的K线数据失败", stockCode, e);
            return new ArrayList<>();
        }
    }

    /**
     * 获取股票最近N天的K线数据
     */
    public List<KLineDTO> getRecentKLines(String stockCode, int days) {
        return getStockHistory(stockCode, days);
    }

    /**
     * 获取股票指定日期范围的K线数据
     */
    public List<KLineDTO> getKLinesByDateRange(String stockCode, LocalDate startDate, LocalDate endDate) {
        // 计算天数差
        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
        int days = (int) (daysBetween * 1.5); // 考虑非交易日,多取50%

        List<KLineDTO> allKLines = getStockHistory(stockCode, days);

        // 过滤日期范围
        return allKLines.stream()
                .filter(kline -> {
                    LocalDate tradeDate = kline.getTradeDate();
                    return !tradeDate.isBefore(startDate) && !tradeDate.isAfter(endDate);
                })
                .toList();
    }

    /**
     * 解析K线字符串
     * 格式: 日期,开盘,收盘,最高,最低,成交量,成交额,振幅,涨跌幅,涨跌额,换手率
     */
    private KLineDTO parseKLine(String stockCode, String klineStr) {
        try {
            String[] parts = klineStr.split(",");
            if (parts.length < 11) {
                logger.warn("K线数据格式错误: {}", klineStr);
                return null;
            }

            KLineDTO kline = new KLineDTO();
            kline.setStockCode(stockCode);
            kline.setTradeDate(LocalDate.parse(parts[0], DateTimeFormatter.ISO_LOCAL_DATE));
            kline.setOpenPrice(new BigDecimal(parts[1]));
            kline.setClosePrice(new BigDecimal(parts[2]));
            kline.setHighPrice(new BigDecimal(parts[3]));
            kline.setLowPrice(new BigDecimal(parts[4]));
            kline.setVolume(Long.parseLong(parts[5]));
            kline.setAmount(new BigDecimal(parts[6]));
            kline.setChangePercent(new BigDecimal(parts[8]));
            kline.setChangeAmount(new BigDecimal(parts[9]));

            return kline;
        } catch (Exception e) {
            logger.error("解析K线数据失败: {}", klineStr, e);
            return null;
        }
    }

    /**
     * 计算移动平均线
     */
    private void calculateMA(List<KLineDTO> klineList) {
        if (klineList == null || klineList.isEmpty()) {
            return;
        }

        int size = klineList.size();

        for (int i = 0; i < size; i++) {
            KLineDTO current = klineList.get(i);

            // 计算MA5
            if (i >= 4) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 4; j <= i; j++) {
                    sum = sum.add(klineList.get(j).getClosePrice());
                }
                current.setMa5(sum.divide(BigDecimal.valueOf(5), 2, RoundingMode.HALF_UP));
            }

            // 计算MA10
            if (i >= 9) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 9; j <= i; j++) {
                    sum = sum.add(klineList.get(j).getClosePrice());
                }
                current.setMa10(sum.divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP));
            }

            // 计算MA20
            if (i >= 19) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 19; j <= i; j++) {
                    sum = sum.add(klineList.get(j).getClosePrice());
                }
                current.setMa20(sum.divide(BigDecimal.valueOf(20), 2, RoundingMode.HALF_UP));
            }

            // 计算MA60
            if (i >= 59) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 59; j <= i; j++) {
                    sum = sum.add(klineList.get(j).getClosePrice());
                }
                current.setMa60(sum.divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP));
            }
        }
    }

    /**
     * 根据股票代码获取市场代码
     * 上海: 1 (6开头,688开头科创板)
     * 深圳: 0 (0开头主板,3开头创业板)
     * 北交所: 0 (8开头,4开头)
     */
    private String getMarketCode(String stockCode) {
        if (stockCode.startsWith("6")) {
            return "1"; // 上海主板、科创板
        } else if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            return "0"; // 深圳主板、创业板
        } else if (stockCode.startsWith("8") || stockCode.startsWith("4")) {
            return "0"; // 北交所
        }
        return "1"; // 默认上海
    }
}
package com.example.financedashboard.service.crawler;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.entity.StockHistory;
import com.example.financedashboard.mapper.StockHistoryMapper;
import com.example.financedashboard.mapper.StockInfoMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 股票历史数据爬虫服务
 * 用于爬取股票的历史K线数据并计算技术指标
 */
@Slf4j
@Service
public class StockHistoryCrawlerService {

    @Autowired
    private StockHistoryMapper stockHistoryMapper;
    
    @Autowired
    private StockInfoMapper stockInfoMapper;
    
    @Autowired
    private OkHttpClient okHttpClient;

    // 东方财富历史K线API
    private static final String KLINE_API = "http://push2his.eastmoney.com/api/qt/stock/kline/get";
    private final Random random = new Random();
    private static final int MAX_RETRIES = 3;

    /**
     * 爬取单个股票的历史数据
     *
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return K线数据列表
     */
    public List<KLineDTO> crawlStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
        log.info("开始爬取股票历史数据: {}, 日期范围: {} 至 {}", stockCode, startDate, endDate);

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                if (retry > 0) {
                    int delay = 1000 + random.nextInt(2000);
                    log.info("第{}次重试，延迟{}ms", retry + 1, delay);
                    Thread.sleep(delay);
                }

                // 构建请求URL
                String secid = buildSecId(stockCode);
                String beg = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                String end = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
                
                String url = String.format(
                    "%s?secid=%s&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&klt=101&fqt=1&beg=%s&end=%s",
                    KLINE_API, secid, beg, end
                );
                
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Referer", "http://quote.eastmoney.com/")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        log.warn("请求失败: HTTP {}", response.code());
                        continue;
                    }

                    String jsonStr = response.body().string();
                    JSONObject json = new JSONObject(jsonStr);
                    
                    if (json.getInt("rc") != 0) {
                        log.warn("API返回错误: {}", json);
                        continue;
                    }

                    JSONObject data = json.getJSONObject("data");
                    if (!data.has("klines")) {
                        log.warn("没有K线数据");
                        return new ArrayList<>();
                    }

                    JSONArray klines = data.getJSONArray("klines");
                    List<KLineDTO> result = new ArrayList<>();
                    
                    for (int i = 0; i < klines.length(); i++) {
                        String kline = klines.getString(i);
                        KLineDTO dto = parseKLine(stockCode, kline);
                        if (dto != null) {
                            result.add(dto);
                        }
                    }

                    log.info("✅ 成功爬取{}条历史数据: {}", result.size(), stockCode);
                    return result;
                }

            } catch (Exception e) {
                log.error("爬取历史数据失败 (第{}次尝试): {}, 错误: {}", retry + 1, stockCode, e.getMessage());
                if (retry == MAX_RETRIES - 1) {
                    log.error("达到最大重试次数，放弃爬取: {}", stockCode);
                }
            }
        }

        return new ArrayList<>();
    }

    /**
     * 解析K线字符串
     * 格式: 日期,开盘,收盘,最高,最低,成交量,成交额,振幅,涨跌幅,涨跌额,换手率
     */
    private KLineDTO parseKLine(String stockCode, String kline) {
        try {
            String[] fields = kline.split(",");
            if (fields.length < 11) {
                return null;
            }

            KLineDTO dto = new KLineDTO();
            dto.setStockCode(stockCode);
            dto.setTradeDate(LocalDate.parse(fields[0], DateTimeFormatter.ofPattern("yyyy-MM-dd")));
            dto.setOpenPrice(new BigDecimal(fields[1]));
            dto.setClosePrice(new BigDecimal(fields[2]));
            dto.setHighPrice(new BigDecimal(fields[3]));
            dto.setLowPrice(new BigDecimal(fields[4]));
            dto.setVolume(Long.parseLong(fields[5]));
            dto.setAmount(new BigDecimal(fields[6]));
            dto.setAmplitude(new BigDecimal(fields[7]));
            dto.setChangePercent(new BigDecimal(fields[8]));
            dto.setChangeAmount(new BigDecimal(fields[9]));
            dto.setTurnoverRate(new BigDecimal(fields[10]));
            
            return dto;
        } catch (Exception e) {
            log.warn("解析K线数据失败: {}", kline, e);
            return null;
        }
    }

    /**
     * 计算均线
     * @param klines K线数据列表(按日期升序)
     * @return 带均线的K线数据列表
     */
    public List<KLineDTO> calculateMA(List<KLineDTO> klines) {
        if (klines == null || klines.isEmpty()) {
            return klines;
        }

        // 确保按日期升序排列
        klines.sort(Comparator.comparing(KLineDTO::getTradeDate));

        for (int i = 0; i < klines.size(); i++) {
            KLineDTO current = klines.get(i);
            
            // 计算MA5
            if (i >= 4) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 4; j <= i; j++) {
                    sum = sum.add(klines.get(j).getClosePrice());
                }
                current.setMa5(sum.divide(new BigDecimal("5"), 2, RoundingMode.HALF_UP));
            }
            
            // 计算MA10
            if (i >= 9) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 9; j <= i; j++) {
                    sum = sum.add(klines.get(j).getClosePrice());
                }
                current.setMa10(sum.divide(new BigDecimal("10"), 2, RoundingMode.HALF_UP));
            }
            
            // 计算MA20
            if (i >= 19) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 19; j <= i; j++) {
                    sum = sum.add(klines.get(j).getClosePrice());
                }
                current.setMa20(sum.divide(new BigDecimal("20"), 2, RoundingMode.HALF_UP));
            }
            
            // 计算MA60
            if (i >= 59) {
                BigDecimal sum = BigDecimal.ZERO;
                for (int j = i - 59; j <= i; j++) {
                    sum = sum.add(klines.get(j).getClosePrice());
                }
                current.setMa60(sum.divide(new BigDecimal("60"), 2, RoundingMode.HALF_UP));
            }
        }

        return klines;
    }

    /**
     * 将KLineDTO转换为StockHistory实体
     */
    private StockHistory convertToEntity(KLineDTO dto, Long stockId) {
        StockHistory history = new StockHistory();
        history.setStockCode(dto.getStockCode());
        history.setStockId(stockId);
        history.setTradeDate(dto.getTradeDate());
        history.setOpenPrice(dto.getOpenPrice());
        history.setHighPrice(dto.getHighPrice());
        history.setLowPrice(dto.getLowPrice());
        history.setClosePrice(dto.getClosePrice());
        history.setVolume(dto.getVolume());
        history.setAmount(dto.getAmount());
        history.setChangeAmount(dto.getChangeAmount());
        history.setChangePercent(dto.getChangePercent());
        history.setMa5(dto.getMa5());
        history.setMa10(dto.getMa10());
        history.setMa20(dto.getMa20());
        history.setMa60(dto.getMa60());
        return history;
    }

    /**
     * 更新单个股票的历史数据到数据库
     *
     * @param stockCode 股票代码
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新的记录数
     */
    @Transactional
    public int updateStockHistory(String stockCode, LocalDate startDate, LocalDate endDate) {
        // 爬取数据
        List<KLineDTO> klines = crawlStockHistory(stockCode, startDate, endDate);
        if (klines.isEmpty()) {
            return 0;
        }

        // 计算均线
        klines = calculateMA(klines);

        // 获取stock_id
        Long stockId = stockInfoMapper.findIdByCode(stockCode);
        if (stockId == null) {
            log.warn("股票代码不存在: {}", stockCode);
            return 0;
        }

        // 转换为实体并批量插入
        List<StockHistory> histories = new ArrayList<>();
        for (KLineDTO dto : klines) {
            histories.add(convertToEntity(dto, stockId));
        }

        try {
            int rows = stockHistoryMapper.batchInsertOrUpdate(histories);
            log.info("✅ 成功更新{}条历史数据到数据库: {}", rows, stockCode);
            return rows;
        } catch (Exception e) {
            log.error("更新历史数据到数据库失败: {}", stockCode, e);
            return 0;
        }
    }

    /**
     * 批量更新所有股票的历史数据
     *
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 更新结果统计
     */
    @Transactional
    public Map<String, Integer> updateAllStockHistory(LocalDate startDate, LocalDate endDate) {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("success", 0);
        stats.put("failed", 0);
        stats.put("records", 0);

        try {
            List<String> stockCodes = stockInfoMapper.getAllStockCodes();
            stats.put("total", stockCodes.size());
            log.info("开始批量更新历史数据，股票总数: {}, 日期范围: {} 至 {}", 
                    stockCodes.size(), startDate, endDate);

            int count = 0;
            for (String stockCode : stockCodes) {
                count++;

                try {
                    if (count % 10 == 0) {
                        log.info("进度: {}/{} ({}%), 成功: {}, 失败: {}, 记录数: {}",
                                count, stockCodes.size(),
                                String.format("%.2f", count * 100.0 / stockCodes.size()),
                                stats.get("success"), stats.get("failed"), stats.get("records"));
                    }

                    int rows = updateStockHistory(stockCode, startDate, endDate);
                    if (rows > 0) {
                        stats.put("success", stats.get("success") + 1);
                        stats.put("records", stats.get("records") + rows);
                    } else {
                        stats.put("failed", stats.get("failed") + 1);
                    }

                    // 随机延迟2-5秒
                    int delay = 2000 + random.nextInt(3000);
                    Thread.sleep(delay);

                } catch (InterruptedException e) {
                    log.error("线程被中断，停止爬取");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理股票 {} 时出错", stockCode, e);
                    stats.put("failed", stats.get("failed") + 1);
                }
            }

            log.info("批量更新完成！总数: {}, 成功: {}, 失败: {}, 总记录数: {}",
                    stats.get("total"), stats.get("success"), stats.get("failed"), stats.get("records"));

        } catch (Exception e) {
            log.error("批量更新过程中发生错误", e);
        }

        return stats;
    }

    /**
     * 构建secid
     * 格式: 市场代码.股票代码
     * 上海: 1.600519
     * 深圳: 0.000001
     */
    private String buildSecId(String stockCode) {
        String marketCode = stockCode.startsWith("6") ? "1" : "0";
        return marketCode + "." + stockCode;
    }
}
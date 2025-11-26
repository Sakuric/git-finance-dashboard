package com.example.financedashboard.service.crawler;

import com.example.financedashboard.dto.sina.SinaIndustryDTO;
import com.example.financedashboard.mapper.StockInfoMapper;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 新浪财经爬虫服务
 * 用于爬取股票的详细信息（行业、板块、总市值、上市日期等）
 */
@Slf4j
@Service
public class SinaCrawlerService {

    @Autowired
    private StockInfoMapper stockInfoMapper;
    
    @Autowired
    private OkHttpClient okHttpClient;

    // 东方财富公司概况API
    private static final String EASTMONEY_API = "http://emweb.securities.eastmoney.com/PC_HSF10/CompanySurvey/PageAjax";
    private final Random random = new Random();
    private static final int MAX_RETRIES = 3;

    /**
     * 爬取单个股票的详细信息
     *
     * @param stockCode 股票代码（如：000001）
     * @return 股票详细信息DTO
     */
    public SinaIndustryDTO crawlStockInfo(String stockCode) {
        log.info("开始获取股票详细信息: {}", stockCode);

        for (int retry = 0; retry < MAX_RETRIES; retry++) {
            try {
                // 添加随机延迟，避免被封IP
                if (retry > 0) {
                    int delay = 2000 + random.nextInt(3000);
                    log.info("第{}次重试，延迟{}ms", retry + 1, delay);
                    Thread.sleep(delay);
                }

                // 构建东方财富API请求
                String code = buildEastmoneyCode(stockCode);
                String url = String.format("%s?code=%s", EASTMONEY_API, code);
                
                Request request = new Request.Builder()
                        .url(url)
                        .get()
                        .addHeader("Referer", "http://emweb.securities.eastmoney.com/")
                        .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .build();

                try (Response response = okHttpClient.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        log.warn("请求失败: HTTP {}", response.code());
                        continue;
                    }

                    String jsonStr = response.body().string();
                    log.debug("API响应长度: {} 字符", jsonStr.length());
                    
                    JSONObject json = new JSONObject(jsonStr);
                    
                    // 解析基本资料
                    if (!json.has("jbzl") || json.getJSONArray("jbzl").length() == 0) {
                        log.warn("API返回数据为空");
                        continue;
                    }
                    
                    JSONObject jbzl = json.getJSONArray("jbzl").getJSONObject(0);
                    SinaIndustryDTO dto = new SinaIndustryDTO();
                    dto.setStockCode(stockCode);
                    
                    // 股票名称
                    if (jbzl.has("SECURITY_NAME_ABBR") && !jbzl.isNull("SECURITY_NAME_ABBR")) {
                        dto.setStockName(jbzl.getString("SECURITY_NAME_ABBR"));
                        log.info("✓ 股票名称: {}", dto.getStockName());
                    }
                    
                    // 所属行业 (EM2016: 东财行业分类)
                    if (jbzl.has("EM2016") && !jbzl.isNull("EM2016")) {
                        String industry = jbzl.getString("EM2016");
                        // 格式: "金融-银行-股份制与城商行", 取第一级作为行业
                        String[] parts = industry.split("-");
                        dto.setIndustry(parts[0]);
                        // 完整分类作为板块
                        dto.setSector(industry);
                        log.info("✓ 所属行业: {}", dto.getIndustry());
                        log.info("✓ 所属板块: {}", dto.getSector());
                    }
                    
                    // 解析发行相关信息
                    if (json.has("fxxg") && json.getJSONArray("fxxg").length() > 0) {
                        JSONObject fxxg = json.getJSONArray("fxxg").getJSONObject(0);
                        
                        // 上市日期
                        if (fxxg.has("LISTING_DATE") && !fxxg.isNull("LISTING_DATE")) {
                            String dateStr = fxxg.getString("LISTING_DATE");
                            // 格式: "1991-04-03 00:00:00"
                            dto.setListingDate(LocalDate.parse(dateStr.substring(0, 10),
                                    DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                            log.info("✓ 上市日期: {}", dto.getListingDate());
                        }
                    }
                    
                    // 获取总市值 - 使用腾讯财经API
                    try {
                        BigDecimal marketValue = fetchMarketValue(stockCode);
                        if (marketValue != null) {
                            dto.setMarketValue(marketValue);
                            log.info("✓ 总市值: {} 亿元", marketValue.divide(new BigDecimal("100000000"), 2, BigDecimal.ROUND_HALF_UP));
                        }
                    } catch (Exception e) {
                        log.warn("获取总市值失败: {}", e.getMessage());
                    }

                    log.info("✅ 成功获取股票信息: {}", stockCode);
                    return dto;
                }

            } catch (Exception e) {
                log.error("获取股票信息失败 (第{}次尝试): {}, 错误: {}", retry + 1, stockCode, e.getMessage(), e);
                if (retry == MAX_RETRIES - 1) {
                    log.error("达到最大重试次数，放弃获取: {}", stockCode);
                }
            }
        }

        return null;
    }

    /**
     * 更新单个股票的详细信息到数据库
     *
     * @param stockCode 股票代码
     * @return 是否更新成功
     */
    @Transactional
    public boolean updateStockInfo(String stockCode) {
        SinaIndustryDTO dto = crawlStockInfo(stockCode);
        if (dto == null) {
            return false;
        }

        try {
            int rows = stockInfoMapper.updateStockCompleteInfo(
                    stockCode,
                    dto.getIndustry(),
                    dto.getSector(),
                    dto.getMarketValue(),
                    dto.getListingDate()
            );
            log.info("更新股票信息到数据库: {}, 影响行数: {}", stockCode, rows);
            return rows > 0;
        } catch (Exception e) {
            log.error("更新股票信息到数据库失败: {}, 错误: {}", stockCode, e.getMessage());
            return false;
        }
    }

    /**
     * 批量更新所有股票的详细信息
     * 使用安全的爬虫策略，避免IP被封
     *
     * @return 更新结果统计
     */
    @Transactional
    public Map<String, Integer> updateAllStockInfo() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("total", 0);
        stats.put("success", 0);
        stats.put("failed", 0);

        try {
            List<String> stockCodes = stockInfoMapper.getAllStockCodes();
            stats.put("total", stockCodes.size());
            log.info("开始批量更新股票信息，总数: {}", stockCodes.size());
            log.warn("⚠️ 此操作可能需要数小时完成，请耐心等待");

            int count = 0;
            for (String stockCode : stockCodes) {
                count++;

                try {
                    // 每处理50只股票输出一次进度
                    if (count % 50 == 0) {
                        log.info("进度: {}/{} ({}%), 成功: {}, 失败: {}",
                                count, stockCodes.size(),
                                String.format("%.2f", count * 100.0 / stockCodes.size()),
                                stats.get("success"), stats.get("failed"));
                    }

                    // 更新股票信息
                    boolean success = updateStockInfo(stockCode);

                    if (success) {
                        stats.put("success", stats.get("success") + 1);
                    } else {
                        stats.put("failed", stats.get("failed") + 1);
                    }

                    // 🔥 关键：随机延迟5-10秒，避免被识别为爬虫
                    int delay = 5000 + random.nextInt(5000);
                    log.debug("等待 {}ms 后继续...", delay);
                    Thread.sleep(delay);

                } catch (InterruptedException e) {
                    log.error("线程被中断，停止爬取");
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    log.error("处理股票 {} 时出错", stockCode, e);
                    stats.put("failed", stats.get("failed") + 1);

                    // 出错后额外等待10秒
                    try {
                        Thread.sleep(10000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }

            log.info("批量更新完成！总数: {}, 成功: {}, 失败: {}",
                    stats.get("total"), stats.get("success"), stats.get("failed"));

        } catch (Exception e) {
            log.error("批量更新过程中发生错误", e);
        }

        return stats;
    }

    /**
     * 构建东方财富code参数
     * 格式: SZ000001 或 SH600519
     *
     * @param stockCode 股票代码
     * @return code
     */
    private String buildEastmoneyCode(String stockCode) {
        String prefix = stockCode.startsWith("6") ? "SH" : "SZ";
        return prefix + stockCode;
    }
    
    /**
     * 获取股票总市值
     * 使用腾讯财经API获取实时行情,计算总市值
     *
     * @param stockCode 股票代码
     * @return 总市值(元),失败返回null
     */
    private BigDecimal fetchMarketValue(String stockCode) {
        try {
            String prefix = stockCode.startsWith("6") ? "sh" : "sz";
            String url = "http://qt.gtimg.cn/q=" + prefix + stockCode;
            
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .addHeader("Referer", "http://qt.gtimg.cn/")
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .build();
            
            try (Response response = okHttpClient.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    return null;
                }
                
                String result = response.body().string();
                // 格式: v_sz000001="51~平安银行~000001~11.72~...~19405600653~19405918198~..."
                // 倒数第4个字段是总股本(股), 第3个字段是当前价
                
                int start = result.indexOf("\"") + 1;
                int end = result.lastIndexOf("\"");
                if (start < 1 || end < start) {
                    return null;
                }
                
                String data = result.substring(start, end);
                String[] fields = data.split("~");
                
                if (fields.length < 45) {
                    return null;
                }
                
                // 当前价格 (索引3)
                BigDecimal currentPrice = new BigDecimal(fields[3]);
                // 总股本 (索引44, 单位:股)
                BigDecimal totalShares = new BigDecimal(fields[44]);
                
                // 总市值 = 总股本 * 当前价格
                BigDecimal marketValue = totalShares.multiply(currentPrice);
                
                return marketValue;
            }
        } catch (Exception e) {
            log.debug("获取市值失败: {}", e.getMessage());
            return null;
        }
    }
}
package com.example.financedashboard.service.sina;

import com.example.financedashboard.utils.StockCodeUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 新浪财经API调用服务
 * 负责调用新浪财经接口获取股票数据
 */
@Slf4j
@Service
public class SinaApiService {

    @Autowired
    private OkHttpClient okHttpClient;

    @Value("${sina.api.base-url:https://hq.sinajs.cn}")
    private String baseUrl;

    @Value("${sina.api.batch-size:50}")
    private int maxBatchSize;

    private static final String LIST_PATH = "/list=";

    /**
     * 获取单个股票的实时数据
     * 
     * @param stockCode 股票代码（如：600519）
     * @return 原始数据字符串，失败返回null
     */
    public String fetchRealtimeData(String stockCode) {
        if (stockCode == null || stockCode.trim().isEmpty()) {
            log.warn("股票代码为空");
            return null;
        }

        // 转换为完整代码
        String fullCode = StockCodeUtil.toFullCode(stockCode);
        if (fullCode == null) {
            log.warn("股票代码格式错误: {}", stockCode);
            return null;
        }

        String url = baseUrl + LIST_PATH + fullCode;
        log.debug("请求URL: {}", url);

        return executeRequest(url);
    }

    /**
     * 批量获取多个股票的实时数据
     * 
     * @param stockCodes 股票代码列表
     * @return 原始数据字符串，失败返回null
     */
    public String fetchBatchRealtimeData(List<String> stockCodes) {
        if (stockCodes == null || stockCodes.isEmpty()) {
            log.warn("股票代码列表为空");
            return null;
        }

        // 限制批量大小
        if (stockCodes.size() > maxBatchSize) {
            log.warn("批量查询数量超过限制: {}, 将只查询前{}个", 
                    stockCodes.size(), maxBatchSize);
            stockCodes = stockCodes.subList(0, maxBatchSize);
        }

        // 转换为完整代码并拼接
        String codes = stockCodes.stream()
                .map(StockCodeUtil::toFullCode)
                .filter(code -> code != null)
                .collect(Collectors.joining(","));

        if (codes.isEmpty()) {
            log.warn("没有有效的股票代码");
            return null;
        }

        String url = baseUrl + LIST_PATH + codes;
        log.debug("批量请求URL: {}, 股票数量: {}", url, stockCodes.size());

        return executeRequest(url);
    }
    /**
     * 获取所有A股列表
     * @return 包含股票代码和名称的Map列表
     */
    public List<Map<String, String>> fetchAllStockCodes() {
        List<Map<String, String>> stockList = new ArrayList<>();
        
        log.info("开始从新浪财经获取所有A股列表...");
        
        // 分别获取上海A股和深圳A股，使用分页方式
        String[] markets = {"sh_a", "sz_a"};
        String[] marketNames = {"上海A股", "深圳A股"};
        
        for (int m = 0; m < markets.length; m++) {
            String market = markets[m];
            String marketName = marketNames[m];
            int page = 1;
            int pageSize = 100; // 新浪接口每次最多返回100条
            boolean hasMoreData = true;
            
            log.info("开始获取{}数据...", marketName);
            
            while (hasMoreData) {
                String url = String.format(
                    "http://vip.stock.finance.sina.com.cn/quotes_service/api/json_v2.php/Market_Center.getHQNodeData?page=%d&num=%d&sort=symbol&asc=1&node=%s&symbol=&_s_r_a=page",
                    page, pageSize, market
                );
                
                log.info("正在请求{}第{}页数据...", marketName, page);
                
                String response = executeRequest(url);
                if (response == null) {
                    log.error("获取{}第{}页数据失败", marketName, page);
                    break;
                }
                
                try {
                    // 新浪返回的是直接的JSON数组，不需要JSONP解析
                    JSONArray jsonArray = new JSONArray(response);
                    
                    if (jsonArray.length() == 0) {
                        log.info("{}第{}页没有数据，获取结束", marketName, page);
                        hasMoreData = false;
                        break;
                    }
                    
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject stock = jsonArray.getJSONObject(i);
                        Map<String, String> stockMap = new HashMap<>();
                        
                        // 新浪返回的代码格式是sh600519或sz000001，我们需要转换为600519或000001
                        String fullCode = stock.getString("symbol");
                        String code = fullCode.substring(2);
                        
                        stockMap.put("code", code);
                        stockMap.put("name", stock.getString("name"));
                        stockMap.put("exchange", market.startsWith("sh") ? "SH" : "SZ");
                        stockList.add(stockMap);
                    }
                    
                    log.info("{}第{}页获取到 {} 只A股", marketName, page, jsonArray.length());
                    
                    // 如果返回的数据少于pageSize，说明已经是最后一页了
                    if (jsonArray.length() < pageSize) {
                        hasMoreData = false;
                    } else {
                        page++;
                    }
                    
                    // 防止请求过于频繁
                    Thread.sleep(300);
                    
                } catch (Exception e) {
                    log.error("解析{}第{}页JSON失败, response: {}", marketName, page, response, e);
                    break;
                }
            }
        }
        
        log.info("成功从新浪财经获取到总共 {} 只A股", stockList.size());
        return stockList;
    }

    /**
     * 执行HTTP请求
     * 
     * @param url 请求URL
     * @return 响应内容，失败返回null
     */
    private String executeRequest(String url) {
        String referer = "https://finance.sina.com.cn/";
        if (url.contains("eastmoney.com")) {
            referer = "https://quote.eastmoney.com/";
        }

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Referer", referer)
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .addHeader("Accept", "*/*")
                .addHeader("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                // 移除 Accept-Encoding，让OkHttp自动处理压缩
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String result = response.body().string();
                log.debug("请求成功，响应长度: {}", result.length());
                return result;
            } else {
                log.error("请求失败: {}, HTTP状态码: {}", url, response.code());
                return null;
            }
        } catch (IOException e) {
            log.error("请求异常: {}", url, e);
            return null;
        }
    }

    /**
     * 测试API连接是否正常
     * 
     * @return true-连接正常，false-连接异常
     */
    public boolean testConnection() {
        try {
            // 使用贵州茅台(600519)测试
            String result = fetchRealtimeData("600519");
            return result != null && result.contains("贵州茅台");
        } catch (Exception e) {
            log.error("测试连接失败", e);
            return false;
        }
    }
}
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
import java.util.List;
import java.util.stream.Collectors;

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
     * 执行HTTP请求
     * 
     * @param url 请求URL
     * @return 响应内容，失败返回null
     */
    private String executeRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Referer", "https://finance.sina.com.cn")
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
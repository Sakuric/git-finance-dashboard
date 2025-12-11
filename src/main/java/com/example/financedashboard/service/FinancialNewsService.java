package com.example.financedashboard.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialNewsService {

    private final OkHttpClient httpClient;
    private static final ConcurrentHashMap<String, CachedNews> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = TimeUnit.HOURS.toMillis(6);

    public String getLatestNews(List<String> stockCodes, String industries) {
        String cacheKey = String.join(",", stockCodes) + "_" + industries;
        CachedNews cached = cache.get(cacheKey);
        if (cached != null && System.currentTimeMillis() - cached.timestamp < CACHE_DURATION) {
            return cached.content;
        }

        String news = fetchNews(stockCodes, industries);
        cache.put(cacheKey, new CachedNews(news, System.currentTimeMillis()));
        return news;
    }

    private String fetchNews(List<String> stockCodes, String industries) {
        StringBuilder news = new StringBuilder("=== 最新财经资讯 ===\n\n");

        news.append("【国际形势】\n").append(fetchGlobalNews()).append("\n");
        news.append("【国家政策】\n").append(fetchNationalNews()).append("\n");
        news.append("【行业动态】\n").append(fetchIndustryNews(industries)).append("\n");
        news.append("【个股新闻】\n");
        for (String stockCode : stockCodes) {
            String stockNews = fetchStockNews(stockCode);
            if (!stockNews.isEmpty()) {
                news.append(stockNews);
            }
        }

        return news.toString();
    }

    private String fetchGlobalNews() {
        Request request = new Request.Builder()
                .url("https://finance.sina.com.cn/roll/index.d.html?cid=56506&page=1")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return "暂无";
            return parseNewsFromHtml(response.body().string(), 2);
        } catch (Exception e) {
            return "暂无";
        }
    }

    private String fetchNationalNews() {
        Request request = new Request.Builder()
                .url("https://finance.sina.com.cn/roll/index.d.html?cid=56507&page=1")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return "暂无";
            return parseNewsFromHtml(response.body().string(), 2);
        } catch (Exception e) {
            return "暂无";
        }
    }

    private String fetchIndustryNews(String industries) {
        Request request = new Request.Builder()
                .url("https://finance.sina.com.cn/roll/index.d.html?cid=56516&page=1")
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return "暂无";
            return parseNewsFromHtml(response.body().string(), 3);
        } catch (Exception e) {
            return "暂无";
        }
    }

    private String parseNewsFromHtml(String html, int count) {
        StringBuilder result = new StringBuilder();
        String[] lines = html.split("<li>");
        int found = 0;
        for (String line : lines) {
            if (found >= count) break;
            if (line.contains("<a href=") && line.contains("</a>")) {
                int start = line.indexOf(">", line.indexOf("<a")) + 1;
                int end = line.indexOf("</a>");
                if (start > 0 && end > start) {
                    String title = line.substring(start, end).trim();
                    if (!title.isEmpty() && title.length() > 5 && !title.contains("<")) {
                        result.append("- ").append(title).append("\n");
                        found++;
                    }
                }
            }
        }
        return result.length() > 0 ? result.toString() : "暂无\n";
    }

    private String fetchStockNews(String stockCode) {
        String cleanCode = stockCode.replace("sh", "").replace("sz", "");
        Request request = new Request.Builder()
                .url("https://vip.stock.finance.sina.com.cn/corp/view/vCB_AllNewsStock.php?symbol=" + cleanCode)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return "";
            }

            String html = response.body().string();
            StringBuilder stockNews = new StringBuilder();

            String[] lines = html.split("<a");
            int count = 0;
            for (String line : lines) {
                if (count >= 3) break;
                if (line.contains("target=_blank>") && line.contains("</a>")) {
                    int start = line.indexOf("target=_blank>") + 14;
                    int end = line.indexOf("</a>");
                    if (start > 0 && end > start) {
                        String title = line.substring(start, end).trim();
                        if (!title.isEmpty() && title.length() > 5 && !title.contains("<")) {
                            stockNews.append("- ").append(title).append("\n");
                            count++;
                        }
                    }
                }
            }

            return stockNews.toString();
        } catch (Exception e) {
            log.error("获取股票{}新闻失败", stockCode, e);
            return "";
        }
    }

    private static class CachedNews {
        final String content;
        final long timestamp;

        CachedNews(String content, long timestamp) {
            this.content = content;
            this.timestamp = timestamp;
        }
    }
}

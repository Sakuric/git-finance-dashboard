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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinancialNewsService {

    private final OkHttpClient httpClient;
    private static final ConcurrentHashMap<String, CachedNews> cache = new ConcurrentHashMap<>();
    private static final long CACHE_DURATION = TimeUnit.HOURS.toMillis(3);

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

        news.append("【国际形势】\n").append(fetchGlobalNewsMultiSource()).append("\n");
        news.append("【国家政策】\n").append(fetchNationalNewsMultiSource()).append("\n");
        news.append("【行业动态】\n").append(fetchIndustryNewsMultiSource(industries)).append("\n");
        news.append("【个股新闻】\n").append(fetchStockNewsMultiSource(stockCodes)).append("\n");

        return news.toString();
    }

    private String fetchGlobalNewsMultiSource() {
        List<String> allNews = new ArrayList<>();
        allNews.addAll(fetchAllNews("https://finance.sina.com.cn/roll/index.d.html?cid=56506&page=1", 30));
        allNews.addAll(fetchAllNews("https://finance.eastmoney.com/a/cgjjxw.html", 30));

        return filterByKeywords(allNews,
            List.of("美联储", "美国", "欧洲", "欧央行", "日本", "日央行", "英国", "全球", "国际", "IMF", "世界银行", "G20", "OPEC", "WTO"),
            "国际形势", 10);
    }

    private String fetchNationalNewsMultiSource() {
        List<String> allNews = new ArrayList<>();
        allNews.addAll(fetchAllNews("https://finance.sina.com.cn/roll/index.d.html?cid=56507&page=1", 30));
        allNews.addAll(fetchAllNews("https://finance.eastmoney.com/a/ccjpl.html", 30));

        return filterByKeywords(allNews,
            List.of("央行", "财政部", "发改委", "工信部", "证监会", "银保监", "国务院", "政策", "降息", "降准", "减税", "补贴", "监管", "改革"),
            "国家政策", 10);
    }

    private String fetchIndustryNewsMultiSource(String industries) {
        List<String> allNews = new ArrayList<>();
        allNews.addAll(fetchAllNews("https://finance.sina.com.cn/roll/index.d.html?cid=56516&page=1", 30));
        allNews.addAll(fetchAllNews("https://finance.eastmoney.com/a/chyjj.html", 30));

        List<String> keywords = new ArrayList<>(List.of("芯片", "半导体", "新能源", "汽车", "AI", "人工智能", "医药", "地产", "银行", "保险", "消费", "科技"));
        if (industries != null && !industries.isEmpty()) {
            keywords.addAll(List.of(industries.split("[,，]")));
        }

        return filterByKeywords(allNews, keywords, "行业动态", 12);
    }

    private List<String> fetchAllNews(String url, int limit) {
        List<String> news = new ArrayList<>();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) return news;

            String html = response.body().string();
            String[] lines = html.split("<li>");
            int found = 0;

            for (String line : lines) {
                if (found >= limit) break;
                if (line.contains("<a href=") && line.contains("</a>")) {
                    try {
                        int start = line.indexOf(">", line.indexOf("<a")) + 1;
                        int end = line.indexOf("</a>");
                        if (start > 0 && end > start && end < line.length()) {
                            String title = line.substring(start, end)
                                    .replaceAll("<[^>]+>", "")
                                    .replaceAll("&nbsp;", " ")
                                    .replaceAll("&quot;", "\"")
                                    .replaceAll("&amp;", "&")
                                    .trim();
                            if (!title.isEmpty() && title.length() > 5 && title.length() < 200) {
                                news.add(title);
                                found++;
                            }
                        }
                    } catch (Exception e) {
                        log.debug("解析新闻失败", e);
                    }
                }
            }
        } catch (Exception e) {
            log.debug("获取新闻失败: {}", url, e);
        }

        return news;
    }

    private String filterByKeywords(List<String> allNews, List<String> keywords, String category, int targetCount) {
        Map<String, Integer> matched = new LinkedHashMap<>();

        for (String news : allNews) {
            for (String keyword : keywords) {
                if (news.contains(keyword)) {
                    matched.putIfAbsent(news, 1);
                    break;
                }
            }
            if (matched.size() >= targetCount) break;
        }

        if (matched.isEmpty()) {
            return allNews.stream().limit(targetCount).map(n -> "- " + n + "\n").collect(Collectors.joining());
        }

        return matched.keySet().stream().map(n -> "- " + n + "\n").collect(Collectors.joining());
    }

    private String fetchStockNewsMultiSource(List<String> stockCodes) {
        StringBuilder result = new StringBuilder();
        for (String stockCode : stockCodes) {
            String stockNews = fetchStockNews(stockCode);
            if (!stockNews.isEmpty()) {
                result.append(stockNews);
            }
        }
        return result.toString();
    }


    private String fetchStockNews(String stockCode) {
        String cleanCode = stockCode.replace("sh", "").replace("sz", "");
        Request request = new Request.Builder()
                .url("https://vip.stock.finance.sina.com.cn/corp/view/vCB_AllNewsStock.php?symbol=" + cleanCode)
                .addHeader("User-Agent", "Mozilla/5.0")
                .build();

        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                log.warn("获取股票{}新闻失败: HTTP {}", stockCode, response.code());
                return "";
            }

            String html = response.body().string();
            StringBuilder stockNews = new StringBuilder();

            String[] lines = html.split("<a");
            int count = 0;
            for (String line : lines) {
                if (count >= 8) break;
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

            return stockNews.length() > 0 ? stockNews.toString() : "";
        } catch (Exception e) {
            log.error("获取股票{}新闻异常", stockCode, e);
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

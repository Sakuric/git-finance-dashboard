package com.example.financedashboard.service.eastmoney;

import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EastMoneyApiService {

    @Autowired
    private OkHttpClient okHttpClient;

    public String fetchIndexKLine(String indexCode, int days) {
        String secid = convertToSecid(indexCode);
        String url = String.format(
            "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58&klt=101&fqt=0&end=20500101&lmt=%d",
            secid, days
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Referer", "https://quote.eastmoney.com/")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("获取东方财富K线失败: {}", indexCode, e);
        }
        return null;
    }

    public String fetchStockKLine(String stockCode, int days) {
        String secid = convertToSecid(stockCode);
        String url = String.format(
            "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&klt=101&fqt=1&end=20500101&lmt=%d",
            secid, days
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Referer", "https://quote.eastmoney.com/")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("获取股票K线失败: {}", stockCode, e);
        }
        return null;
    }

    public String fetchStockQuote(String stockCode) {
        String secid = convertToSecid(stockCode);
        String url = String.format(
            "https://push2.eastmoney.com/api/qt/stock/get?secid=%s&fields=f43,f168,f162,f116,f117",
            secid
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Referer", "https://quote.eastmoney.com/")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String body = response.body().string();
                log.info("东方财富API响应: {}", body);
                return body;
            }
            log.error("东方财富API请求失败: code={}", response.code());
        } catch (Exception e) {
            log.error("获取东方财富行情失败: {}", stockCode, e);
        }
        return null;
    }

    private String convertToSecid(String code) {
        if (code.startsWith("sh")) {
            return "1." + code.substring(2);
        }
        if (code.startsWith("sz")) {
            return "0." + code.substring(2);
        }
        if (code.startsWith("6") || code.equals("000001") || code.equals("000016") || code.equals("000300")) {
            return "1." + code;
        }
        return "0." + code;
    }

    public String fetchIndexTimeline(String indexCode) {
        String secid = convertToSecid(indexCode);
        String url = String.format(
            "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&klt=1&fqt=0&end=20500101&lmt=240",
            secid
        );

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Referer", "https://quote.eastmoney.com/")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("获取分时数据失败: {}", indexCode, e);
        }
        return null;
    }

    public String fetchMarketOverview() {
        String url = "https://push2.eastmoney.com/api/qt/ulist.np/get?fltt=2&invt=2&fields=f2,f3,f12,f13,f14&secids=1.000001,0.399001,0.399006&ut=fa5fd1943c7b386f172d6893dbfba10b";

        Request request = new Request.Builder()
            .url(url)
            .addHeader("User-Agent", "Mozilla/5.0")
            .addHeader("Referer", "https://quote.eastmoney.com/")
            .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                return response.body().string();
            }
        } catch (Exception e) {
            log.error("获取市场概览失败", e);
        }
        return null;
    }
}

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

    private String convertToSecid(String code) {
        if (code.startsWith("sh") || code.startsWith("sz")) {
            code = code.substring(2);
        }
        if (code.startsWith("399") || code.startsWith("159")) {
            return "0." + code;
        }
        return "1." + code;
    }
}

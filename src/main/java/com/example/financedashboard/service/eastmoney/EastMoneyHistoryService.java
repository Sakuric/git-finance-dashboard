package com.example.financedashboard.service.eastmoney;

import com.example.financedashboard.entity.StockHistory;
import com.example.financedashboard.mapper.StockHistoryMapper;
import com.example.financedashboard.mapper.StockInfoMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EastMoneyHistoryService {

    private final EastMoneyApiService eastMoneyApiService;
    private final StockHistoryMapper stockHistoryMapper;
    private final StockInfoMapper stockInfoMapper;

    public boolean syncStockHistory(String stockCode, int days) {
        try {
            Long stockId = stockInfoMapper.findIdByCode(stockCode);
            if (stockId == null) {
                log.warn("股票 {} 不存在于stock_info表中，跳过同步", stockCode);
                return false;
            }

            String response = eastMoneyApiService.fetchStockKLine(stockCode, days);
            if (response == null) return false;

            JSONObject json = new JSONObject(response);
            if (json.getInt("rc") != 0) return false;

            JSONObject data = json.getJSONObject("data");
            JSONArray klines = data.getJSONArray("klines");

            List<StockHistory> historyList = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

            for (int i = 0; i < klines.length(); i++) {
                String kline = klines.getString(i);
                String[] parts = kline.split(",");

                StockHistory history = new StockHistory();
                history.setStockCode(stockCode);
                history.setStockId(stockId);
                history.setTradeDate(LocalDate.parse(parts[0], formatter));
                history.setOpenPrice(new BigDecimal(parts[1]));
                history.setClosePrice(new BigDecimal(parts[2]));
                history.setHighPrice(new BigDecimal(parts[3]));
                history.setLowPrice(new BigDecimal(parts[4]));
                history.setVolume(Long.parseLong(parts[5]));
                history.setAmount(new BigDecimal(parts[6]));

                historyList.add(history);
            }

            for (StockHistory history : historyList) {
                stockHistoryMapper.insertOrUpdate(history);
            }

            log.info("同步股票 {} 历史数据成功，共 {} 条", stockCode, historyList.size());
            return true;
        } catch (Exception e) {
            log.error("同步股票 {} 历史数据失败", stockCode, e);
            return false;
        }
    }
}

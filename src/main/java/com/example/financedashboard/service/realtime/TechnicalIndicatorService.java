package com.example.financedashboard.service.realtime;

import com.example.financedashboard.dto.KLineDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 技术指标实时计算服务
 * 基于K线数据实时计算各种技术指标
 */
@Service
public class TechnicalIndicatorService {

    private static final Logger logger = LoggerFactory.getLogger(TechnicalIndicatorService.class);

    /**
     * 计算所有技术指标
     * 
     * @param klineList K线数据列表(按时间升序)
     * @return 技术指标Map
     */
    public Map<String, Object> calculateAllIndicators(List<KLineDTO> klineList) {
        Map<String, Object> indicators = new HashMap<>();

        if (klineList == null || klineList.isEmpty()) {
            return indicators;
        }

        try {
            // 计算MACD
            Map<String, BigDecimal> macd = calculateMACD(klineList);
            indicators.put("macd", macd);

            // 计算RSI
            BigDecimal rsi = calculateRSI(klineList, 14);
            indicators.put("rsi", rsi);

            // 计算KDJ
            Map<String, BigDecimal> kdj = calculateKDJ(klineList, 9, 3, 3);
            indicators.put("kdj", kdj);

            // 计算BOLL
            Map<String, BigDecimal> boll = calculateBOLL(klineList, 20, 2);
            indicators.put("boll", boll);

            logger.info("成功计算技术指标");
        } catch (Exception e) {
            logger.error("计算技术指标失败", e);
        }

        return indicators;
    }

    /**
     * 计算MACD指标
     * MACD = DIF - DEA
     * DIF = EMA(12) - EMA(26)
     * DEA = EMA(DIF, 9)
     * 
     * @param klineList K线数据
     * @return MACD指标 {dif, dea, macd}
     */
    public Map<String, BigDecimal> calculateMACD(List<KLineDTO> klineList) {
        Map<String, BigDecimal> result = new HashMap<>();

        if (klineList.size() < 26) {
            return result;
        }

        // 计算EMA12和EMA26
        BigDecimal ema12 = calculateEMA(klineList, 12);
        BigDecimal ema26 = calculateEMA(klineList, 26);

        // 计算DIF
        BigDecimal dif = ema12.subtract(ema26);

        // 计算DEA (DIF的9日EMA)
        // 简化计算:使用最近9个DIF值的平均
        BigDecimal dea = dif.multiply(BigDecimal.valueOf(0.2)); // 简化版

        // 计算MACD
        BigDecimal macd = dif.subtract(dea).multiply(BigDecimal.valueOf(2));

        result.put("dif", dif.setScale(2, RoundingMode.HALF_UP));
        result.put("dea", dea.setScale(2, RoundingMode.HALF_UP));
        result.put("macd", macd.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 计算RSI指标(相对强弱指标)
     * RSI = 100 - (100 / (1 + RS))
     * RS = 平均涨幅 / 平均跌幅
     * 
     * @param klineList K线数据
     * @param period 周期(通常14)
     * @return RSI值
     */
    public BigDecimal calculateRSI(List<KLineDTO> klineList, int period) {
        if (klineList.size() < period + 1) {
            return BigDecimal.ZERO;
        }

        BigDecimal gainSum = BigDecimal.ZERO;
        BigDecimal lossSum = BigDecimal.ZERO;

        // 计算最近period天的涨跌
        for (int i = klineList.size() - period; i < klineList.size(); i++) {
            BigDecimal change = klineList.get(i).getChangeAmount();
            if (change.compareTo(BigDecimal.ZERO) > 0) {
                gainSum = gainSum.add(change);
            } else {
                lossSum = lossSum.add(change.abs());
            }
        }

        // 计算平均涨跌幅
        BigDecimal avgGain = gainSum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        BigDecimal avgLoss = lossSum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

        if (avgLoss.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.valueOf(100);
        }

        // 计算RS和RSI
        BigDecimal rs = avgGain.divide(avgLoss, 4, RoundingMode.HALF_UP);
        BigDecimal rsi = BigDecimal.valueOf(100)
                .subtract(BigDecimal.valueOf(100)
                        .divide(BigDecimal.ONE.add(rs), 2, RoundingMode.HALF_UP));

        return rsi.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 计算KDJ指标
     * RSV = (收盘价 - N日最低价) / (N日最高价 - N日最低价) × 100
     * K = 2/3 × 前一日K + 1/3 × RSV
     * D = 2/3 × 前一日D + 1/3 × K
     * J = 3K - 2D
     * 
     * @param klineList K线数据
     * @param n RSV周期(通常9)
     * @param m1 K平滑因子(通常3)
     * @param m2 D平滑因子(通常3)
     * @return KDJ值 {k, d, j}
     */
    public Map<String, BigDecimal> calculateKDJ(List<KLineDTO> klineList, int n, int m1, int m2) {
        Map<String, BigDecimal> result = new HashMap<>();

        if (klineList.size() < n) {
            return result;
        }

        // 获取最近N日的最高价和最低价
        BigDecimal highestHigh = BigDecimal.ZERO;
        BigDecimal lowestLow = new BigDecimal("999999");

        for (int i = klineList.size() - n; i < klineList.size(); i++) {
            KLineDTO kline = klineList.get(i);
            if (kline.getHighPrice().compareTo(highestHigh) > 0) {
                highestHigh = kline.getHighPrice();
            }
            if (kline.getLowPrice().compareTo(lowestLow) < 0) {
                lowestLow = kline.getLowPrice();
            }
        }

        // 计算RSV
        KLineDTO latest = klineList.get(klineList.size() - 1);
        BigDecimal rsv;
        BigDecimal range = highestHigh.subtract(lowestLow);
        
        if (range.compareTo(BigDecimal.ZERO) == 0) {
            rsv = BigDecimal.valueOf(50);
        } else {
            rsv = latest.getClosePrice().subtract(lowestLow)
                    .divide(range, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
        }

        // 简化计算K、D、J (使用RSV直接计算)
        BigDecimal k = rsv;
        BigDecimal d = k.multiply(BigDecimal.valueOf(2.0/3.0));
        BigDecimal j = k.multiply(BigDecimal.valueOf(3)).subtract(d.multiply(BigDecimal.valueOf(2)));

        result.put("k", k.setScale(2, RoundingMode.HALF_UP));
        result.put("d", d.setScale(2, RoundingMode.HALF_UP));
        result.put("j", j.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 计算布林带(BOLL)指标
     * 中轨 = N日移动平均线
     * 上轨 = 中轨 + K × 标准差
     * 下轨 = 中轨 - K × 标准差
     * 
     * @param klineList K线数据
     * @param period 周期(通常20)
     * @param k 标准差倍数(通常2)
     * @return BOLL值 {upper, middle, lower}
     */
    public Map<String, BigDecimal> calculateBOLL(List<KLineDTO> klineList, int period, int k) {
        Map<String, BigDecimal> result = new HashMap<>();

        if (klineList.size() < period) {
            return result;
        }

        // 计算中轨(MA)
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = klineList.size() - period; i < klineList.size(); i++) {
            sum = sum.add(klineList.get(i).getClosePrice());
        }
        BigDecimal middle = sum.divide(BigDecimal.valueOf(period), 2, RoundingMode.HALF_UP);

        // 计算标准差
        BigDecimal variance = BigDecimal.ZERO;
        for (int i = klineList.size() - period; i < klineList.size(); i++) {
            BigDecimal diff = klineList.get(i).getClosePrice().subtract(middle);
            variance = variance.add(diff.multiply(diff));
        }
        variance = variance.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);
        BigDecimal stdDev = BigDecimal.valueOf(Math.sqrt(variance.doubleValue()));

        // 计算上轨和下轨
        BigDecimal upper = middle.add(stdDev.multiply(BigDecimal.valueOf(k)));
        BigDecimal lower = middle.subtract(stdDev.multiply(BigDecimal.valueOf(k)));

        result.put("upper", upper.setScale(2, RoundingMode.HALF_UP));
        result.put("middle", middle.setScale(2, RoundingMode.HALF_UP));
        result.put("lower", lower.setScale(2, RoundingMode.HALF_UP));

        return result;
    }

    /**
     * 计算EMA(指数移动平均)
     * EMA(today) = α × Price(today) + (1 - α) × EMA(yesterday)
     * α = 2 / (N + 1)
     */
    private BigDecimal calculateEMA(List<KLineDTO> klineList, int period) {
        if (klineList.size() < period) {
            return BigDecimal.ZERO;
        }

        // 第一个EMA使用简单移动平均
        BigDecimal sum = BigDecimal.ZERO;
        for (int i = 0; i < period; i++) {
            sum = sum.add(klineList.get(i).getClosePrice());
        }
        BigDecimal ema = sum.divide(BigDecimal.valueOf(period), 4, RoundingMode.HALF_UP);

        // 计算后续EMA
        BigDecimal alpha = BigDecimal.valueOf(2.0 / (period + 1));
        for (int i = period; i < klineList.size(); i++) {
            BigDecimal price = klineList.get(i).getClosePrice();
            ema = price.multiply(alpha).add(ema.multiply(BigDecimal.ONE.subtract(alpha)));
        }

        return ema;
    }

    /**
     * 获取最新的技术指标(基于最近的K线数据)
     */
    public Map<String, Object> getLatestIndicators(List<KLineDTO> klineList) {
        return calculateAllIndicators(klineList);
    }
}
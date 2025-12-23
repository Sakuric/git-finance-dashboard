package com.example.financedashboard.service;

import com.example.financedashboard.dto.BacktestRequestDTO;
import com.example.financedashboard.dto.BacktestResponseDTO;
import com.example.financedashboard.entity.*;
import com.example.financedashboard.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvancedBacktestService {

    private final StructuredAdviceMapper structuredAdviceMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final com.example.financedashboard.service.eastmoney.EastMoneyHistoryService eastMoneyHistoryService;

    public BacktestResponseDTO runAdvancedBacktest(BacktestRequestDTO request) {
        log.info("开始回测，参数: adviceId={}, startDate={}, endDate={}, initialCapital={}, trainRatio={}",
            request.getAdviceId(), request.getStartDate(), request.getEndDate(),
            request.getInitialCapital(), request.getTrainRatio());

        List<StructuredAdvice> advices = structuredAdviceMapper.findByAdviceId(request.getAdviceId());
        log.info("找到{}条投资建议", advices.size());

        if (advices.isEmpty()) {
            throw new RuntimeException("未找到投资建议");
        }

        // 预先同步所有需要的股票历史数据
        for (StructuredAdvice advice : advices) {
            log.info("同步股票历史数据: {}", advice.getStockCode());
            eastMoneyHistoryService.syncStockHistory(advice.getStockCode(), 730);
        }

        long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        long trainDays = (long) (totalDays * request.getTrainRatio().doubleValue());
        LocalDate splitDate = request.getStartDate().plusDays(trainDays);

        BacktestResponseDTO.PeriodResult trainResult = runPeriodBacktest(
            advices, request.getStartDate(), splitDate, request.getInitialCapital(), "训练期"
        );

        BacktestResponseDTO.PeriodResult testResult = runPeriodBacktest(
            advices, splitDate.plusDays(1), request.getEndDate(), request.getInitialCapital(), "测试期"
        );

        BacktestResponseDTO.OverfittingDetection overfitting = detectOverfitting(trainResult, testResult);

        // 生成完整的收益曲线（训练期+测试期）
        List<BacktestResponseDTO.EquityPoint> equityCurve = new ArrayList<>();
        equityCurve.addAll(trainResult.getEquityCurve());
        equityCurve.addAll(testResult.getEquityCurve());

        BacktestResponseDTO response = new BacktestResponseDTO();
        response.setTrainPeriod(trainResult);
        response.setTestPeriod(testResult);
        response.setOverfitting(overfitting);
        response.setEquityCurve(equityCurve);

        // AI分析
        String aiAnalysis = generateAIAnalysis(trainResult, testResult, overfitting, request);
        response.setAiAnalysis(aiAnalysis);

        return response;
    }

    private BacktestResponseDTO.PeriodResult runPeriodBacktest(
        List<StructuredAdvice> advices, LocalDate startDate, LocalDate endDate,
        BigDecimal initialCapital, String periodName
    ) {
        log.info("开始{}回测: {} 到 {}, 初始资金: {}", periodName, startDate, endDate, initialCapital);

        BigDecimal cash = initialCapital;
        Map<String, Position> positions = new HashMap<>();
        List<BigDecimal> dailyValues = new ArrayList<>();
        int winCount = 0, lossCount = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (StructuredAdvice advice : advices) {
                if (!"BUY".equals(advice.getSuggestedAction())) continue;

                List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
                    advice.getStockCode(), date, date
                );
                if (history.isEmpty()) {
                    log.debug("日期{}没有股票{}的数据", date, advice.getStockCode());
                    continue;
                }

                StockHistory todayData = history.get(0);

                if (positions.containsKey(advice.getStockCode())) {
                    Position pos = positions.get(advice.getStockCode());

                    // 止损：价格下跌超过5%
                    BigDecimal stopLossThreshold = pos.buyPrice.multiply(new BigDecimal("0.95"));
                    if (todayData.getLowPrice().compareTo(stopLossThreshold) <= 0) {
                        BigDecimal sellPrice = stopLossThreshold;
                        cash = cash.add(sellPrice.multiply(new BigDecimal(pos.shares)));
                        if (sellPrice.compareTo(pos.buyPrice) > 0) winCount++;
                        else lossCount++;
                        log.info("止损卖出: {} 股票={}, 买入价={}, 卖出价={}", date, advice.getStockCode(), pos.buyPrice, sellPrice);
                        positions.remove(advice.getStockCode());
                    }
                    // 止盈：价格上涨超过10%
                    else if (todayData.getHighPrice().compareTo(pos.buyPrice.multiply(new BigDecimal("1.10"))) >= 0) {
                        BigDecimal sellPrice = pos.buyPrice.multiply(new BigDecimal("1.10"));
                        cash = cash.add(sellPrice.multiply(new BigDecimal(pos.shares)));
                        winCount++;
                        log.info("止盈卖出: {} 股票={}, 买入价={}, 卖出价={}", date, advice.getStockCode(), pos.buyPrice, sellPrice);
                        positions.remove(advice.getStockCode());
                    }
                } else {
                    // 买入逻辑：使用当前价格而不是建议价格（因为建议价格可能已过时）
                    BigDecimal buyPrice = todayData.getClosePrice();
                    BigDecimal positionSize = cash.multiply(new BigDecimal("0.10"));
                    int shares = positionSize.divide(buyPrice, 0, RoundingMode.DOWN).intValue();

                    if (shares > 0) {
                        cash = cash.subtract(buyPrice.multiply(new BigDecimal(shares)));
                        positions.put(advice.getStockCode(), new Position(shares, buyPrice));
                        log.info("买入: {} 股票={}, 价格={}, 数量={}", date, advice.getStockCode(), buyPrice, shares);
                    }
                }
            }

            BigDecimal totalValue = cash;
            for (Map.Entry<String, Position> entry : positions.entrySet()) {
                BigDecimal currentPrice = getCurrentPrice(entry.getKey(), date);
                totalValue = totalValue.add(currentPrice.multiply(new BigDecimal(entry.getValue().shares)));
            }
            dailyValues.add(totalValue);
        }

        for (String stockCode : new HashSet<>(positions.keySet())) {
            Position pos = positions.get(stockCode);
            BigDecimal closePrice = getCurrentPrice(stockCode, endDate);
            cash = cash.add(closePrice.multiply(new BigDecimal(pos.shares)));
            if (closePrice.compareTo(pos.buyPrice) > 0) winCount++;
            else lossCount++;
        }

        log.info("{}回测完成: 交易次数={}, 胜率={}/{}, 每日价值数量={}",
            periodName, winCount + lossCount, winCount, winCount + lossCount, dailyValues.size());

        BacktestResponseDTO.PeriodResult result = new BacktestResponseDTO.PeriodResult();
        result.setPeriod(periodName);

        BigDecimal finalValue = dailyValues.isEmpty() ? initialCapital : dailyValues.get(dailyValues.size() - 1);
        log.info("{}最终价值: {}, 初始资金: {}", periodName, finalValue, initialCapital);

        result.setTotalReturn(finalValue.subtract(initialCapital).divide(initialCapital, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")));

        long days = ChronoUnit.DAYS.between(startDate, endDate);
        result.setAnnualReturn(result.getTotalReturn().multiply(new BigDecimal("365")).divide(new BigDecimal(days), 2, RoundingMode.HALF_UP));

        result.setMaxDrawdown(calculateMaxDrawdown(dailyValues));
        result.setSharpeRatio(calculateSharpeRatio(dailyValues, initialCapital));

        int totalTrades = winCount + lossCount;
        result.setWinRate(totalTrades > 0 ?
            new BigDecimal(winCount).divide(new BigDecimal(totalTrades), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100")) :
            BigDecimal.ZERO
        );
        result.setTradeCount(totalTrades);

        // 生成收益曲线数据
        List<BacktestResponseDTO.EquityPoint> equityCurve = new ArrayList<>();
        LocalDate currentDate = startDate;
        for (BigDecimal value : dailyValues) {
            BacktestResponseDTO.EquityPoint point = new BacktestResponseDTO.EquityPoint();
            point.setDate(currentDate.toString());
            point.setValue(value);
            equityCurve.add(point);
            currentDate = currentDate.plusDays(1);
        }
        result.setEquityCurve(equityCurve);

        return result;
    }

    private BacktestResponseDTO.OverfittingDetection detectOverfitting(
        BacktestResponseDTO.PeriodResult train, BacktestResponseDTO.PeriodResult test
    ) {
        BacktestResponseDTO.OverfittingDetection detection = new BacktestResponseDTO.OverfittingDetection();

        BigDecimal degradation = train.getTotalReturn().subtract(test.getTotalReturn())
            .divide(train.getTotalReturn().abs().max(new BigDecimal("0.01")), 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));

        detection.setPerformanceDegradation(degradation);
        detection.setIsOverfitted(degradation.compareTo(new BigDecimal("30")) > 0);

        BigDecimal returnDiff = train.getTotalReturn().subtract(test.getTotalReturn()).abs();
        BigDecimal stabilityScore = new BigDecimal("100").subtract(returnDiff.min(new BigDecimal("100")));
        detection.setStabilityScore(stabilityScore);

        if (detection.getIsOverfitted()) {
            detection.setWarning("警告：测试期表现显著低于训练期，可能存在过拟合风险");
        } else if (stabilityScore.compareTo(new BigDecimal("70")) < 0) {
            detection.setWarning("提示：策略稳定性较低，建议谨慎使用");
        } else {
            detection.setWarning("策略表现稳定，可以考虑实盘应用");
        }

        return detection;
    }

    private BigDecimal getCurrentPrice(String stockCode, LocalDate date) {
        // 先尝试从数据库获取
        for (int i = 0; i < 10; i++) {
            List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
                stockCode, date.minusDays(i), date.minusDays(i)
            );
            if (!history.isEmpty()) return history.get(0).getClosePrice();
        }

        return BigDecimal.ONE;
    }

    private BigDecimal calculateMaxDrawdown(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO;

        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = values.get(0);

        for (BigDecimal value : values) {
            if (value.compareTo(peak) > 0) peak = value;
            BigDecimal drawdown = peak.subtract(value).divide(peak, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            if (drawdown.compareTo(maxDrawdown) > 0) maxDrawdown = drawdown;
        }

        return maxDrawdown;
    }

    private BigDecimal calculateSharpeRatio(List<BigDecimal> values, BigDecimal initialCapital) {
        if (values.size() < 2) return BigDecimal.ZERO;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) {
            returns.add(values.get(i).subtract(values.get(i - 1))
                .divide(values.get(i - 1), 4, RoundingMode.HALF_UP));
        }

        BigDecimal mean = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
            .map(r -> r.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);

        BigDecimal volatility = new BigDecimal(Math.sqrt(variance.doubleValue()));

        if (volatility.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return mean.subtract(new BigDecimal("0.025")).divide(volatility, 2, RoundingMode.HALF_UP);
    }

    private String generateAIAnalysis(BacktestResponseDTO.PeriodResult train, BacktestResponseDTO.PeriodResult test,
                                      BacktestResponseDTO.OverfittingDetection overfitting, BacktestRequestDTO request) {
        StringBuilder analysis = new StringBuilder();
        analysis.append("<p><strong>策略整体表现：</strong></p>");

        if (train.getTotalReturn().compareTo(BigDecimal.ZERO) > 0 && test.getTotalReturn().compareTo(BigDecimal.ZERO) > 0) {
            analysis.append("<p>✅ 训练期和测试期均实现正收益，策略表现良好。</p>");
        } else {
            analysis.append("<p>⚠️ 策略在部分时期出现亏损，需要优化。</p>");
        }

        analysis.append("<p><strong>风险收益比分析：</strong></p>");
        if (train.getSharpeRatio().compareTo(new BigDecimal("1")) > 0) {
            analysis.append("<p>✅ 夏普比率大于1，风险收益比合理。</p>");
        } else {
            analysis.append("<p>⚠️ 夏普比率偏低，风险相对收益较高。</p>");
        }

        analysis.append("<p><strong>过拟合风险：</strong></p>");
        if (overfitting.getIsOverfitted()) {
            analysis.append("<p>❌ 检测到过拟合风险，测试期表现显著低于训练期。</p>");
        } else {
            analysis.append("<p>✅ 未检测到明显过拟合，策略稳定性良好。</p>");
        }

        analysis.append("<p><strong>实盘建议：</strong></p>");
        if (!overfitting.getIsOverfitted() && test.getTotalReturn().compareTo(BigDecimal.ZERO) > 0) {
            analysis.append("<p>✅ 策略表现稳定，可以考虑小仓位实盘测试。</p>");
        } else {
            analysis.append("<p>⚠️ 建议进一步优化策略参数后再考虑实盘应用。</p>");
        }

        return analysis.toString();
    }

    private static class Position {
        int shares;
        BigDecimal buyPrice;

        Position(int shares, BigDecimal buyPrice) {
            this.shares = shares;
            this.buyPrice = buyPrice;
        }
    }
}

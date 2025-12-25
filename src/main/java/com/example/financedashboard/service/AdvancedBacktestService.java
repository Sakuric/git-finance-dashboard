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
        long daysNeeded = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 30;
        for (StructuredAdvice advice : advices) {
            log.info("同步股票历史数据: {}, 天数: {}", advice.getStockCode(), daysNeeded);
            eastMoneyHistoryService.syncStockHistory(advice.getStockCode(), (int) daysNeeded);
        }

        long totalDays = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate());
        long trainDays = (long) (totalDays * request.getTrainRatio().doubleValue());
        LocalDate splitDate = request.getStartDate().plusDays(trainDays);

        List<BacktestResponseDTO.TradeDetail> allTrades = new ArrayList<>();

        BacktestResponseDTO.PeriodResult trainResult = runPeriodBacktest(
            advices, request.getStartDate(), splitDate, request.getInitialCapital(), "训练期", allTrades, request
        );

        BacktestResponseDTO.PeriodResult testResult = runPeriodBacktest(
            advices, splitDate.plusDays(1), request.getEndDate(), request.getInitialCapital(), "测试期", allTrades, request
        );

        BacktestResponseDTO.OverfittingDetection overfitting = detectOverfitting(trainResult, testResult);

        // 生成完整的收益曲线（训练期+测试期）
        List<BacktestResponseDTO.EquityPoint> equityCurve = new ArrayList<>();
        equityCurve.addAll(trainResult.getEquityCurve());
        equityCurve.addAll(testResult.getEquityCurve());

        // 生成基准曲线（买入并持有）
        List<BacktestResponseDTO.EquityPoint> benchmarkCurve = generateBenchmarkCurve(
            advices, request.getStartDate(), request.getEndDate(), request.getInitialCapital()
        );

        BacktestResponseDTO response = new BacktestResponseDTO();
        response.setTrainPeriod(trainResult);
        response.setTestPeriod(testResult);
        response.setOverfitting(overfitting);
        response.setEquityCurve(equityCurve);
        response.setBenchmarkCurve(benchmarkCurve);
        response.setTrades(allTrades);

        // AI分析
        String aiAnalysis = generateAIAnalysis(trainResult, testResult, overfitting, request);
        response.setAiAnalysis(aiAnalysis);

        return response;
    }

    private BacktestResponseDTO.PeriodResult runPeriodBacktest(
        List<StructuredAdvice> advices, LocalDate startDate, LocalDate endDate,
        BigDecimal initialCapital, String periodName, List<BacktestResponseDTO.TradeDetail> trades,
        BacktestRequestDTO request
    ) {
        log.info("开始{}回测: {} 到 {}, 初始资金: {}", periodName, startDate, endDate, initialCapital);

        BigDecimal cash = initialCapital;
        Map<String, Position> positions = new HashMap<>();
        Set<String> initialBuyDone = new HashSet<>();
        List<BigDecimal> dailyValues = new ArrayList<>();
        int winCount = 0, lossCount = 0;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean hasValidData = false;
            for (StructuredAdvice advice : advices) {
                List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
                    advice.getStockCode(), date, date
                );
                if (!history.isEmpty()) {
                    hasValidData = true;
                    break;
                }
            }
            if (!hasValidData) continue;

            for (StructuredAdvice advice : advices) {
                List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
                    advice.getStockCode(), date, date
                );
                if (history.isEmpty()) continue;

                StockHistory todayData = history.get(0);
                BigDecimal currentPrice = todayData.getClosePrice();

                if (positions.containsKey(advice.getStockCode())) {
                    Position pos = positions.get(advice.getStockCode());
                    BigDecimal changeRate = currentPrice.subtract(pos.buyPrice).divide(pos.buyPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

                    boolean shouldSell = false;
                    String sellReason = "";

                    // 优先使用用户自定义的止盈止损阈值
                    if (changeRate.compareTo(request.getTakeProfitPct()) >= 0) {
                        shouldSell = true;
                        sellReason = "上涨超" + request.getTakeProfitPct() + "%止盈";
                    }
                    else if (changeRate.compareTo(request.getStopLossPct().negate()) <= 0) {
                        shouldSell = true;
                        sellReason = "下跌超" + request.getStopLossPct() + "%止损";
                    }

                    if (shouldSell) {
                        cash = cash.add(currentPrice.multiply(new BigDecimal(pos.shares)));
                        BigDecimal returnRate = changeRate;

                        BacktestResponseDTO.TradeDetail trade = new BacktestResponseDTO.TradeDetail();
                        trade.setDate(date.toString());
                        trade.setStockCode(advice.getStockCode());
                        trade.setStockName(advice.getStockName());
                        trade.setAction("SELL");
                        trade.setPrice(currentPrice);
                        trade.setQuantity(pos.shares);
                        trade.setReturnRate(returnRate);
                        trade.setReason(sellReason);
                        trades.add(trade);

                        if (returnRate.compareTo(BigDecimal.ZERO) > 0) winCount++;
                        else lossCount++;
                        log.info("{}: {} 股票={}, 买入价={}, 卖出价={}, 收益率={}%", sellReason, date, advice.getStockCode(), pos.buyPrice, currentPrice, returnRate);
                        positions.remove(advice.getStockCode());
                    }
                } else if (!initialBuyDone.contains(advice.getStockCode())) {
                    // 只在首次遇到有效数据时买入
                    boolean shouldBuy = "BUY".equals(advice.getSuggestedAction());
                    String buyReason = "初始建仓";

                    if (shouldBuy && cash.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal positionSize = cash.multiply(request.getPositionSizePct().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
                        int shares = positionSize.divide(currentPrice, 0, RoundingMode.DOWN).intValue();

                        if (shares > 0) {
                            cash = cash.subtract(currentPrice.multiply(new BigDecimal(shares)));
                            positions.put(advice.getStockCode(), new Position(shares, currentPrice, advice));
                            initialBuyDone.add(advice.getStockCode());

                            BacktestResponseDTO.TradeDetail trade = new BacktestResponseDTO.TradeDetail();
                            trade.setDate(date.toString());
                            trade.setStockCode(advice.getStockCode());
                            trade.setStockName(advice.getStockName());
                            trade.setAction("BUY");
                            trade.setPrice(currentPrice);
                            trade.setQuantity(shares);
                            trade.setReturnRate(BigDecimal.ZERO);
                            trade.setReason(buyReason);
                            trades.add(trade);

                            log.info("{}: {} 股票={}, 价格={}, 数量={}", buyReason, date, advice.getStockCode(), currentPrice, shares);
                        }
                    }
                }
            }

            BigDecimal totalValue = cash;
            for (Map.Entry<String, Position> entry : positions.entrySet()) {
                List<StockHistory> posHistory = stockHistoryMapper.findByStockCodeAndDateRange(
                    entry.getKey(), date, date
                );
                BigDecimal currentPrice = posHistory.isEmpty() ?
                    getCurrentPrice(entry.getKey(), date) : posHistory.get(0).getClosePrice();
                totalValue = totalValue.add(currentPrice.multiply(new BigDecimal(entry.getValue().shares)));
            }
            dailyValues.add(totalValue);
        }

        for (String stockCode : new HashSet<>(positions.keySet())) {
            Position pos = positions.get(stockCode);
            BigDecimal closePrice = getCurrentPrice(stockCode, endDate);
            cash = cash.add(closePrice.multiply(new BigDecimal(pos.shares)));
            BigDecimal returnRate = closePrice.subtract(pos.buyPrice).divide(pos.buyPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

            BacktestResponseDTO.TradeDetail trade = new BacktestResponseDTO.TradeDetail();
            trade.setDate(endDate.toString());
            trade.setStockCode(stockCode);
            trade.setStockName(stockCode);
            trade.setAction("SELL");
            trade.setPrice(closePrice);
            trade.setQuantity(pos.shares);
            trade.setReturnRate(returnRate);
            trade.setReason("期末平仓");
            trades.add(trade);

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
        if (days > 0) {
            result.setAnnualReturn(result.getTotalReturn().multiply(new BigDecimal("365")).divide(new BigDecimal(days), 2, RoundingMode.HALF_UP));
        } else {
            result.setAnnualReturn(BigDecimal.ZERO);
        }

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

    private List<BacktestResponseDTO.EquityPoint> generateBenchmarkCurve(
        List<StructuredAdvice> advices, LocalDate startDate, LocalDate endDate, BigDecimal initialCapital
    ) {
        List<BacktestResponseDTO.EquityPoint> curve = new ArrayList<>();
        if (advices.isEmpty()) return curve;

        // 过滤出BUY建议的股票
        List<StructuredAdvice> buyAdvices = advices.stream()
            .filter(a -> "BUY".equals(a.getSuggestedAction()))
            .collect(java.util.stream.Collectors.toList());

        if (buyAdvices.isEmpty()) return curve;

        // 找到第一个有效交易日并买入所有股票
        Map<String, Integer> holdings = new HashMap<>();
        BigDecimal remainingCash = initialCapital;
        LocalDate firstTradeDate = null;

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean hasValidData = false;
            for (StructuredAdvice advice : buyAdvices) {
                BigDecimal price = getCurrentPrice(advice.getStockCode(), date);
                if (price.compareTo(BigDecimal.ZERO) > 0) {
                    hasValidData = true;
                    break;
                }
            }

            if (hasValidData) {
                firstTradeDate = date;
                // 平均分配资金买入所有股票
                BigDecimal perStockCapital = initialCapital.divide(new BigDecimal(buyAdvices.size()), 0, RoundingMode.DOWN);

                for (StructuredAdvice advice : buyAdvices) {
                    BigDecimal price = getCurrentPrice(advice.getStockCode(), date);
                    if (price.compareTo(BigDecimal.ZERO) > 0) {
                        int shares = perStockCapital.divide(price, 0, RoundingMode.DOWN).intValue();
                        if (shares > 0) {
                            holdings.put(advice.getStockCode(), shares);
                            remainingCash = remainingCash.subtract(price.multiply(new BigDecimal(shares)));
                        }
                    }
                }
                break;
            }
        }

        if (firstTradeDate == null || holdings.isEmpty()) return curve;

        // 生成每日净值曲线
        for (LocalDate date = firstTradeDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            BigDecimal totalValue = remainingCash;

            for (Map.Entry<String, Integer> entry : holdings.entrySet()) {
                BigDecimal currentPrice = getCurrentPrice(entry.getKey(), date);
                if (currentPrice.compareTo(BigDecimal.ZERO) > 0) {
                    totalValue = totalValue.add(currentPrice.multiply(new BigDecimal(entry.getValue())));
                }
            }

            BacktestResponseDTO.EquityPoint point = new BacktestResponseDTO.EquityPoint();
            point.setDate(date.toString());
            point.setValue(totalValue);
            curve.add(point);
        }

        return curve;
    }

    private BigDecimal getCurrentPrice(String stockCode, LocalDate date) {
        // 向前查找最近的有效价格（最多30天）
        for (int i = 0; i < 30; i++) {
            List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
                stockCode, date.minusDays(i), date.minusDays(i)
            );
            if (!history.isEmpty()) {
                return history.get(0).getClosePrice();
            }
        }
        // 如果30天内都没有数据，返回0表示无效
        return BigDecimal.ZERO;
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

    public String generateDeepAnalysis(BacktestResponseDTO data) {
        StringBuilder analysis = new StringBuilder();

        analysis.append("<h3>📊 总体趋势分析</h3>");
        List<BacktestResponseDTO.EquityPoint> curve = data.getEquityCurve();
        if (curve != null && curve.size() > 1) {
            BigDecimal startValue = curve.get(0).getValue();
            BigDecimal endValue = curve.get(curve.size() - 1).getValue();
            BigDecimal totalChange = endValue.subtract(startValue).divide(startValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

            if (totalChange.compareTo(BigDecimal.ZERO) > 0) {
                analysis.append("<p>✅ 策略整体呈<strong>上升趋势</strong>，累计收益率为 ").append(totalChange.setScale(2, RoundingMode.HALF_UP)).append("%</p>");
            } else {
                analysis.append("<p>⚠️ 策略整体呈<strong>下降趋势</strong>，累计亏损 ").append(totalChange.abs().setScale(2, RoundingMode.HALF_UP)).append("%</p>");
            }
        }

        analysis.append("<h3>⚡ 异常波动分析</h3>");
        List<String> anomalies = detectAnomalies(curve);
        if (anomalies.isEmpty()) {
            analysis.append("<p>✅ 未检测到显著异常波动，策略运行平稳</p>");
        } else {
            analysis.append("<p>检测到 <strong>").append(anomalies.size()).append(" 个</strong>异常波动点：</p><ul>");
            for (String anomaly : anomalies) {
                analysis.append("<li>").append(anomaly);

                // 为每个异常点添加可能的原因分析
                if (anomaly.contains("下跌")) {
                    analysis.append(" - <span style='color:#F85149'>可能触发止损机制或市场大幅调整</span>");
                } else if (anomaly.contains("上涨")) {
                    analysis.append(" - <span style='color:#39D353'>可能触发止盈机制或股票强势反弹</span>");
                }
                analysis.append("</li>");
            }
            analysis.append("</ul>");
        }

        analysis.append("<h3>💡 原因分析</h3>");
        if (data.getTrades() != null && !data.getTrades().isEmpty()) {
            int winCount = (int) data.getTrades().stream().filter(t -> t.getReturnRate() != null && t.getReturnRate().compareTo(BigDecimal.ZERO) > 0).count();
            int lossCount = (int) data.getTrades().stream().filter(t -> t.getReturnRate() != null && t.getReturnRate().compareTo(BigDecimal.ZERO) < 0).count();

            analysis.append("<p>交易统计：共 ").append(data.getTrades().size()).append(" 笔交易，其中盈利 ").append(winCount).append(" 笔，亏损 ").append(lossCount).append(" 笔</p>");

            if (winCount > lossCount) {
                analysis.append("<p>✅ 策略胜率较高，说明选股逻辑较为合理</p>");
            } else {
                analysis.append("<p>⚠️ 策略胜率偏低，建议优化选股条件或止损策略</p>");
            }
        }

        return analysis.toString();
    }

    private List<String> detectAnomalies(List<BacktestResponseDTO.EquityPoint> curve) {
        List<String> anomalies = new ArrayList<>();
        if (curve == null || curve.size() < 3) return anomalies;

        for (int i = 1; i < curve.size(); i++) {
            BigDecimal prevValue = curve.get(i - 1).getValue();
            BigDecimal currValue = curve.get(i).getValue();
            BigDecimal change = currValue.subtract(prevValue).divide(prevValue, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));

            if (change.abs().compareTo(new BigDecimal("5")) > 0) {
                String direction = change.compareTo(BigDecimal.ZERO) > 0 ? "上涨" : "下跌";
                anomalies.add(curve.get(i).getDate() + " 单日" + direction + " " + change.abs().setScale(2, RoundingMode.HALF_UP) + "%");
            }
        }

        return anomalies;
    }

    private static class Position {
        int shares;
        BigDecimal buyPrice;
        StructuredAdvice advice;

        Position(int shares, BigDecimal buyPrice, StructuredAdvice advice) {
            this.shares = shares;
            this.buyPrice = buyPrice;
            this.advice = advice;
        }
    }
}

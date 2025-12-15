package com.example.financedashboard.service;

import com.example.financedashboard.entity.*;
import com.example.financedashboard.mapper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class BacktestService {

    private final BacktestResultMapper backtestMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final StructuredAdviceMapper structuredAdviceMapper;
    private final StockBacktestDetailMapper detailMapper;

    private static final BigDecimal INITIAL_CASH = new BigDecimal("1000000");
    private static final BigDecimal POSITION_SIZE_RATIO = new BigDecimal("0.10");

    @Transactional
    public BacktestResult performBacktest(InvestmentAdvice advice) {
        List<StructuredAdvice> advices = structuredAdviceMapper.findByAdviceId(advice.getId());
        if (advices.isEmpty()) {
            BacktestResult result = new BacktestResult();
            result.setAdviceId(advice.getId());
            setDefaultValues(result);
            backtestMapper.insert(result);
            return result;
        }

        LocalDate endDate = LocalDate.now();
        String[] periods = {"1M", "3M", "6M", "1Y"};
        Map<String, Map<String, StockDetail>> allPeriodResults = new HashMap<>();

        for (String period : periods) {
            LocalDate startDate = calculateStartDate(endDate, period);
            SimulationResult simulation = runSimulation(advices, startDate, endDate);
            allPeriodResults.put(period, simulation.stockDetails);
        }

        BacktestResult result = new BacktestResult();
        result.setAdviceId(advice.getId());
        result.setBacktestStartDate(calculateStartDate(endDate, "6M"));
        result.setBacktestEndDate(endDate);
        result.setBacktestDuration(180);

        SimulationResult mainSimulation = runSimulation(advices, calculateStartDate(endDate, "6M"), endDate);
        result.setTotalReturn(mainSimulation.totalReturn);
        result.setAnnualizedReturn(mainSimulation.totalReturn.multiply(new BigDecimal("2")));
        result.setMaxDrawdown(mainSimulation.maxDrawdown);
        result.setSharpeRatio(mainSimulation.sharpeRatio);
        result.setWinRate(mainSimulation.winRate);
        result.setVolatility(mainSimulation.volatility);
        result.setIsSuccess(mainSimulation.totalReturn.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
        result.setFailureReason("多周期回测结果，仅供参考");

        backtestMapper.insert(result);
        saveMultiPeriodDetails(result.getId(), advice.getId(), advices, allPeriodResults);

        return result;
    }

    private LocalDate calculateStartDate(LocalDate endDate, String period) {
        switch (period) {
            case "1M": return endDate.minusMonths(1);
            case "3M": return endDate.minusMonths(3);
            case "6M": return endDate.minusMonths(6);
            case "1Y": return endDate.minusYears(1);
            default: return endDate.minusMonths(6);
        }
    }

    private SimulationResult runSimulation(List<StructuredAdvice> advices, LocalDate startDate, LocalDate endDate) {
        Map<String, StockDetail> stockDetails = new HashMap<>();
        BigDecimal cash = INITIAL_CASH;
        Map<String, Position> positions = new HashMap<>();
        List<BigDecimal> dailyValues = new ArrayList<>();
        Map<String, Integer> tradeResults = new HashMap<>();

        for (StructuredAdvice advice : advices) {
            stockDetails.put(advice.getStockCode(), new StockDetail());
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            for (StructuredAdvice advice : advices) {
                if (!"BUY".equals(advice.getSuggestedAction())) continue;

                StockDetail detail = stockDetails.get(advice.getStockCode());
                List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(advice.getStockCode(), date, date);
                if (history.isEmpty()) continue;

                StockHistory todayData = history.get(0);

                if (positions.containsKey(advice.getStockCode())) {
                    Position pos = positions.get(advice.getStockCode());
                    boolean shouldSell = false;
                    BigDecimal sellPrice = todayData.getClosePrice();
                    String exitReason = null;

                    if (advice.getStopLossPrice() != null && todayData.getLowPrice().compareTo(advice.getStopLossPrice()) <= 0) {
                        sellPrice = advice.getStopLossPrice();
                        shouldSell = true;
                        exitReason = "STOP_LOSS";
                    } else if (advice.getTakeProfitPrice() != null && todayData.getHighPrice().compareTo(advice.getTakeProfitPrice()) >= 0) {
                        sellPrice = advice.getTakeProfitPrice();
                        shouldSell = true;
                        exitReason = "TAKE_PROFIT";
                    }

                    if (shouldSell) {
                        cash = cash.add(sellPrice.multiply(new BigDecimal(pos.shares)));
                        BigDecimal profit = sellPrice.subtract(pos.buyPrice).multiply(new BigDecimal(pos.shares));

                        detail.exitPrice = sellPrice;
                        detail.exitReason = exitReason;
                        detail.totalReturn = sellPrice.subtract(pos.buyPrice).divide(pos.buyPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                        detail.tradeCount++;
                        if (profit.compareTo(BigDecimal.ZERO) > 0) {
                            detail.winCount++;
                            tradeResults.put("win", tradeResults.getOrDefault("win", 0) + 1);
                        } else {
                            tradeResults.put("loss", tradeResults.getOrDefault("loss", 0) + 1);
                        }

                        positions.remove(advice.getStockCode());
                    }
                } else {
                    if (advice.getEntryPriceStart() == null || advice.getEntryPriceEnd() == null) continue;
                    if (date.isAfter(startDate.plusDays(advice.getAdviceEffectiveDays() != null ? advice.getAdviceEffectiveDays() : 180))) continue;

                    if (todayData.getLowPrice().compareTo(advice.getEntryPriceEnd()) <= 0 &&
                        todayData.getHighPrice().compareTo(advice.getEntryPriceStart()) >= 0) {

                        BigDecimal buyPrice = advice.getEntryPriceEnd();
                        BigDecimal positionSize = cash.multiply(POSITION_SIZE_RATIO);
                        int shares = positionSize.divide(buyPrice, 0, RoundingMode.DOWN).intValue();

                        if (shares > 0) {
                            cash = cash.subtract(buyPrice.multiply(new BigDecimal(shares)));
                            positions.put(advice.getStockCode(), new Position(shares, buyPrice, date));
                            detail.entryPrice = buyPrice;
                        }
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

            StockDetail detail = stockDetails.get(stockCode);
            if (detail.entryPrice != null) {
                detail.exitPrice = closePrice;
                detail.exitReason = "END_OF_PERIOD";
                detail.totalReturn = closePrice.subtract(pos.buyPrice).divide(pos.buyPrice, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
                detail.tradeCount++;

                BigDecimal profit = closePrice.subtract(pos.buyPrice).multiply(new BigDecimal(pos.shares));
                if (profit.compareTo(BigDecimal.ZERO) > 0) {
                    detail.winCount++;
                    tradeResults.put("win", tradeResults.getOrDefault("win", 0) + 1);
                } else {
                    tradeResults.put("loss", tradeResults.getOrDefault("loss", 0) + 1);
                }
            }
        }

        SimulationResult result = calculateMetrics(dailyValues, tradeResults, INITIAL_CASH);
        result.stockDetails = stockDetails;
        return result;
    }

    private BigDecimal getCurrentPrice(String stockCode, LocalDate date) {
        for (int i = 0; i < 10; i++) {
            List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(stockCode, date.minusDays(i), date.minusDays(i));
            if (!history.isEmpty()) return history.get(0).getClosePrice();
        }
        return BigDecimal.ONE;
    }

    private void saveMultiPeriodDetails(Long backtestId, Long adviceId, List<StructuredAdvice> advices,
                                        Map<String, Map<String, StockDetail>> allPeriodResults) {
        List<StockBacktestDetail> details = new ArrayList<>();

        for (StructuredAdvice advice : advices) {
            for (String period : new String[]{"1M", "3M", "6M", "1Y"}) {
                StockBacktestDetail detail = new StockBacktestDetail();
                detail.setBacktestId(backtestId);
                detail.setAdviceId(adviceId);
                detail.setBacktestPeriod(period);
                detail.setStockCode(advice.getStockCode());
                detail.setStockName(advice.getStockName());
                detail.setSuggestedAction(advice.getSuggestedAction());

                Map<String, StockDetail> periodDetails = allPeriodResults.get(period);
                StockDetail sd = periodDetails != null ? periodDetails.get(advice.getStockCode()) : null;

                if (sd != null && sd.entryPrice != null) {
                    detail.setTotalReturn(sd.totalReturn);
                    detail.setTradeCount(sd.tradeCount);
                    detail.setWinCount(sd.winCount);
                    detail.setEntryPrice(sd.entryPrice);
                    detail.setExitPrice(sd.exitPrice);
                    detail.setExitReason(sd.exitReason);
                } else {
                    if ("NOT_RECOMMENDED".equals(advice.getSuggestedAction())) {
                        detail.setNoTradeReason("不建议投资");
                    } else if ("HOLD".equals(advice.getSuggestedAction())) {
                        detail.setNoTradeReason("持有观望，无交易策略");
                    } else if ("BUY".equals(advice.getSuggestedAction())) {
                        detail.setNoTradeReason("价格未触及买入区间");
                    }
                }

                details.add(detail);
            }
        }

        calculateRiskLevels(details);

        if (!details.isEmpty()) {
            detailMapper.batchInsert(details);
            log.info("保存了{}只股票的多周期回测详情", details.size());
        }
    }

    private void calculateRiskLevels(List<StockBacktestDetail> details) {
        Map<String, List<StockBacktestDetail>> stockGroups = new HashMap<>();
        for (StockBacktestDetail detail : details) {
            stockGroups.computeIfAbsent(detail.getStockCode(), k -> new ArrayList<>()).add(detail);
        }

        for (List<StockBacktestDetail> group : stockGroups.values()) {
            int negativeCount = 0;
            BigDecimal totalVolatility = BigDecimal.ZERO;
            int validCount = 0;

            for (StockBacktestDetail detail : group) {
                if (detail.getTotalReturn() != null) {
                    if (detail.getTotalReturn().compareTo(BigDecimal.ZERO) < 0) {
                        negativeCount++;
                    }
                    totalVolatility = totalVolatility.add(detail.getTotalReturn().abs());
                    validCount++;
                }
            }

            String riskLevel;
            if (validCount == 0) {
                riskLevel = "UNKNOWN";
            } else if (negativeCount == validCount) {
                riskLevel = "EXTREME";
            } else if (negativeCount >= 3) {
                riskLevel = "HIGH";
            } else if (negativeCount >= 2 || (validCount > 0 && totalVolatility.divide(new BigDecimal(validCount), 2, RoundingMode.HALF_UP).compareTo(new BigDecimal("30")) > 0)) {
                riskLevel = "MEDIUM";
            } else {
                riskLevel = "LOW";
            }

            for (StockBacktestDetail detail : group) {
                detail.setRiskLevel(riskLevel);
            }
        }
    }

    private SimulationResult calculateMetrics(List<BigDecimal> dailyValues, Map<String, Integer> tradeResults, BigDecimal initialCash) {
        SimulationResult result = new SimulationResult();

        BigDecimal finalValue = dailyValues.get(dailyValues.size() - 1);
        result.totalReturn = finalValue.subtract(initialCash).divide(initialCash, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
        result.maxDrawdown = calculateMaxDrawdown(dailyValues);
        result.volatility = calculateVolatility(dailyValues);
        result.sharpeRatio = calculateSharpeRatio(result.totalReturn, result.volatility);

        int totalTrades = tradeResults.getOrDefault("win", 0) + tradeResults.getOrDefault("loss", 0);
        result.winRate = totalTrades > 0
            ? new BigDecimal(tradeResults.getOrDefault("win", 0)).divide(new BigDecimal(totalTrades), 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return result;
    }

    private BigDecimal calculateMaxDrawdown(List<BigDecimal> values) {
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = values.get(0);

        for (BigDecimal value : values) {
            if (value.compareTo(peak) > 0) peak = value;
            BigDecimal drawdown = peak.subtract(value).divide(peak, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            if (drawdown.compareTo(maxDrawdown) > 0) maxDrawdown = drawdown;
        }

        return maxDrawdown;
    }

    private BigDecimal calculateVolatility(List<BigDecimal> values) {
        if (values.size() < 2) return BigDecimal.ZERO;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) {
            returns.add(values.get(i).subtract(values.get(i - 1)).divide(values.get(i - 1), 4, RoundingMode.HALF_UP));
        }

        BigDecimal mean = returns.stream().reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);
        BigDecimal variance = returns.stream().map(r -> r.subtract(mean).pow(2)).reduce(BigDecimal.ZERO, BigDecimal::add).divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);

        return new BigDecimal(Math.sqrt(variance.doubleValue())).multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateSharpeRatio(BigDecimal returns, BigDecimal volatility) {
        if (volatility.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return returns.subtract(new BigDecimal("2.5")).divide(volatility, 2, RoundingMode.HALF_UP);
    }

    private void setDefaultValues(BacktestResult result) {
        result.setTotalReturn(BigDecimal.ZERO);
        result.setAnnualizedReturn(BigDecimal.ZERO);
        result.setMaxDrawdown(BigDecimal.ZERO);
        result.setSharpeRatio(BigDecimal.ZERO);
        result.setWinRate(BigDecimal.ZERO);
        result.setVolatility(BigDecimal.ZERO);
    }

    public BacktestResult getBacktestByAdviceId(Long adviceId) {
        return backtestMapper.findByAdviceId(adviceId);
    }

    private static class Position {
        int shares;
        BigDecimal buyPrice;
        LocalDate buyDate;

        Position(int shares, BigDecimal buyPrice, LocalDate buyDate) {
            this.shares = shares;
            this.buyPrice = buyPrice;
            this.buyDate = buyDate;
        }
    }

    private static class SimulationResult {
        BigDecimal totalReturn;
        BigDecimal maxDrawdown;
        BigDecimal volatility;
        BigDecimal sharpeRatio;
        BigDecimal winRate;
        Map<String, StockDetail> stockDetails = new HashMap<>();
    }

    private static class StockDetail {
        BigDecimal totalReturn;
        int tradeCount;
        int winCount;
        BigDecimal entryPrice;
        BigDecimal exitPrice;
        String exitReason;
    }
}

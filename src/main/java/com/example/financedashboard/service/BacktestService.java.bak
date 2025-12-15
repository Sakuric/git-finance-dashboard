package com.example.financedashboard.service;

import com.example.financedashboard.entity.BacktestResult;
import com.example.financedashboard.entity.InvestmentAdvice;
import com.example.financedashboard.entity.StockHistory;
import com.example.financedashboard.entity.StructuredAdvice;
import com.example.financedashboard.mapper.BacktestResultMapper;
import com.example.financedashboard.mapper.StockHistoryMapper;
import com.example.financedashboard.mapper.StructuredAdviceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

    private static final BigDecimal INITIAL_CASH = new BigDecimal("1000000");
    private static final BigDecimal POSITION_SIZE_RATIO = new BigDecimal("0.10");

    public BacktestResult performBacktest(InvestmentAdvice advice) {
        BacktestResult result = new BacktestResult();
        result.setAdviceId(advice.getId());

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusMonths(6);
        result.setBacktestStartDate(startDate);
        result.setBacktestEndDate(endDate);
        result.setBacktestDuration(180);

        try {
            List<StructuredAdvice> advices = structuredAdviceMapper.findByAdviceId(advice.getId());
            if (advices.isEmpty()) {
                log.warn("没有找到结构化建议，使用默认值");
                setDefaultValues(result);
                backtestMapper.insert(result);
                return result;
            }

            SimulationResult simulation = runSimulation(advices, startDate, endDate);

            result.setTotalReturn(simulation.totalReturn);
            result.setAnnualizedReturn(simulation.totalReturn.multiply(new BigDecimal("2")));
            result.setMaxDrawdown(simulation.maxDrawdown);
            result.setSharpeRatio(simulation.sharpeRatio);
            result.setWinRate(simulation.winRate);
            result.setVolatility(simulation.volatility);
            result.setIsSuccess(simulation.totalReturn.compareTo(BigDecimal.ZERO) > 0 ? 1 : 0);
            result.setFailureReason("基于具体买卖价位的回测结果，仅供参考");

            log.info("回测完成，建议ID: {}, 收益率: {}%", advice.getId(), simulation.totalReturn);
        } catch (Exception e) {
            log.error("回测失败", e);
            setDefaultValues(result);
        }

        backtestMapper.insert(result);
        return result;
    }

    private SimulationResult runSimulation(List<StructuredAdvice> advices, LocalDate startDate, LocalDate endDate) {
        Portfolio portfolio = new Portfolio(INITIAL_CASH, stockHistoryMapper);
        List<BigDecimal> dailyValues = new ArrayList<>();
        Map<String, Integer> tradeResults = new HashMap<>();

        List<StructuredAdvice> buyAdvices = new ArrayList<>();
        for (StructuredAdvice advice : advices) {
            if ("BUY".equals(advice.getSuggestedAction())) {
                buyAdvices.add(advice);
            }
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDate currentDate = date;

            for (StructuredAdvice advice : buyAdvices) {
                if (portfolio.hasPosition(advice.getStockCode())) {
                    checkSellSignals(portfolio, advice, currentDate, tradeResults);
                } else {
                    checkBuySignals(portfolio, advice, currentDate, startDate);
                }
            }

            BigDecimal dailyValue = portfolio.calculateTotalValue(currentDate);
            dailyValues.add(dailyValue);
        }

        portfolio.closeAllPositions(endDate, tradeResults);

        return calculateMetrics(dailyValues, tradeResults, INITIAL_CASH);
    }

    private void checkBuySignals(Portfolio portfolio, StructuredAdvice advice, LocalDate currentDate, LocalDate adviceDate) {
        if (advice.getEntryPriceStart() == null || advice.getEntryPriceEnd() == null) return;

        Integer effectiveDays = advice.getAdviceEffectiveDays() != null ? advice.getAdviceEffectiveDays() : 180;
        if (currentDate.isAfter(adviceDate.plusDays(effectiveDays))) return;

        List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
            advice.getStockCode(), currentDate, currentDate);

        if (history.isEmpty()) return;

        StockHistory todayData = history.get(0);
        if (todayData.getLowPrice().compareTo(advice.getEntryPriceEnd()) <= 0 &&
            todayData.getHighPrice().compareTo(advice.getEntryPriceStart()) >= 0) {

            BigDecimal buyPrice = advice.getEntryPriceEnd();
            BigDecimal positionSize = portfolio.cash.multiply(POSITION_SIZE_RATIO);
            int shares = positionSize.divide(buyPrice, 0, RoundingMode.DOWN).intValue();

            if (shares > 0) {
                BigDecimal cost = buyPrice.multiply(new BigDecimal(shares));
                portfolio.buy(advice.getStockCode(), shares, buyPrice, currentDate);
                log.debug("买入: {} 股票代码={}, 价格={}, 股数={}", currentDate, advice.getStockCode(), buyPrice, shares);
            }
        }
    }

    private void checkSellSignals(Portfolio portfolio, StructuredAdvice advice, LocalDate currentDate, Map<String, Integer> tradeResults) {
        Position position = portfolio.getPosition(advice.getStockCode());
        if (position == null) return;

        List<StockHistory> history = stockHistoryMapper.findByStockCodeAndDateRange(
            advice.getStockCode(), currentDate, currentDate);

        if (history.isEmpty()) return;

        StockHistory todayData = history.get(0);
        boolean shouldSell = false;
        BigDecimal sellPrice = todayData.getClosePrice();

        if (advice.getStopLossPrice() != null && todayData.getLowPrice().compareTo(advice.getStopLossPrice()) <= 0) {
            sellPrice = advice.getStopLossPrice();
            shouldSell = true;
            log.debug("触发止损: {} 股票代码={}, 价格={}", currentDate, advice.getStockCode(), sellPrice);
        } else if (advice.getTakeProfitPrice() != null && todayData.getHighPrice().compareTo(advice.getTakeProfitPrice()) >= 0) {
            sellPrice = advice.getTakeProfitPrice();
            shouldSell = true;
            log.debug("触发止盈: {} 股票代码={}, 价格={}", currentDate, advice.getStockCode(), sellPrice);
        }

        if (shouldSell) {
            BigDecimal profit = sellPrice.subtract(position.buyPrice).multiply(new BigDecimal(position.shares));
            portfolio.sell(advice.getStockCode(), sellPrice);

            String key = profit.compareTo(BigDecimal.ZERO) > 0 ? "win" : "loss";
            tradeResults.put(key, tradeResults.getOrDefault(key, 0) + 1);
        }
    }

    private SimulationResult calculateMetrics(List<BigDecimal> dailyValues, Map<String, Integer> tradeResults, BigDecimal initialCash) {
        SimulationResult result = new SimulationResult();

        BigDecimal finalValue = dailyValues.get(dailyValues.size() - 1);
        result.totalReturn = finalValue.subtract(initialCash)
            .divide(initialCash, 4, RoundingMode.HALF_UP)
            .multiply(new BigDecimal("100"));

        result.maxDrawdown = calculateMaxDrawdown(dailyValues);
        result.volatility = calculateVolatility(dailyValues);
        result.sharpeRatio = calculateSharpeRatio(result.totalReturn, result.volatility);

        int totalTrades = tradeResults.getOrDefault("win", 0) + tradeResults.getOrDefault("loss", 0);
        result.winRate = totalTrades > 0
            ? new BigDecimal(tradeResults.getOrDefault("win", 0))
                .divide(new BigDecimal(totalTrades), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
            : BigDecimal.ZERO;

        return result;
    }

    private BigDecimal calculateMaxDrawdown(List<BigDecimal> values) {
        BigDecimal maxDrawdown = BigDecimal.ZERO;
        BigDecimal peak = values.get(0);

        for (BigDecimal value : values) {
            if (value.compareTo(peak) > 0) {
                peak = value;
            }
            BigDecimal drawdown = peak.subtract(value).divide(peak, 4, RoundingMode.HALF_UP).multiply(new BigDecimal("100"));
            if (drawdown.compareTo(maxDrawdown) > 0) {
                maxDrawdown = drawdown;
            }
        }

        return maxDrawdown;
    }

    private BigDecimal calculateVolatility(List<BigDecimal> values) {
        if (values.size() < 2) return BigDecimal.ZERO;

        List<BigDecimal> returns = new ArrayList<>();
        for (int i = 1; i < values.size(); i++) {
            BigDecimal ret = values.get(i).subtract(values.get(i - 1))
                .divide(values.get(i - 1), 4, RoundingMode.HALF_UP);
            returns.add(ret);
        }

        BigDecimal mean = returns.stream()
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);

        BigDecimal variance = returns.stream()
            .map(r -> r.subtract(mean).pow(2))
            .reduce(BigDecimal.ZERO, BigDecimal::add)
            .divide(new BigDecimal(returns.size()), 4, RoundingMode.HALF_UP);

        return new BigDecimal(Math.sqrt(variance.doubleValue())).multiply(new BigDecimal("100"));
    }

    private BigDecimal calculateSharpeRatio(BigDecimal returns, BigDecimal volatility) {
        if (volatility.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        BigDecimal riskFreeRate = new BigDecimal("2.5");
        return returns.subtract(riskFreeRate).divide(volatility, 2, RoundingMode.HALF_UP);
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

    private static class Portfolio {
        BigDecimal cash;
        Map<String, Position> positions = new HashMap<>();
        StockHistoryMapper mapper;

        Portfolio(BigDecimal initialCash, StockHistoryMapper mapper) {
            this.cash = initialCash;
            this.mapper = mapper;
        }

        void buy(String stockCode, int shares, BigDecimal price, LocalDate date) {
            BigDecimal cost = price.multiply(new BigDecimal(shares));
            cash = cash.subtract(cost);
            positions.put(stockCode, new Position(shares, price, date));
        }

        void sell(String stockCode, BigDecimal price) {
            Position pos = positions.remove(stockCode);
            if (pos != null) {
                cash = cash.add(price.multiply(new BigDecimal(pos.shares)));
            }
        }

        boolean hasPosition(String stockCode) {
            return positions.containsKey(stockCode);
        }

        Position getPosition(String stockCode) {
            return positions.get(stockCode);
        }

        BigDecimal calculateTotalValue(LocalDate date) {
            BigDecimal total = cash;
            for (Map.Entry<String, Position> entry : positions.entrySet()) {
                BigDecimal currentPrice = getCurrentPrice(entry.getKey(), date);
                total = total.add(currentPrice.multiply(new BigDecimal(entry.getValue().shares)));
            }
            return total;
        }

        void closeAllPositions(LocalDate date, Map<String, Integer> tradeResults) {
            for (String stockCode : new HashSet<>(positions.keySet())) {
                BigDecimal closePrice = getCurrentPrice(stockCode, date);
                Position pos = positions.get(stockCode);
                BigDecimal profit = closePrice.subtract(pos.buyPrice).multiply(new BigDecimal(pos.shares));
                sell(stockCode, closePrice);

                String key = profit.compareTo(BigDecimal.ZERO) > 0 ? "win" : "loss";
                tradeResults.put(key, tradeResults.getOrDefault(key, 0) + 1);
            }
        }

        private BigDecimal getCurrentPrice(String stockCode, LocalDate date) {
            for (int i = 0; i < 10; i++) {
                LocalDate queryDate = date.minusDays(i);
                List<StockHistory> history = mapper.findByStockCodeAndDateRange(stockCode, queryDate, queryDate);
                if (!history.isEmpty()) {
                    return history.get(0).getClosePrice();
                }
            }
            return BigDecimal.ONE;
        }
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
    }
}

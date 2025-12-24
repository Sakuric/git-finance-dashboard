package com.example.financedashboard.service;

import com.example.financedashboard.entity.InvestmentAdvice;
import com.example.financedashboard.entity.InvestmentPreference;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.entity.UserFavorite;
import com.example.financedashboard.mapper.InvestmentAdviceMapper;
import com.example.financedashboard.mapper.InvestmentPreferenceMapper;
import com.example.financedashboard.mapper.StockInfoMapper;
import com.example.financedashboard.mapper.UserFavoriteMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PortfolioAnalysisService {

    private final UserFavoriteMapper favoriteMapper;
    private final StockInfoMapper stockInfoMapper;
    private final InvestmentPreferenceMapper preferenceMapper;
    private final InvestmentAdviceMapper adviceMapper;

    public Map<String, Object> calculatePortfolioBalance(Long userId) {
        List<UserFavorite> favorites = favoriteMapper.findByUserId(userId);
        InvestmentPreference preference = preferenceMapper.findByUserId(userId);
        InvestmentAdvice latestAdvice = adviceMapper.findLatestByUserId(userId);

        Map<String, Object> result = new HashMap<>();
        result.put("current", calculateCurrentPortfolio(favorites));
        result.put("suggested", calculateSuggestedPortfolio(favorites, preference, latestAdvice));

        return result;
    }

    private Map<String, Double> calculateCurrentPortfolio(List<UserFavorite> favorites) {
        if (favorites.isEmpty()) {
            return getDefaultPortfolio();
        }

        List<StockInfo> stocks = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            StockInfo stock = stockInfoMapper.findById(favorite.getStockId());
            if (stock != null) {
                stocks.add(stock);
            }
        }

        return analyzeStockCharacteristics(stocks);
    }

    private Map<String, Double> calculateSuggestedPortfolio(List<UserFavorite> favorites,
                                                             InvestmentPreference preference,
                                                             InvestmentAdvice latestAdvice) {
        if (preference == null) {
            return getDefaultPortfolio();
        }

        Map<String, Double> suggested = new HashMap<>();
        Integer riskLevel = preference.getRiskToleranceLevel();

        // 根据风险等级调整建议配置
        switch (riskLevel != null ? riskLevel : 3) {
            case 1: // 保守型
                suggested.put("growth", 60.0);
                suggested.put("value", 85.0);
                suggested.put("stability", 95.0);
                suggested.put("profitability", 70.0);
                suggested.put("liquidity", 90.0);
                break;
            case 2: // 稳健型
                suggested.put("growth", 75.0);
                suggested.put("value", 80.0);
                suggested.put("stability", 90.0);
                suggested.put("profitability", 80.0);
                suggested.put("liquidity", 85.0);
                break;
            case 3: // 平衡型
                suggested.put("growth", 85.0);
                suggested.put("value", 75.0);
                suggested.put("stability", 80.0);
                suggested.put("profitability", 85.0);
                suggested.put("liquidity", 85.0);
                break;
            case 4: // 积极型
                suggested.put("growth", 90.0);
                suggested.put("value", 70.0);
                suggested.put("stability", 75.0);
                suggested.put("profitability", 90.0);
                suggested.put("liquidity", 80.0);
                break;
            case 5: // 激进型
                suggested.put("growth", 95.0);
                suggested.put("value", 65.0);
                suggested.put("stability", 70.0);
                suggested.put("profitability", 95.0);
                suggested.put("liquidity", 75.0);
                break;
            default:
                return getDefaultPortfolio();
        }

        return suggested;
    }

    private Map<String, Double> analyzeStockCharacteristics(List<StockInfo> stocks) {
        if (stocks.isEmpty()) {
            return getDefaultPortfolio();
        }

        double totalGrowth = 0, totalValue = 0, totalStability = 0, totalProfitability = 0, totalLiquidity = 0;
        int count = stocks.size();

        for (StockInfo stock : stocks) {
            totalGrowth += calculateGrowthScore(stock);
            totalValue += calculateValueScore(stock);
            totalStability += calculateStabilityScore(stock);
            totalProfitability += calculateProfitabilityScore(stock);
            totalLiquidity += calculateLiquidityScore(stock);
        }

        Map<String, Double> result = new HashMap<>();
        result.put("growth", Math.min(100, totalGrowth / count));
        result.put("value", Math.min(100, totalValue / count));
        result.put("stability", Math.min(100, totalStability / count));
        result.put("profitability", Math.min(100, totalProfitability / count));
        result.put("liquidity", Math.min(100, totalLiquidity / count));

        return result;
    }

    private double calculateGrowthScore(StockInfo stock) {
        double score = 50.0;
        if (stock.getChangePercent() != null) {
            score += Math.min(30, stock.getChangePercent().doubleValue() * 2.0);
        }
        return Math.max(0, Math.min(100, score));
    }

    private double calculateValueScore(StockInfo stock) {
        double score = 50.0;
        if (stock.getCurrentPrice() != null && stock.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0) {
            score += 20;
        }
        return Math.max(0, Math.min(100, score));
    }

    private double calculateStabilityScore(StockInfo stock) {
        double score = 60.0;
        if (stock.getTotalMarketCap() != null) {
            double marketCap = stock.getTotalMarketCap().doubleValue();
            if (marketCap > 1000) score += 30;
            else if (marketCap > 500) score += 20;
            else if (marketCap > 100) score += 10;
        }
        if (stock.getChangePercent() != null) {
            double volatility = Math.abs(stock.getChangePercent().doubleValue());
            if (volatility < 2) score += 10;
            else if (volatility > 5) score -= 10;
        }
        return Math.max(0, Math.min(100, score));
    }

    private double calculateProfitabilityScore(StockInfo stock) {
        double score = 50.0;
        if (stock.getChangePercent() != null && stock.getChangePercent().compareTo(BigDecimal.ZERO) > 0) {
            score += 30;
        }
        return Math.max(0, Math.min(100, score));
    }

    private double calculateLiquidityScore(StockInfo stock) {
        double score = 70.0;
        if (stock.getStatus() != null && stock.getStatus() == 1) {
            score += 20;
        }
        return Math.max(0, Math.min(100, score));
    }

    private Map<String, Double> getDefaultPortfolio() {
        Map<String, Double> result = new HashMap<>();
        result.put("growth", 70.0);
        result.put("value", 70.0);
        result.put("stability", 70.0);
        result.put("profitability", 70.0);
        result.put("liquidity", 70.0);
        return result;
    }
}

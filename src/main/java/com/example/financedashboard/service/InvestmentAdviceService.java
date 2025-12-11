package com.example.financedashboard.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.example.financedashboard.dto.LLMRequest;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentAdviceService {

    private final InvestmentAdviceMapper adviceMapper;
    private final InvestmentPreferenceMapper preferenceMapper;
    private final UserFavoriteMapper favoriteMapper;
    private final StockInfoMapper stockInfoMapper;
    private final LLMService llmService;
    private final FinancialNewsService newsService;

    public InvestmentAdvice generateAdvice(Long userId) {
        InvestmentPreference preference = preferenceMapper.findByUserId(userId);
        if (preference == null) {
            throw new RuntimeException("用户未设置投资偏好");
        }

        List<UserFavorite> favorites = favoriteMapper.findByUserId(userId);
        if (favorites.isEmpty()) {
            throw new RuntimeException("用户自选股列表为空");
        }

        List<StockInfo> stocks = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            StockInfo stock = getStockById(favorite.getStockId());
            if (stock != null) {
                stocks.add(stock);
            }
        }

        String prompt = buildPrompt(preference, stocks);
        String llmResponse = llmService.chat(userId, prompt, null);

        JSONObject responseJson = JSON.parseObject(llmResponse);

        InvestmentAdvice advice = new InvestmentAdvice();
        advice.setUserId(userId);
        advice.setTitle("AI投资建议");
        advice.setContent(llmResponse);
        advice.setReasoning(extractReasoning(responseJson));
        advice.setRiskAssessment(extractRiskAssessment(responseJson));
        advice.setRecommendedStocks(stocks.stream().map(StockInfo::getStockCode).collect(Collectors.joining(",")));
        advice.setIsValid(1);
        advice.setIsRead(0);

        adviceMapper.insert(advice);
        return advice;
    }

    public InvestmentAdvice getLatestAdvice(Long userId) {
        return adviceMapper.findLatestByUserId(userId);
    }

    private String buildPrompt(InvestmentPreference preference, List<StockInfo> stocks) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("当前时间：2025年12月。请基于2025年最新的市场环境、政策和行业动态生成投资建议。\n\n");

        List<String> stockCodes = stocks.stream().map(StockInfo::getStockCode).collect(Collectors.toList());
        prompt.append(newsService.getLatestNews(stockCodes, preference.getPreferredIndustry())).append("\n\n");

        prompt.append("请根据以上最新资讯和以下用户投资偏好、自选股列表，生成分层级的投资建议。\n\n");

        prompt.append("用户投资偏好：\n");
        prompt.append("- 风险承受能力：").append(getRiskLevelText(preference.getRiskToleranceLevel())).append("\n");
        prompt.append("- 投资期限：").append(preference.getInvestmentHorizonDisplay()).append("\n");
        prompt.append("- 偏好行业：").append(preference.getPreferredIndustry()).append("\n\n");

        prompt.append("自选股列表：\n");
        for (StockInfo stock : stocks) {
            prompt.append("- ").append(stock.getStockName())
                    .append("(").append(stock.getStockCode()).append(")")
                    .append(" - ").append(stock.getIndustry()).append("\n");
        }

        prompt.append("\n请按以下JSON格式返回分层分析：\n");
        prompt.append("{\n");
        prompt.append("  \"worldSituation\": {\"title\": \"世界形势\", \"content\": \"全球经济和地缘政治分析\"},\n");
        prompt.append("  \"nationalPolicy\": {\"title\": \"国家政策\", \"content\": \"国内政策和监管环境分析\"},\n");
        prompt.append("  \"industryTrends\": {\"title\": \"行业趋势\", \"content\": \"相关行业发展趋势分析\"},\n");
        prompt.append("  \"companyOverview\": {\"title\": \"公司概况\", \"content\": \"各公司具体分析\"},\n");
        prompt.append("  \"recommendations\": [{\"code\": \"股票代码\", \"name\": \"股票名称\", \"suggestedAction\": \"买入/持有/卖出\", \"thesis\": \"投资理由\", \"entryPrice\": \"建议价格\"}]\n");
        prompt.append("}\n");

        return prompt.toString();
    }

    private String getRiskLevelText(Integer level) {
        if (level == null) return "未知";
        switch (level) {
            case 1: return "保守型";
            case 2: return "稳健型";
            case 3: return "平衡型";
            case 4: return "成长型";
            case 5: return "激进型";
            default: return "未知";
        }
    }

    private StockInfo getStockById(Long stockId) {
        List<StockInfo> allStocks = stockInfoMapper.findAll();
        return allStocks.stream()
                .filter(s -> s.getId().equals(stockId))
                .findFirst()
                .orElse(null);
    }

    private String extractReasoning(JSONObject response) {
        JSONObject world = response.getJSONObject("worldSituation");
        JSONObject national = response.getJSONObject("nationalPolicy");
        JSONObject industry = response.getJSONObject("industryTrends");

        StringBuilder reasoning = new StringBuilder();
        if (world != null) reasoning.append("世界形势：").append(world.getString("content")).append("\n");
        if (national != null) reasoning.append("国家政策：").append(national.getString("content")).append("\n");
        if (industry != null) reasoning.append("行业趋势：").append(industry.getString("content")).append("\n");

        return reasoning.toString();
    }

    private String extractRiskAssessment(JSONObject response) {
        JSONObject company = response.getJSONObject("companyOverview");
        return company != null ? company.getString("content") : "";
    }
}

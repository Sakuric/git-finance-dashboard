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
        prompt.append("# 投资建议生成任务\n\n");
        prompt.append("**当前时间**: 2025年12月\n");
        prompt.append("**要求**: 请基于以下提供的最新资讯（多源聚合，3小时内更新）生成全面、深入的投资建议分析。\n\n");

        prompt.append("---\n\n");
        prompt.append("## 一、最新市场资讯（四大维度·多源聚合）\n\n");
        prompt.append("*数据来源: 新浪财经 + 东方财富 等多个权威财经平台*\n\n");

        List<String> stockCodes = stocks.stream().map(StockInfo::getStockCode).collect(Collectors.toList());
        String newsContent = newsService.getLatestNews(stockCodes, preference.getPreferredIndustry());
        prompt.append(newsContent).append("\n");

        prompt.append("---\n\n");
        prompt.append("## 二、用户投资画像\n\n");
        prompt.append("| 维度 | 详情 |\n");
        prompt.append("|------|------|\n");
        prompt.append("| 风险承受能力 | ").append(getRiskLevelText(preference.getRiskToleranceLevel())).append(" |\n");
        prompt.append("| 投资期限 | ").append(preference.getInvestmentHorizonDisplay()).append(" |\n");
        prompt.append("| 偏好行业 | ").append(preference.getPreferredIndustry()).append(" |\n\n");

        prompt.append("## 三、自选股池\n\n");
        for (int i = 0; i < stocks.size(); i++) {
            StockInfo stock = stocks.get(i);
            prompt.append(String.format("%d. **%s** (%s) - %s\n",
                i + 1, stock.getStockName(), stock.getStockCode(), stock.getIndustry()));
        }

        prompt.append("\n---\n\n");
        prompt.append("## 四、输出要求\n\n");
        prompt.append("请基于以上**四大维度的最新资讯**（国际形势、国家政策、行业动态、个股新闻），");
        prompt.append("结合用户投资画像和自选股池，生成结构化的投资建议。\n\n");
        prompt.append("**必须严格按照以下JSON格式返回**：\n\n");
        prompt.append("```json\n");
        prompt.append("{\n");
        prompt.append("  \"worldSituation\": {\n");
        prompt.append("    \"title\": \"世界形势分析\",\n");
        prompt.append("    \"content\": \"基于上述国际财经资讯，深入分析全球经济走势、地缘政治风险、主要经济体政策对A股市场的影响\"\n");
        prompt.append("  },\n");
        prompt.append("  \"nationalPolicy\": {\n");
        prompt.append("    \"title\": \"国家政策解读\",\n");
        prompt.append("    \"content\": \"基于上述国家政策资讯，分析最新财政、货币、产业政策对相关行业和个股的影响\"\n");
        prompt.append("  },\n");
        prompt.append("  \"industryTrends\": {\n");
        prompt.append("    \"title\": \"行业趋势研判\",\n");
        prompt.append("    \"content\": \"基于上述行业动态资讯，分析用户偏好行业及自选股所在行业的发展趋势、竞争格局、投资机会\"\n");
        prompt.append("  },\n");
        prompt.append("  \"companyOverview\": {\n");
        prompt.append("    \"title\": \"个股深度分析\",\n");
        prompt.append("    \"content\": \"基于上述个股新闻，逐一分析自选股的最新动态、基本面变化、风险因素\"\n");
        prompt.append("  },\n");
        prompt.append("  \"recommendations\": [\n");
        prompt.append("    {\n");
        prompt.append("      \"code\": \"股票代码\",\n");
        prompt.append("      \"name\": \"股票名称\",\n");
        prompt.append("      \"suggestedAction\": \"买入/持有/卖出\",\n");
        prompt.append("      \"thesis\": \"综合四大维度资讯的投资逻辑和理由\",\n");
        prompt.append("      \"entryPrice\": \"建议操作价格区间\"\n");
        prompt.append("    }\n");
        prompt.append("  ]\n");
        prompt.append("}\n");
        prompt.append("```\n");

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

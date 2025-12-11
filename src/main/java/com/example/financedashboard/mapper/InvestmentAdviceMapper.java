package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.InvestmentAdvice;
import org.apache.ibatis.annotations.*;

@Mapper
public interface InvestmentAdviceMapper {

    @Insert("INSERT INTO investment_advice (user_id, prompt_id, model_config_id, title, content, " +
            "reasoning, risk_assessment, recommended_stocks, target_return_rate, is_valid, is_read) " +
            "VALUES (#{userId}, #{promptId}, #{modelConfigId}, #{title}, #{content}, #{reasoning}, " +
            "#{riskAssessment}, #{recommendedStocks}, #{targetReturnRate}, #{isValid}, #{isRead})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InvestmentAdvice advice);

    @Select("SELECT * FROM investment_advice WHERE user_id = #{userId} ORDER BY created_at DESC LIMIT 1")
    InvestmentAdvice findLatestByUserId(Long userId);
}

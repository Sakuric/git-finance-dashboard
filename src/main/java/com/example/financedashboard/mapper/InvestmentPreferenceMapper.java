package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.InvestmentPreference;
import org.apache.ibatis.annotations.*;

@Mapper
public interface InvestmentPreferenceMapper {

    @Select("SELECT * FROM investment_preference WHERE user_id = #{userId}")
    InvestmentPreference findByUserId(Long userId);

    @Insert("INSERT INTO investment_preference (user_id, risk_tolerance_level, investment_horizon_type, " +
            "investment_horizon_preset, investment_horizon_custom_days, investment_horizon_custom_months, " +
            "investment_horizon_custom_years, investment_horizon_display, capital_amount, " +
            "preferred_asset_classes, preferred_industry, min_expected_return, max_acceptable_loss) " +
            "VALUES (#{userId}, #{riskToleranceLevel}, #{investmentHorizonType}, #{investmentHorizonPreset}, " +
            "#{investmentHorizonCustomDays}, #{investmentHorizonCustomMonths}, #{investmentHorizonCustomYears}, " +
            "#{investmentHorizonDisplay}, #{capitalAmount}, #{preferredAssetClasses}, #{preferredIndustry}, " +
            "#{minExpectedReturn}, #{maxAcceptableLoss})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(InvestmentPreference preference);

    @Update("UPDATE investment_preference SET risk_tolerance_level = #{riskToleranceLevel}, " +
            "investment_horizon_type = #{investmentHorizonType}, investment_horizon_preset = #{investmentHorizonPreset}, " +
            "investment_horizon_custom_days = #{investmentHorizonCustomDays}, " +
            "investment_horizon_custom_months = #{investmentHorizonCustomMonths}, " +
            "investment_horizon_custom_years = #{investmentHorizonCustomYears}, " +
            "investment_horizon_display = #{investmentHorizonDisplay}, capital_amount = #{capitalAmount}, " +
            "preferred_asset_classes = #{preferredAssetClasses}, preferred_industry = #{preferredIndustry}, " +
            "min_expected_return = #{minExpectedReturn}, max_acceptable_loss = #{maxAcceptableLoss} " +
            "WHERE user_id = #{userId}")
    void update(InvestmentPreference preference);

    @Delete("DELETE FROM investment_preference WHERE user_id = #{userId}")
    void deleteByUserId(Long userId);
}

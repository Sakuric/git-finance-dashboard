package com.example.financedashboard.controller;

import com.example.financedashboard.entity.InvestmentPreference;
import com.example.financedashboard.service.InvestmentPreferenceService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/investment-preference")
@RequiredArgsConstructor
public class InvestmentPreferenceController {

    private final InvestmentPreferenceService preferenceService;

    @GetMapping("/user/{userId}")
    public InvestmentPreference getPreference(@PathVariable Long userId) {
        return preferenceService.getByUserId(userId);
    }

    @PostMapping
    public Map<String, Object> savePreference(@RequestBody PreferenceRequest request) {
        InvestmentPreference preference = new InvestmentPreference();
        preference.setUserId(request.getUserId());
        preference.setRiskToleranceLevel(request.getRiskToleranceLevel());
        preference.setInvestmentHorizonType(request.getInvestmentHorizonType());
        preference.setInvestmentHorizonPreset(request.getInvestmentHorizonPreset());
        preference.setInvestmentHorizonCustomDays(request.getInvestmentHorizonCustomDays());
        preference.setInvestmentHorizonCustomMonths(request.getInvestmentHorizonCustomMonths());
        preference.setInvestmentHorizonCustomYears(request.getInvestmentHorizonCustomYears());
        preference.setInvestmentHorizonDisplay(request.getInvestmentHorizonDisplay());
        preference.setCapitalAmount(request.getCapitalAmount());
        preference.setPreferredAssetClasses(request.getPreferredAssetClasses());
        preference.setPreferredIndustry(request.getPreferredIndustry());
        preference.setMinExpectedReturn(request.getMinExpectedReturn());
        preference.setMaxAcceptableLoss(request.getMaxAcceptableLoss());

        preferenceService.saveOrUpdate(preference);
        return Map.of("message", "保存成功");
    }

    @DeleteMapping("/user/{userId}")
    public Map<String, String> deletePreference(@PathVariable Long userId) {
        preferenceService.delete(userId);
        return Map.of("message", "删除成功");
    }

    @Data
    static class PreferenceRequest {
        private Long userId;
        private Integer riskToleranceLevel;
        private String investmentHorizonType;
        private String investmentHorizonPreset;
        private Integer investmentHorizonCustomDays;
        private Integer investmentHorizonCustomMonths;
        private Integer investmentHorizonCustomYears;
        private String investmentHorizonDisplay;
        private BigDecimal capitalAmount;
        private String preferredAssetClasses;
        private String preferredIndustry;
        private BigDecimal minExpectedReturn;
        private BigDecimal maxAcceptableLoss;
    }
}

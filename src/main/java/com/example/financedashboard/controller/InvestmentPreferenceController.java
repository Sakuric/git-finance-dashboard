package com.example.financedashboard.controller;

import com.example.financedashboard.entity.InvestmentPreference;
import com.example.financedashboard.mapper.UserMapper;
import com.example.financedashboard.service.InvestmentPreferenceService;
import com.example.financedashboard.utils.Result;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/investment-preference")
@RequiredArgsConstructor
public class InvestmentPreferenceController {

    private final InvestmentPreferenceService preferenceService;
    private final UserMapper userMapper;

    @GetMapping("/user/{userId}")
    public InvestmentPreference getPreference(@PathVariable Long userId) {
        return preferenceService.getByUserId(userId);
    }

    @PostMapping
    public Result<String> savePreference(@RequestBody PreferenceRequest request) {
        if (request.getUserId() == null) {
            return Result.error(400, "用户未登录或会话已失效，请重新登录");
        }

        if (userMapper.findById(request.getUserId()) == null) {
            return Result.error(404, "用户不存在，请重新登录后重试");
        }

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

        try {
            preferenceService.saveOrUpdate(preference);
            return Result.success("保存成功");
        } catch (DataIntegrityViolationException ex) {
            return Result.error(400, "用户不存在或未登录，请重新登录后再试");
        } catch (Exception ex) {
            return Result.error(500, "保存失败：" + ex.getMessage());
        }
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

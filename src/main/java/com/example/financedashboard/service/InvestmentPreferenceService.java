package com.example.financedashboard.service;

import com.example.financedashboard.entity.InvestmentPreference;
import com.example.financedashboard.mapper.InvestmentPreferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InvestmentPreferenceService {

    private final InvestmentPreferenceMapper preferenceMapper;

    public InvestmentPreference getByUserId(Long userId) {
        return preferenceMapper.findByUserId(userId);
    }

    @Transactional
    public void saveOrUpdate(InvestmentPreference preference) {
        InvestmentPreference existing = preferenceMapper.findByUserId(preference.getUserId());
        if (existing == null) {
            preferenceMapper.insert(preference);
        } else {
            preferenceMapper.update(preference);
        }
    }

    @Transactional
    public void delete(Long userId) {
        preferenceMapper.deleteByUserId(userId);
    }
}

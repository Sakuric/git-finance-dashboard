package com.example.financedashboard.service;

import com.example.financedashboard.dto.KLineDTO;
import com.example.financedashboard.dto.sina.SinaRealtimeDTO;

import java.util.List;
import java.util.Map;

public interface MarketIndexService {
    List<SinaRealtimeDTO> getMainIndices();
    SinaRealtimeDTO getIndexByCode(String indexCode);
    List<KLineDTO> getIndexKLine(String indexCode, Integer days);
    Map<String, Object> getMarketOverview();
}

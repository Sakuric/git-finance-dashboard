package com.example.financedashboard.service.sina;

import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.parser.SinaDataParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 新浪数据同步服务
 * 整合API调用、数据解析、数据清洗的完整流程
 */
@Slf4j
@Service
public class SinaDataSyncService {

    @Autowired
    private SinaApiService sinaApiService;

    @Autowired
    private SinaDataParser sinaDataParser;

    @Autowired
    private SinaDataCleanService sinaDataCleanService;

    /**
     * 获取单个股票的实时数据（完整流程）
     * 
     * @param stockCode 股票代码
     * @return 清洗后的数据DTO，失败返回null
     */
    public SinaRealtimeDTO fetchAndCleanRealtimeData(String stockCode) {
        log.info("开始获取股票数据: {}", stockCode);

        // 1. 调用API获取原始数据
        String rawData = sinaApiService.fetchRealtimeData(stockCode);
        if (rawData == null || rawData.trim().isEmpty()) {
            log.error("获取原始数据失败: {}", stockCode);
            return null;
        }

        // 2. 解析数据
        SinaRealtimeDTO dto = sinaDataParser.parseRealtimeData(rawData);
        if (dto == null) {
            log.error("解析数据失败: {}", stockCode);
            return null;
        }

        // 3. 清洗数据
        if (!sinaDataCleanService.cleanRealtimeData(dto)) {
            log.error("数据清洗失败: {}", stockCode);
            return null;
        }

        log.info("成功获取股票数据: {} - {}, 当前价: {}, 涨跌幅: {}%",
                dto.getStockCode(), dto.getStockName(),
                dto.getCurrentPrice(), dto.getChangePercent());

        return dto;
    }

    /**
     * 批量获取多个股票的实时数据（完整流程）
     * 
     * @param stockCodes 股票代码列表
     * @return 清洗后的数据列表
     */
    public List<SinaRealtimeDTO> fetchAndCleanBatchRealtimeData(List<String> stockCodes) {
        log.info("开始批量获取股票数据，数量: {}", stockCodes.size());

        List<SinaRealtimeDTO> resultList = new ArrayList<>();

        // 1. 调用API获取原始数据
        String rawData = sinaApiService.fetchBatchRealtimeData(stockCodes);
        if (rawData == null || rawData.trim().isEmpty()) {
            log.error("批量获取原始数据失败");
            return resultList;
        }

        log.debug("原始数据: {}", rawData);

        // 2. 按行分割数据（支持\n、\r\n、\r多种换行符）
        String[] lines = rawData.split("\\r?\\n");
        log.info("收到{}行数据", lines.length);

        // 3. 逐行解析和清洗
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line == null || line.trim().isEmpty()) {
                log.debug("第{}行数据为空，跳过", i + 1);
                continue;
            }

            log.debug("处理第{}行: {}", i + 1, line.substring(0, Math.min(50, line.length())) + "...");

            // 解析
            SinaRealtimeDTO dto = sinaDataParser.parseRealtimeData(line);
            if (dto == null) {
                log.warn("第{}行解析失败", i + 1);
                continue;
            }

            // 清洗
            if (!sinaDataCleanService.cleanRealtimeData(dto)) {
                log.warn("第{}行清洗失败: {}", i + 1, dto.getStockCode());
                continue;
            }

            log.info("成功处理: {} - {}", dto.getStockCode(), dto.getStockName());
            resultList.add(dto);
        }

        log.info("批量获取完成，请求: {}, 成功: {}", stockCodes.size(), resultList.size());

        return resultList;
    }

    /**
     * 测试新浪API是否可用
     * 
     * @return true-可用，false-不可用
     */
    public boolean testApiAvailability() {
        log.info("测试新浪API连接...");

        try {
            // 使用贵州茅台测试
            SinaRealtimeDTO dto = fetchAndCleanRealtimeData("600519");
            boolean available = dto != null && "贵州茅台".equals(dto.getStockName());

            if (available) {
                log.info("新浪API连接正常");
            } else {
                log.error("新浪API连接异常");
            }

            return available;

        } catch (Exception e) {
            log.error("测试新浪API失败", e);
            return false;
        }
    }
}
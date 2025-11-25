package com.example.financedashboard.parser;

import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.utils.StockCodeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 新浪财经数据解析器
 * 负责解析新浪API返回的原始数据字符串
 */
@Slf4j
@Component
public class SinaDataParser {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 解析实时行情数据
     * 
     * 新浪API返回格式示例：
     * var hq_str_sh600519="贵州茅台,1758.00,1750.00,1765.50,1770.00,1750.00,1765.00,1766.00,12345,2175000000,100,1765.00,...,2024-01-15,15:00:00,00";
     * 
     * @param rawData 原始数据字符串
     * @return 解析后的DTO，解析失败返回null
     */
    public SinaRealtimeDTO parseRealtimeData(String rawData) {
        try {
            if (rawData == null || rawData.trim().isEmpty()) {
                log.warn("原始数据为空");
                return null;
            }
            
            // 提取数据部分: var hq_str_sh600519="数据内容";
            int start = rawData.indexOf("\"");
            int end = rawData.lastIndexOf("\"");
            
            if (start == -1 || end == -1 || start >= end) {
                log.warn("数据格式错误，无法找到引号: {}", rawData);
                return null;
            }
            
            String data = rawData.substring(start + 1, end);
            
            // 按逗号分割字段
            String[] fields = data.split(",");
            if (fields.length < 32) {
                log.warn("数据字段不完整，期望至少32个字段，实际{}个: {}", fields.length, rawData);
                return null;
            }
            
            // 创建DTO对象
            SinaRealtimeDTO dto = new SinaRealtimeDTO();
            dto.setRawData(rawData);
            
            // 提取股票代码
            String stockCode = extractStockCode(rawData);
            if (stockCode == null) {
                log.warn("无法提取股票代码: {}", rawData);
                return null;
            }
            dto.setStockCode(stockCode);
            
            // 解析基础信息
            dto.setStockName(fields[0].trim());
            dto.setOpenPrice(parseBigDecimal(fields[1]));
            dto.setPreClosePrice(parseBigDecimal(fields[2]));
            dto.setCurrentPrice(parseBigDecimal(fields[3]));
            dto.setHighPrice(parseBigDecimal(fields[4]));
            dto.setLowPrice(parseBigDecimal(fields[5]));
            
            // 解析成交信息
            dto.setVolume(parseLong(fields[8]));
            dto.setAmount(parseBigDecimal(fields[9]));
            
            // 计算涨跌额和涨跌幅
            calculateChange(dto);
            
            // 解析时间信息
            parseDateTime(dto, fields[30], fields[31]);
            
            log.debug("成功解析股票数据: {} - {}", dto.getStockCode(), dto.getStockName());
            return dto;
            
        } catch (Exception e) {
            log.error("解析数据失败: {}", rawData, e);
            return null;
        }
    }

    /**
     * 从原始数据中提取股票代码
     * var hq_str_sh600519="..." -> 600519
     */
    private String extractStockCode(String rawData) {
        try {
            // 找到 hq_str_ 后面的部分
            int start = rawData.indexOf("hq_str_");
            if (start == -1) {
                return null;
            }
            
            start += 7; // "hq_str_"的长度
            int end = rawData.indexOf("=", start);
            if (end == -1) {
                return null;
            }
            
            String fullCode = rawData.substring(start, end).trim();
            
            // 使用工具类提取纯数字代码
            return StockCodeUtil.extractCode(fullCode);
            
        } catch (Exception e) {
            log.error("提取股票代码失败: {}", rawData, e);
            return null;
        }
    }

    /**
     * 计算涨跌额和涨跌幅
     */
    private void calculateChange(SinaRealtimeDTO dto) {
        BigDecimal current = dto.getCurrentPrice();
        BigDecimal preClose = dto.getPreClosePrice();
        
        if (current == null || preClose == null || preClose.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }
        
        // 计算涨跌额 = 当前价 - 昨收价
        BigDecimal change = current.subtract(preClose);
        dto.setChange(change.setScale(2, RoundingMode.HALF_UP));
        
        // 计算涨跌幅 = (涨跌额 / 昨收价) * 100
        BigDecimal changePercent = change.divide(preClose, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        dto.setChangePercent(changePercent.setScale(2, RoundingMode.HALF_UP));
    }

    /**
     * 解析日期时间
     */
    private void parseDateTime(SinaRealtimeDTO dto, String dateStr, String timeStr) {
        try {
            if (dateStr == null || timeStr == null || dateStr.trim().isEmpty() || timeStr.trim().isEmpty()) {
                dto.setDataTime(LocalDateTime.now());
                return;
            }
            
            String dateTimeStr = dateStr.trim() + " " + timeStr.trim();
            dto.setDataTime(LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER));
            
        } catch (Exception e) {
            log.warn("解析时间失败，使用当前时间: {} {}", dateStr, timeStr, e);
            dto.setDataTime(LocalDateTime.now());
        }
    }

    /**
     * 安全解析BigDecimal
     */
    private BigDecimal parseBigDecimal(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP);
        } catch (Exception e) {
            log.debug("解析BigDecimal失败: {}", value);
            return null;
        }
    }

    /**
     * 安全解析Long
     */
    private Long parseLong(String value) {
        try {
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return Long.parseLong(value.trim());
        } catch (Exception e) {
            log.debug("解析Long失败: {}", value);
            return null;
        }
    }
}
package com.example.financedashboard.service.sina;

import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * 数据清洗服务
 * 负责验证和清洗从新浪API获取的数据
 */
@Slf4j
@Service
public class SinaDataCleanService {

    /**
     * 清洗实时数据
     * 
     * @param dto 原始数据DTO
     * @return true-数据有效，false-数据无效
     */
    public boolean cleanRealtimeData(SinaRealtimeDTO dto) {
        if (dto == null) {
            log.warn("数据对象为空");
            return false;
        }

        // 1. 验证必填字段
        if (!validateRequiredFields(dto)) {
            log.warn("必填字段验证失败: {}", dto.getStockCode());
            return false;
        }

        // 2. 验证价格逻辑
        if (!validatePriceLogic(dto)) {
            log.warn("价格逻辑验证失败: {}", dto.getStockCode());
            return false;
        }

        // 3. 验证异常波动（仅警告，不拒绝）
        if (!validatePriceChange(dto)) {
            log.warn("价格波动异常: {}, 涨跌幅: {}%", 
                    dto.getStockCode(), dto.getChangePercent());
            // 不返回false，只是记录警告
        }

        // 4. 数据标准化
        normalizeData(dto);

        log.debug("数据清洗通过: {} - {}", dto.getStockCode(), dto.getStockName());
        return true;
    }

    /**
     * 验证必填字段
     */
    private boolean validateRequiredFields(SinaRealtimeDTO dto) {
        // 股票代码必须存在
        if (dto.getStockCode() == null || dto.getStockCode().trim().isEmpty()) {
            log.warn("股票代码为空");
            return false;
        }

        // 股票名称必须存在
        if (dto.getStockName() == null || dto.getStockName().trim().isEmpty()) {
            log.warn("股票名称为空: {}", dto.getStockCode());
            return false;
        }

        // 当前价格必须存在
        if (dto.getCurrentPrice() == null) {
            log.warn("当前价格为空: {}", dto.getStockCode());
            return false;
        }

        // 数据时间必须存在
        if (dto.getDataTime() == null) {
            log.warn("数据时间为空: {}", dto.getStockCode());
            return false;
        }

        return true;
    }

    /**
     * 验证价格逻辑
     */
    private boolean validatePriceLogic(SinaRealtimeDTO dto) {
        BigDecimal current = dto.getCurrentPrice();
        BigDecimal high = dto.getHighPrice();
        BigDecimal low = dto.getLowPrice();
        BigDecimal open = dto.getOpenPrice();

        // 当前价格必须大于0
        if (current.compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("当前价格<=0: {}, price={}", dto.getStockCode(), current);
            return false;
        }

        // 如果有最高价和最低价，验证逻辑关系
        if (high != null && low != null) {
            // 最高价必须 >= 最低价
            if (high.compareTo(low) < 0) {
                log.warn("最高价<最低价: {}, high={}, low={}", 
                        dto.getStockCode(), high, low);
                return false;
            }

            // 当前价应该在最高最低之间（允许小幅偏差）
            if (current.compareTo(high.multiply(new BigDecimal("1.01"))) > 0) {
                log.warn("当前价>最高价: {}, current={}, high={}", 
                        dto.getStockCode(), current, high);
                return false;
            }

            if (current.compareTo(low.multiply(new BigDecimal("0.99"))) < 0) {
                log.warn("当前价<最低价: {}, current={}, low={}", 
                        dto.getStockCode(), current, low);
                return false;
            }
        }

        return true;
    }

    /**
     * 验证价格波动
     * 涨跌幅超过±20%视为异常（但不拒绝数据）
     */
    private boolean validatePriceChange(SinaRealtimeDTO dto) {
        BigDecimal changePercent = dto.getChangePercent();
        if (changePercent == null) {
            return true;
        }

        // 涨跌幅绝对值
        BigDecimal absChange = changePercent.abs();

        // 超过20%视为异常
        if (absChange.compareTo(new BigDecimal("20")) > 0) {
            return false;
        }

        return true;
    }

    /**
     * 数据标准化
     */
    private void normalizeData(SinaRealtimeDTO dto) {
        // 1. 股票名称去除首尾空格
        if (dto.getStockName() != null) {
            dto.setStockName(dto.getStockName().trim());
        }

        // 2. 股票代码转大写并去除空格
        if (dto.getStockCode() != null) {
            dto.setStockCode(dto.getStockCode().trim().toUpperCase());
        }

        // 3. 确保所有价格都保留2位小数（已在解析时处理）

        // 4. 如果成交量为null，设为0
        if (dto.getVolume() == null) {
            dto.setVolume(0L);
        }

        // 5. 如果成交额为null，设为0
        if (dto.getAmount() == null) {
            dto.setAmount(BigDecimal.ZERO);
        }
    }

    /**
     * 批量清洗数据
     * 
     * @param dtoList 数据列表
     * @return 清洗后的有效数据数量
     */
    public int batchCleanData(java.util.List<SinaRealtimeDTO> dtoList) {
        if (dtoList == null || dtoList.isEmpty()) {
            return 0;
        }

        int validCount = 0;
        for (SinaRealtimeDTO dto : dtoList) {
            if (cleanRealtimeData(dto)) {
                validCount++;
            }
        }

        log.info("批量清洗完成，总数: {}, 有效: {}, 无效: {}", 
                dtoList.size(), validCount, dtoList.size() - validCount);

        return validCount;
    }
}
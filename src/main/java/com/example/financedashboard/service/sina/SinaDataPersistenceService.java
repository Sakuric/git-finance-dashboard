package com.example.financedashboard.service.sina;

import com.example.financedashboard.dto.sina.SinaRealtimeDTO;
import com.example.financedashboard.entity.StockHistory;
import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.mapper.StockHistoryMapper;
import com.example.financedashboard.mapper.StockInfoMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 新浪数据持久化服务
 * 负责将新浪API获取的数据保存到数据库
 */
@Slf4j
@Service
public class SinaDataPersistenceService {

    @Autowired
    private SinaDataSyncService sinaDataSyncService;

    @Autowired
    private StockInfoMapper stockInfoMapper;

    @Autowired
    private StockHistoryMapper stockHistoryMapper;

    /**
     * 同步单个股票数据到数据库
     * 
     * @param stockCode 股票代码
     * @return true-成功，false-失败
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean syncStockToDatabase(String stockCode) {
        log.info("开始同步股票数据到数据库: {}", stockCode);

        try {
            // 1. 获取实时数据
            SinaRealtimeDTO realtimeData = sinaDataSyncService.fetchAndCleanRealtimeData(stockCode);
            if (realtimeData == null) {
                log.error("获取股票数据失败: {}", stockCode);
                return false;
            }

            // 2. 保存或更新stock_info表
            saveOrUpdateStockInfo(realtimeData);

            // 3. 保存到stock_history表
            saveStockHistory(realtimeData);

            log.info("成功同步股票数据: {} - {}", stockCode, realtimeData.getStockName());
            return true;

        } catch (Exception e) {
            log.error("同步股票数据失败: {}", stockCode, e);
            throw new RuntimeException("同步股票数据失败: " + e.getMessage(), e);
        }
    }

    /**
     * 批量同步股票数据到数据库
     * 
     * @param stockCodes 股票代码列表
     * @return 成功同步的数量
     */
    @Transactional(rollbackFor = Exception.class)
    public int batchSyncStocksToDatabase(List<String> stockCodes) {
        log.info("开始批量同步股票数据，数量: {}", stockCodes.size());

        int successCount = 0;
        List<StockInfo> stockInfoList = new ArrayList<>();
        List<StockHistory> stockHistoryList = new ArrayList<>();

        // 1. 批量获取数据
        List<SinaRealtimeDTO> realtimeDataList = sinaDataSyncService
                .fetchAndCleanBatchRealtimeData(stockCodes);

        if (realtimeDataList.isEmpty()) {
            log.warn("未获取到任何股票数据");
            return 0;
        }

        // 2. 转换为实体对象
        for (SinaRealtimeDTO dto : realtimeDataList) {
            try {
                // 转换StockInfo
                StockInfo stockInfo = convertToStockInfo(dto);
                stockInfoList.add(stockInfo);

                // 转换StockHistory
                StockHistory stockHistory = convertToStockHistory(dto);
                stockHistoryList.add(stockHistory);

                successCount++;
            } catch (Exception e) {
                log.error("转换数据失败: {}", dto.getStockCode(), e);
            }
        }

        // 3. 批量保存
        try {
            if (!stockInfoList.isEmpty()) {
                // 逐个插入或更新StockInfo（因为需要获取ID）
                for (StockInfo stockInfo : stockInfoList) {
                    saveOrUpdateStockInfoEntity(stockInfo);
                }
                log.info("批量保存StockInfo成功，数量: {}", stockInfoList.size());
            }

            if (!stockHistoryList.isEmpty()) {
                // 批量插入或更新StockHistory
                int count = stockHistoryMapper.batchInsertOrUpdate(stockHistoryList);
                log.info("批量保存StockHistory成功，数量: {}", count);
            }

        } catch (Exception e) {
            log.error("批量保存数据失败", e);
            throw new RuntimeException("批量保存数据失败: " + e.getMessage(), e);
        }

        log.info("批量同步完成，成功: {}/{}", successCount, stockCodes.size());
        return successCount;
    }

    /**
     * 保存或更新StockInfo
     */
    private void saveOrUpdateStockInfo(SinaRealtimeDTO dto) {
        StockInfo stockInfo = convertToStockInfo(dto);
        saveOrUpdateStockInfoEntity(stockInfo);
    }

    /**
     * 保存或更新StockInfo实体
     */
    private void saveOrUpdateStockInfoEntity(StockInfo stockInfo) {
        // 查询是否已存在
        StockInfo existing = stockInfoMapper.findByStockCode(stockInfo.getStockCode());

        if (existing == null) {
            // 新增
            stockInfoMapper.insert(stockInfo);
            log.debug("新增StockInfo: {}", stockInfo.getStockCode());
        } else {
            // 更新
            stockInfo.setId(existing.getId());
            stockInfoMapper.update(stockInfo);
            log.debug("更新StockInfo: {}", stockInfo.getStockCode());
        }
    }

    /**
     * 保存StockHistory
     */
    private void saveStockHistory(SinaRealtimeDTO dto) {
        StockHistory stockHistory = convertToStockHistory(dto);

        // 使用insertOrUpdate，如果当天已有数据则更新
        stockHistoryMapper.insertOrUpdate(stockHistory);
        log.debug("保存StockHistory: {} - {}", dto.getStockCode(), dto.getDataTime().toLocalDate());
    }

    /**
     * 将SinaRealtimeDTO转换为StockInfo
     */
    private StockInfo convertToStockInfo(SinaRealtimeDTO dto) {
        StockInfo stockInfo = new StockInfo();

        // 基础信息
        stockInfo.setStockCode(dto.getStockCode());
        stockInfo.setStockSymbol(dto.getStockCode());
        stockInfo.setStockName(dto.getStockName());

        // 判断交易所
        String exchange = dto.getStockCode().startsWith("6") ? "SH" : "SZ";
        stockInfo.setExchange(exchange);

        // 价格信息
        stockInfo.setCurrentPrice(dto.getCurrentPrice());
        stockInfo.setYesterdayClose(dto.getPreClosePrice());
        stockInfo.setChangePercent(dto.getChangePercent());

        // 状态和时间
        stockInfo.setStatus(1); // 正常交易
        stockInfo.setLastUpdateTime(dto.getDataTime());
        stockInfo.setCreatedAt(LocalDateTime.now());
        stockInfo.setUpdatedAt(LocalDateTime.now());

        return stockInfo;
    }

    /**
     * 将SinaRealtimeDTO转换为StockHistory
     */
    private StockHistory convertToStockHistory(SinaRealtimeDTO dto) {
        StockHistory history = new StockHistory();

        // 基础信息
        history.setStockCode(dto.getStockCode());
        history.setTradeDate(dto.getDataTime().toLocalDate());

        // 获取stock_id（必须字段）
        StockInfo stockInfo = stockInfoMapper.findByStockCode(dto.getStockCode());
        if (stockInfo != null) {
            history.setStockId(stockInfo.getId());
        } else {
            log.warn("未找到股票信息: {}", dto.getStockCode());
            // 如果找不到stock_info记录，先创建一个
            StockInfo newStockInfo = convertToStockInfo(dto);
            saveOrUpdateStockInfoEntity(newStockInfo);
            history.setStockId(newStockInfo.getId());
        }

        // 价格信息
        history.setOpenPrice(dto.getOpenPrice());
        history.setHighPrice(dto.getHighPrice());
        history.setLowPrice(dto.getLowPrice());
        history.setClosePrice(dto.getCurrentPrice());

        // 成交信息
        history.setVolume(dto.getVolume());
        history.setAmount(dto.getAmount());

        // 涨跌信息
        history.setChangeAmount(dto.getChange());
        history.setChangePercent(dto.getChangePercent());

        // 时间
        history.setCreatedAt(LocalDateTime.now());
        history.setUpdatedAt(LocalDateTime.now());

        return history;
    }

    /**
     * 获取数据库中的所有股票代码
     * 
     * @return 股票代码列表
     */
    public List<String> getAllStockCodesFromDatabase() {
        List<StockInfo> stockInfoList = stockInfoMapper.findAll();
        List<String> stockCodes = new ArrayList<>();

        for (StockInfo stockInfo : stockInfoList) {
            stockCodes.add(stockInfo.getStockCode());
        }

        log.info("从数据库获取到{}只股票", stockCodes.size());
        return stockCodes;
    }
}
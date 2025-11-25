package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StockHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 股票历史数据Mapper
 */
@Mapper
public interface StockHistoryMapper {
    
    /**
     * 插入单条历史数据
     */
    int insert(StockHistory stockHistory);
    
    /**
     * 批量插入历史数据
     */
    int batchInsert(@Param("list") List<StockHistory> list);
    
    /**
     * 根据股票代码和日期查询
     */
    StockHistory findByStockCodeAndDate(
            @Param("stockCode") String stockCode, 
            @Param("tradeDate") LocalDate tradeDate);
    
    /**
     * 更新历史数据
     */
    int update(StockHistory stockHistory);
    
    /**
     * 插入或更新（如果存在则更新）
     */
    int insertOrUpdate(StockHistory stockHistory);
    
    /**
     * 批量插入或更新
     */
    int batchInsertOrUpdate(@Param("list") List<StockHistory> list);
    
    /**
     * 查询股票的最新N条历史数据
     */
    List<StockHistory> findLatestByStockCode(
            @Param("stockCode") String stockCode, 
            @Param("limit") int limit);
    
    /**
     * 查询指定日期范围的历史数据
     */
    List<StockHistory> findByStockCodeAndDateRange(
            @Param("stockCode") String stockCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
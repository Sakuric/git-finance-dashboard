package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StockInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface StockInfoMapper {
    List<StockInfo> findAll();

    StockInfo findByStockCode(@Param("stockCode")String stockCode);

    int insert(StockInfo stockInfo);
    int batchInsert(@Param("stockInfos") List<StockInfo> stockInfos);
    int batchInsertOrUpdate(@Param("stockInfos") List<StockInfo> stockInfos);
    int update(StockInfo stockInfo);
    int deleteByStockCode(@Param("stockCode")String stockCode);

    /**
     * 获取所有股票代码
     */
    List<String> getAllStockCodes();

    /**
     * 更新股票的完整信息（行业、板块、市值、上市日期）
     */
    int updateStockCompleteInfo(
            @Param("stockCode") String stockCode,
            @Param("industry") String industry,
            @Param("sector") String sector,
            @Param("marketValue") BigDecimal marketValue,
            @Param("listingDate") LocalDate listingDate
    );
}

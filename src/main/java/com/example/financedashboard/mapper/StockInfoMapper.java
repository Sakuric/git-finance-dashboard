package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StockInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockInfoMapper {
    List<StockInfo> findAll();

    StockInfo findByStockCode(@Param("stockCode")String stockCode);

    int insert(StockInfo stockInfo);
    int batchInsert(@Param("stockInfos") List<StockInfo> stockInfos);
    int update(StockInfo stockInfo);
    int deleteByStockCode(@Param("stockCode")String stockCode);

}

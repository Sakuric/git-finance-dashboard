package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StockBacktestDetail;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StockBacktestDetailMapper {

    int insert(StockBacktestDetail detail);

    int batchInsert(@Param("list") List<StockBacktestDetail> list);

    List<StockBacktestDetail> findByBacktestId(@Param("backtestId") Long backtestId);

    List<StockBacktestDetail> findByAdviceId(@Param("adviceId") Long adviceId);
}

package com.example.financedashboard.mapper;

import com.example.financedashboard.entity.StockInfo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface StockInfoMapper {



    List<StockInfo> findAll();

}

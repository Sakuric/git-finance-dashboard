package com.example.financedashboard.service;

import com.example.financedashboard.entity.StockInfo;
import com.example.financedashboard.mapper.StockInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StockService {

    @Autowired
    private StockInfoMapper stockInfoMapper;

    public List<StockInfo> getAllStockInfo() {
        return stockInfoMapper.findAll();
    }

}

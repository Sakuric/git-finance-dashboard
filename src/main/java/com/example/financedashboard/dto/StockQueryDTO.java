package com.example.financedashboard.dto;

import lombok.Data;

/**
 * 股票查询数据传输对象
 * DTO（Data Transfer Object）是一种设计模式，用于在不同层之间传输数据
 * StockQueryDTO用于封装前端传递到后端的股票查询条件
 */
@Data  // Lombok注解，自动生成getter、setter等方法
public class StockQueryDTO {
    private String keyword;  // 搜索关键词，可用于搜索股票代码或股票名称
    private String industry; // 行业筛选，用于按行业分类筛选股票
}
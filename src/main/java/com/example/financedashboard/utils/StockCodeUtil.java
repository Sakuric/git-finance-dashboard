package com.example.financedashboard.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * 股票代码工具类
 * 用于处理股票代码的格式转换和验证
 */
public class StockCodeUtil {

    /**
     * 将纯数字股票代码转换为带市场前缀的完整代码
     * 
     * @param stockCode 股票代码（如：600519）
     * @return 完整代码（如：sh600519）
     */
    public static String toFullCode(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return null;
        }
        
        // 去除空格
        stockCode = stockCode.trim();
        
        // 如果已经有前缀，直接返回
        if (stockCode.startsWith("sh") || stockCode.startsWith("sz")) {
            return stockCode.toLowerCase();
        }
        
        // 根据代码首位数字判断市场
        if (stockCode.startsWith("6")) {
            // 6开头：上交所主板
            return "sh" + stockCode;
        } else if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            // 0开头：深交所主板，3开头：创业板
            return "sz" + stockCode;
        } else if (stockCode.startsWith("688")) {
            // 688开头：科创板
            return "sh" + stockCode;
        }
        
        return stockCode;
    }

    /**
     * 从完整代码中提取纯数字股票代码
     * 
     * @param fullCode 完整代码（如：sh600519）
     * @return 纯数字代码（如：600519）
     */
    public static String extractCode(String fullCode) {
        if (StringUtils.isBlank(fullCode)) {
            return null;
        }
        
        fullCode = fullCode.trim().toLowerCase();
        
        // 去除市场前缀
        if (fullCode.startsWith("sh") || fullCode.startsWith("sz")) {
            return fullCode.substring(2);
        }
        
        return fullCode;
    }

    /**
     * 验证股票代码格式是否正确
     * 
     * @param stockCode 股票代码
     * @return true-格式正确，false-格式错误
     */
    public static boolean isValidCode(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return false;
        }
        
        stockCode = stockCode.trim();
        
        // 纯数字代码：6位数字
        if (stockCode.matches("^\\d{6}$")) {
            return true;
        }
        
        // 带前缀代码：sh或sz + 6位数字
        if (stockCode.matches("^(sh|sz|SH|SZ)\\d{6}$")) {
            return true;
        }
        
        return false;
    }

    /**
     * 获取股票所属市场
     * 
     * @param stockCode 股票代码
     * @return 市场名称（上交所/深交所/未知）
     */
    public static String getMarket(String stockCode) {
        if (StringUtils.isBlank(stockCode)) {
            return "未知";
        }
        
        stockCode = extractCode(stockCode);
        
        if (stockCode.startsWith("6") || stockCode.startsWith("688")) {
            return "上交所";
        } else if (stockCode.startsWith("0") || stockCode.startsWith("3")) {
            return "深交所";
        }
        
        return "未知";
    }
}
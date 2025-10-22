package com.example.finance.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
@AllArgsConstructor
public enum ResultCode {
    
    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),
    
    /**
     * 参数错误
     */
    BAD_REQUEST(400, "参数错误"),
    
    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权"),
    
    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),
    
    /**
     * 资源不存在
     */
    NOT_FOUND(404, "资源不存在"),
    
    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "方法不允许"),
    
    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),
    
    /**
     * 请求实体过大
     */
    PAYLOAD_TOO_LARGE(413, "请求实体过大"),
    
    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    /**
     * 内部服务器错误
     */
    INTERNAL_SERVER_ERROR(500, "内部服务器错误"),
    
    /**
     * 服务不可用
     */
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    /**
     * 网关超时
     */
    GATEWAY_TIMEOUT(504, "网关超时"),
    
    /**
     * 用户相关错误码
     */
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_PASSWORD_ERROR(1003, "密码错误"),
    USER_ACCOUNT_DISABLED(1004, "用户账户已禁用"),
    USER_TOKEN_EXPIRED(1005, "用户令牌已过期"),
    USER_TOKEN_INVALID(1006, "用户令牌无效"),
    
    /**
     * 股票相关错误码
     */
    STOCK_NOT_FOUND(2001, "股票不存在"),
    STOCK_ALREADY_EXISTS(2002, "股票已存在"),
    STOCK_DATA_INVALID(2003, "股票数据无效"),
    STOCK_PRICE_INVALID(2004, "股票价格无效"),
    
    /**
     * 分析相关错误码
     */
    ANALYSIS_DATA_NOT_FOUND(3001, "分析数据不存在"),
    ANALYSIS_CALCULATION_ERROR(3002, "分析计算错误"),
    ANALYSIS_PARAMETERS_INVALID(3003, "分析参数无效"),
    
    /**
     * 系统相关错误码
     */
    SYSTEM_ERROR(9001, "系统错误"),
    DATABASE_ERROR(9002, "数据库错误"),
    NETWORK_ERROR(9003, "网络错误"),
    FILE_UPLOAD_ERROR(9004, "文件上传错误"),
    CONFIGURATION_ERROR(9005, "配置错误");
    
    /**
     * 响应码
     */
    private final Integer code;
    
    /**
     * 响应消息
     */
    private final String message;
}